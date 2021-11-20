package com.ldv.samlproxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/18/21
 */
@Configuration
public class DataStoreConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreConfig.class);

    @Value("${custom.config-file}")
    private String configFile;

    @PostConstruct
    public void init() throws Exception {
        LOGGER.info("Configuration file will be stored in as [{}]", configFile);

        File configFile = new File(this.configFile);

        if (configFile.exists()) {
            LOGGER.info("Config file [{}] already exist", configFile);
        } else {
            LOGGER.info("Creating new config file [{}]", configFile);

            InputStream config = this.getClass().getClassLoader().getResourceAsStream(configFile.getName());
            Assert.notNull(config, String.format("%s file doesn't exist in classpath", configFile.getName()));

            boolean created = configFile.getParentFile().mkdirs();
            if (!created) {
                LOGGER.warn("Unable to create directories for {} path", configFile);
            }

            FileCopyUtils.copy(config, new FileOutputStream(configFile));
        }
    }
}
