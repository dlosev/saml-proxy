package com.ldv.samlproxy.controller.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.ldv.samlproxy.dto.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
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

    private final ObjectMapper mapper;

    private File configFile;

    @Autowired
    private Environment env;

    @Value("${custom.data-dir}")
    private String dataDir;

    public ConfigManager() {
        mapper = new ObjectMapper(YAMLFactory.builder()
                .disable(WRITE_DOC_START_MARKER)
                .build());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @PostConstruct
    public void init() {
        configFile = new File(env.getRequiredProperty("custom.config-file"));
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

        return config.get();
    }

    public void saveConfig(Config config) throws Exception {
        ObjectReader objectReader = mapper.readerForUpdating(loadConfig());
        Config updatedConfig = objectReader.readValue(mapper.writeValueAsString(config));

        mapper.writeValue(configFile, updatedConfig);
    }
}
