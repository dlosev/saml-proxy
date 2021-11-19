package com.ldv.samlproxy.controller.service;

import com.ldv.samlproxy.dto.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/19/21
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConfigManagerTest {

    private ConfigManager configManager = new ConfigManager();

    @Test
    public void testLoadConfig() throws Exception {
        Config config = configManager.loadConfig();

        assertThat(config, notNullValue());
        assertThat(config.getCustomLoginRedirectUrl(), is(not(emptyString())));
    }
}
