package com.ldv.samlproxy.config;

import com.ldv.samlproxy.filter.SamlConfigurationFilter;
import com.ldv.samlproxy.saml2.RelyingPartyRegistrationBuilder;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/13/21
 */
@Configuration
@Order(20)
@EnableWebSecurity
@DependsOn("dataStoreConfig")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
                .antMatchers("/auth").hasRole("USER")
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
        http.addFilterBefore(saml2MetadataFilter(), Saml2WebSsoAuthenticationFilter.class);
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
    public SamlConfigurationFilter samlConfigurationFilter() {
        return new SamlConfigurationFilter("idp");
    }

    public Saml2MetadataFilter saml2MetadataFilter() {
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver =
                new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository());

        return new Saml2MetadataFilter(relyingPartyRegistrationResolver, new OpenSamlMetadataResolver());
    }
}
