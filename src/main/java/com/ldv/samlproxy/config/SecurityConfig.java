package com.ldv.samlproxy.config;

import com.ldv.samlproxy.filter.SamlConfigurationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
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

    @Value("${custom.idp-metadata-location}")
    private Resource idpMetadata;

    @Value("classpath:default-idp-metadata.xml")
    private Resource defaultIdpMetadata;

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
                .saml2Logout();

        http.addFilterBefore(samlConfigurationFilter(), Saml2WebSsoAuthenticationRequestFilter.class);
        http.addFilterBefore(saml2MetadataFilter(), Saml2WebSsoAuthenticationFilter.class);
    }

    @Bean
    @RefreshScope
    public InMemoryRelyingPartyRegistrationRepository relyingPartyRegistrationRepository() throws Exception {
        String idpMetadataLocation = (idpMetadata.exists() ? idpMetadata : defaultIdpMetadata).getURI().toString();

        RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistrations
                .fromMetadataLocation(idpMetadataLocation)
                .registrationId("idp")
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(relyingPartyRegistration);
    }

    @Bean
    public SamlConfigurationFilter samlConfigurationFilter() {
        return new SamlConfigurationFilter("idp");
    }

    public Saml2MetadataFilter saml2MetadataFilter() throws Exception {
        RelyingPartyRegistrationResolver relyingPartyRegistrationResolver =
                new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository());

        return new Saml2MetadataFilter(relyingPartyRegistrationResolver, new OpenSamlMetadataResolver());
    }
}
