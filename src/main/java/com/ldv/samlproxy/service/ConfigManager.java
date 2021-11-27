package com.ldv.samlproxy.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ldv.samlproxy.dto.config.SystemConfig;
import com.ldv.samlproxy.dto.config.IdpConfig;
import com.ldv.samlproxy.dto.config.SpConfig;
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

    public SystemConfig loadSystemConfig() {
        return mapper.convertValue(readAllConfig(), SystemConfig.class);
    }

    public SpConfig loadSpConfig() {
        SpConfig config = mapper.convertValue(readAllConfig(), SpConfig.class);

        readCredentials(config);

        return config;
    }

    public IdpConfig loadIdpConfig() {
        IdpConfig config = mapper.convertValue(readAllConfig(), IdpConfig.class);

        config.setIdpMetadata(readMetadata());

        return config;
    }

    public String readMetadata() {
        if (idpMetadata.exists()) {
            try (InputStreamReader isr = new InputStreamReader(idpMetadata.getInputStream())) {
                return FileCopyUtils.copyToString(isr);
            } catch (Exception e) {
                LOGGER.warn("Unable to read IDP metadata file", e);
            }
        }

        return null;
    }

    public void saveSystemConfig(SystemConfig config) throws Exception {
        saveConfig(config);
    }

    public void saveSpConfig(SpConfig config) throws Exception {
        writeCredentials(config);

        saveConfig(config);
    }

    public void saveIdpConfig(IdpConfig config) throws Exception {
        writeMetadata(config);

        saveConfig(config);
    }

    private <T> void saveConfig(T newConfig) throws Exception {
        ObjectReader objectReader = mapper.readerForUpdating(readAllConfig());
        T updatedConfig = objectReader.readValue(mapper.writeValueAsString(newConfig));

        mapper.writeValue(configFile, updatedConfig);
    }

    private Map<String, String> readAllConfig() {
        AtomicReference<Map<String, String>> config = new AtomicReference<>();

        ((AbstractEnvironment) env).getPropertySources().stream()
                .filter(ps -> ps.getName().contains(configFile.getName())).findFirst().ifPresent(ps -> {
                    Map<String, Object> propertySource = ((OriginTrackedMapPropertySource) ps).getSource();
                    Map<String, String> configMap = new HashMap<>(propertySource.size());

                    propertySource.keySet().forEach(key -> configMap.put(key, env.getProperty(key)));

                    config.set(configMap);
                });

        return config.get();
    }

    private void readCredentials(SpConfig config) {
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

    private void writeCredentials(SpConfig config) throws Exception {
        if (config.getSpSigningPrivateKey() != null) {
            if (!config.getSpSigningPrivateKey().isEmpty()) {
                FileCopyUtils.copy(config.getSpSigningPrivateKey().getBytes(), privateKeyFile.getFile());

                config.setSpSigningPrivateKeyLocation(privateKeyFile.getURI().toString());
            } else {
                deleteFile(privateKeyFile);
                config.setSpSigningPrivateKeyLocation(null);
            }
        }

        if (config.getSpSigningX509Certificate() != null) {
            if (!config.getSpSigningX509Certificate().isEmpty()) {
                FileCopyUtils.copy(config.getSpSigningX509Certificate().getBytes(), certificateFile.getFile());

                config.setSpSigningX509CertificateLocation(certificateFile.getURI().toString());
            } else {
                deleteFile(certificateFile);
                config.setSpSigningX509CertificateLocation(null);
            }
        }
    }

    private void writeMetadata(IdpConfig config) throws Exception {
        if (config.getIdpMetadata() != null) {
            if (!config.getIdpMetadata().isEmpty()) {
                FileCopyUtils.copy(config.getIdpMetadata().getBytes(), idpMetadata.getFile());
            } else {
                deleteFile(idpMetadata);
            }
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
