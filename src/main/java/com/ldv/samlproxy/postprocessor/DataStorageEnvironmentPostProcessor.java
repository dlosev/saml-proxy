package com.ldv.samlproxy.postprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/28/21
 */
@Order(ConfigDataEnvironmentPostProcessor.ORDER - 1)
public class DataStorageEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStorageEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configFilePath = getConfigFilePath(environment);;

        LOGGER.info("Configuration file will be stored in as [{}]", configFilePath);

        File configFile = new File(configFilePath);

        if (configFile.exists()) {
            LOGGER.info("Config file [{}] already exist", configFile);
        } else {
            LOGGER.info("Creating new config file [{}]", configFile);

            InputStream config = this.getClass().getClassLoader().getResourceAsStream(configFile.getName());
            Assert.notNull(config, String.format("%s file doesn't exist in classpath", configFile.getName()));

            if (!configFile.getParentFile().exists()) {
                boolean created = configFile.getParentFile().mkdirs();

                if (!created) {
                    LOGGER.warn("Unable to create directories for {} file", configFile);
                }
            }

            try {
                FileCopyUtils.copy(config, new FileOutputStream(configFile));
            } catch (Exception e) {
                LOGGER.error("Unable to write config to a file:", e);

                throw new RuntimeException(e);
            }
        }
    }

    private String getConfigFilePath(ConfigurableEnvironment environment) {
        String propertySourceName = "tmp";

        List<PropertySource<?>> applicationPropertySource;

        try {
            applicationPropertySource = new YamlPropertySourceLoader().load(propertySourceName, new ClassPathResource("application.yaml"));
        } catch (Exception e) {
            LOGGER.error("Unable to read config file path from the application properties file:", e);

            throw new RuntimeException(e);
        }

        environment.getPropertySources().addLast(applicationPropertySource.get(0));

        String configFilePath = environment.getProperty("custom.config-file");

        environment.getPropertySources().remove(propertySourceName);

        return configFilePath;
    }
}
