package com.ldv.samlproxy.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ldv.samlproxy.dto.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/19/21
 */
@Component
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private final ObjectMapper mapper;

    @Value("${custom.config-file}")
    private File configFile;

    @Value("${custom.idp-metadata-location}")
    private Resource idpMetadata;

    @Value("file:${custom.data-dir}/private-key.pem")
    private Resource privateKeyFile;

    @Value("file:${custom.data-dir}/certificate.pem")
    private Resource certificateFile;

    @Autowired
    private Environment env;

    public ConfigManager() {
        mapper = new ObjectMapper(YAMLFactory.builder()
                .disable(WRITE_DOC_START_MARKER)
                .build());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public Config loadConfig() {
        AtomicReference<Config> config = new AtomicReference<>();

        ((AbstractEnvironment) env).getPropertySources().stream()
                .filter(ps -> ps.getName().contains(configFile.getName())).findFirst().ifPresent(ps -> {
                    Map<String, Object> propertySource = ((OriginTrackedMapPropertySource) ps).getSource();
                    Map<String, String> configMap = new HashMap<>(propertySource.size());

                    propertySource.keySet().forEach(key -> configMap.put(key, env.getProperty(key)));

                    config.set(mapper.convertValue(configMap, Config.class));
                });

        Config result = config.get();

        readCredentials(result);
        readMetadata(result);

        return result;
    }

    public void saveConfig(Config config) throws Exception {
        writeCredentials(config);
        writeMetadata(config);

        ObjectReader objectReader = mapper.readerForUpdating(loadConfig());
        Config updatedConfig = objectReader.readValue(mapper.writeValueAsString(config));

        mapper.writeValue(configFile, updatedConfig);
    }

    private void readCredentials(Config config) {
        if (privateKeyFile.exists()) {
            try (InputStreamReader isr = new InputStreamReader(privateKeyFile.getInputStream())) {
                config.setSpSigningPrivateKey(FileCopyUtils.copyToString(isr));

                config.setSpSigningPrivateKeyLocation(privateKeyFile.getURI().toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to read private file", e);
            }
        }

        if (certificateFile.exists()) {
            try (InputStreamReader isr = new InputStreamReader(certificateFile.getInputStream())) {
                config.setSpSigningX509Certificate(FileCopyUtils.copyToString(isr));

                config.setSpSigningX509CertificateLocation(certificateFile.getURI().toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to read X509 certificate form file", e);
            }
        }
    }

    private void readMetadata(Config config) {
        if (idpMetadata.exists()) {
            try (InputStreamReader isr = new InputStreamReader(idpMetadata.getInputStream())) {
                config.setIdpMetadata(FileCopyUtils.copyToString(isr));
            } catch (Exception e) {
                LOGGER.warn("Unable to read IDP metadata file", e);
            }
        }
    }

    private void writeCredentials(Config config) throws Exception {
        if (StringUtils.hasText(config.getSpSigningPrivateKey())) {
            FileCopyUtils.copy(config.getSpSigningPrivateKey().getBytes(), privateKeyFile.getFile());

            config.setSpSigningPrivateKeyLocation(privateKeyFile.getURI().toString());
        } else {
            deleteFile(privateKeyFile);
            config.setSpSigningPrivateKeyLocation(null);
        }

        if (StringUtils.hasText(config.getSpSigningX509Certificate())) {
            FileCopyUtils.copy(config.getSpSigningX509Certificate().getBytes(), certificateFile.getFile());

            config.setSpSigningX509CertificateLocation(certificateFile.getURI().toString());
        } else {
            deleteFile(certificateFile);
            config.setSpSigningX509CertificateLocation(null);
        }
    }

    private void writeMetadata(Config config) throws Exception {
        if (StringUtils.hasText(config.getIdpMetadata())) {
            FileCopyUtils.copy(config.getIdpMetadata().getBytes(), idpMetadata.getFile());
        } else {
            deleteFile(idpMetadata);
        }
    }

    private void deleteFile(Resource file) throws Exception {
        if (file.exists()) {
            boolean deleted = file.getFile().delete();

            if (!deleted) {
                LOGGER.warn("Unable to delete file [{}]", file.getFilename());
            }
        }
    }
}
