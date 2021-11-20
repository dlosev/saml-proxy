package com.ldv.samlproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;

import javax.servlet.http.HttpServletRequest;

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

    @Autowired
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

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
                .saml2Login();

        http.addFilterBefore(saml2MetadataFilter(), Saml2WebSsoAuthenticationFilter.class);
    }

    public Saml2MetadataFilter saml2MetadataFilter() {
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver =
                new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);

        return new Saml2MetadataFilter(relyingPartyRegistrationResolver, new OpenSamlMetadataResolver());
    }
}
