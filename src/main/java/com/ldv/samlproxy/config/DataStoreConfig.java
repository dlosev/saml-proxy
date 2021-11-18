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
    private static final String CONFIG_FILE_NAME = "saml-proxy.yaml";

    @Value("${custom.data-dir}")
    private String dataDir;

    @PostConstruct
    public void init() throws Exception {
        LOGGER.info("Configuration file will be stored in [{}] directory", dataDir);

        File configFile = new File(dataDir, CONFIG_FILE_NAME);

        if (configFile.exists()) {
            LOGGER.info("Config file [{}] already exist", configFile);
        } else {
            LOGGER.info("Creating new config file [{}]", configFile);

            InputStream config = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
            Assert.notNull(config, String.format("%s file doesn't exist in classpath", CONFIG_FILE_NAME));

            FileCopyUtils.copy(config, new FileOutputStream(configFile));
        }
    }
}
