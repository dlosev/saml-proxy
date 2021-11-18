package com.ldv.samlproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/16/21
 */
@Configuration
@Order(10)
@DependsOn("dataStoreConfig")
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/admin", "/admin/**")
                .and()
                .authorizeRequests()
                .antMatchers("/admin/login").hasAnyRole("ANONYMOUS", "ADMIN")
                .anyRequest().hasRole("ADMIN").and()
                .formLogin()
                    .loginPage("/admin/login")
                    .defaultSuccessUrl("/admin/config")
                .and()
                .logout()
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(env.getRequiredProperty("custom.admin.username"))
                .password(passwordEncoder().encode(env.getRequiredProperty("custom.admin.password")))
                .authorities("ROLE_ADMIN");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
