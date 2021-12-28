package com.ldv.samlproxy.config;

import com.ldv.samlproxy.filter.SamlConfigurationFilter;
import com.ldv.samlproxy.filter.SamlSessionExpirationFilter;
import com.ldv.samlproxy.saml2.RelyingPartyRegistrationBuilder;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.session.SessionManagementFilter;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/13/21
 */
@Configuration
@Order(20)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String AUTH_REQUEST_PATTERN = "/auth";

    public SecurityConfig() {
        super(true);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling();
        http.sessionManagement();
        http.securityContext();
        http.anonymous();

        http
                .authorizeRequests()
                .antMatchers(AUTH_REQUEST_PATTERN).hasRole("USER")
                .and()
                .saml2Login()
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
                .and()
                .logout()
                .logoutSuccessUrl("/auth")
                .and()
                .saml2Logout()
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository());

        http.addFilterBefore(samlConfigurationFilter(), Saml2WebSsoAuthenticationRequestFilter.class);

        Saml2MetadataFilter saml2MetadataFilter = new Saml2MetadataFilter(relyingPartyRegistrationResolver(),
                new OpenSamlMetadataResolver());
        http.addFilterBefore(saml2MetadataFilter, Saml2WebSsoAuthenticationFilter.class);

        http.addFilterBefore(samlSessionExpirationFilter(), SessionManagementFilter.class);
    }

    @Bean
    public Saml2RelyingPartyProperties saml2RelyingPartyProperties() {
        return new Saml2RelyingPartyProperties();
    }

    @Bean
    @RefreshScope
    public InMemoryRelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        return new InMemoryRelyingPartyRegistrationRepository(
                new RelyingPartyRegistrationBuilder().build(saml2RelyingPartyProperties()));
    }

    @Bean
    public RelyingPartyRegistrationResolver relyingPartyRegistrationResolver() {
        return new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository());
    }

    @Bean
    public SamlConfigurationFilter samlConfigurationFilter() {
        return new SamlConfigurationFilter("idp");
    }

    @Bean
    public SamlSessionExpirationFilter samlSessionExpirationFilter() {
        return new SamlSessionExpirationFilter(AUTH_REQUEST_PATTERN);
    }
}
