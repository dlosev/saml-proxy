package com.ldv.samlproxy.saml2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Duplicates {@link org.springframework.boot.autoconfigure.security.saml2}.Saml2RelyingPartyRegistrationConfiguration
 * functionality, because it's not allowed to extend that class. The only difference is it makes
 * {@link RefreshableSaml2RelyingPartyRegistrationConfiguration} refreshable.
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/21/21
 */
@Configuration(proxyBeanMethods = false)
@Conditional(RegistrationConfiguredCondition.class)
@ConditionalOnMissingBean(RelyingPartyRegistrationRepository.class)
public class RefreshableSaml2RelyingPartyRegistrationConfiguration {

    @Bean
    @RefreshScope
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(Saml2RelyingPartyProperties properties) {
        List<RelyingPartyRegistration> registrations = properties.getRegistration().entrySet().stream()
                .map(this::asRegistration).collect(Collectors.toList());
        return new InMemoryRelyingPartyRegistrationRepository(registrations);
    }

    private RelyingPartyRegistration asRegistration(Map.Entry<String, Saml2RelyingPartyProperties.Registration> entry) {
        return asRegistration(entry.getKey(), entry.getValue());
    }

    private RelyingPartyRegistration asRegistration(String id, Saml2RelyingPartyProperties.Registration properties) {
        boolean usingMetadata = StringUtils.hasText(properties.getIdentityprovider().getMetadataUri());
        RelyingPartyRegistration.Builder builder = (usingMetadata) ? RelyingPartyRegistrations
                .fromMetadataLocation(properties.getIdentityprovider().getMetadataUri()).registrationId(id)
                : RelyingPartyRegistration.withRegistrationId(id);
        builder.assertionConsumerServiceLocation(properties.getAcs().getLocation());
        builder.assertionConsumerServiceBinding(properties.getAcs().getBinding());
        builder.assertingPartyDetails(mapIdentityProvider(properties, usingMetadata));
        builder.signingX509Credentials((credentials) -> properties.getSigning().getCredentials().stream()
                .map(this::asSigningCredential).forEach(credentials::add));
        builder.decryptionX509Credentials((credentials) -> properties.getDecryption().getCredentials().stream()
                .map(this::asDecryptionCredential).forEach(credentials::add));
        builder.assertingPartyDetails((details) -> details
                .verificationX509Credentials((credentials) -> properties.getIdentityprovider().getVerification()
                        .getCredentials().stream().map(this::asVerificationCredential).forEach(credentials::add)));
        builder.entityId(properties.getEntityId());
        RelyingPartyRegistration registration = builder.build();
        boolean signRequest = registration.getAssertingPartyDetails().getWantAuthnRequestsSigned();
        validateSigningCredentials(properties, signRequest);
        return registration;
    }

    private Consumer<RelyingPartyRegistration.AssertingPartyDetails.Builder> mapIdentityProvider(Saml2RelyingPartyProperties.Registration properties,
                                                                                                 boolean usingMetadata) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        Saml2RelyingPartyProperties.Identityprovider identityprovider = properties.getIdentityprovider();
        return (details) -> {
            map.from(identityprovider::getEntityId).to(details::entityId);
            map.from(identityprovider.getSinglesignon()::getBinding).whenNonNull()
                    .to(details::singleSignOnServiceBinding);
            map.from(identityprovider.getSinglesignon()::getUrl).to(details::singleSignOnServiceLocation);
            map.from(identityprovider.getSinglesignon()::isSignRequest).when((signRequest) -> !usingMetadata)
                    .to(details::wantAuthnRequestsSigned);
        };
    }

    private void validateSigningCredentials(Saml2RelyingPartyProperties.Registration properties, boolean signRequest) {
        if (signRequest) {
            Assert.state(!properties.getSigning().getCredentials().isEmpty(),
                    "Signing credentials must not be empty when authentication requests require signing.");
        }
    }

    private Saml2X509Credential asSigningCredential(Saml2RelyingPartyProperties.Registration.Signing.Credential properties) {
        RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKeyLocation());
        X509Certificate certificate = readCertificate(properties.getCertificateLocation());
        return new Saml2X509Credential(privateKey, certificate, Saml2X509Credential.Saml2X509CredentialType.SIGNING);
    }

    private Saml2X509Credential asDecryptionCredential(Saml2RelyingPartyProperties.Decryption.Credential properties) {
        RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKeyLocation());
        X509Certificate certificate = readCertificate(properties.getCertificateLocation());
        return new Saml2X509Credential(privateKey, certificate, Saml2X509Credential.Saml2X509CredentialType.DECRYPTION);
    }

    private Saml2X509Credential asVerificationCredential(Saml2RelyingPartyProperties.Identityprovider.Verification.Credential properties) {
        X509Certificate certificate = readCertificate(properties.getCertificateLocation());
        return new Saml2X509Credential(certificate, Saml2X509Credential.Saml2X509CredentialType.ENCRYPTION,
                Saml2X509Credential.Saml2X509CredentialType.VERIFICATION);
    }

    private RSAPrivateKey readPrivateKey(Resource location) {
        Assert.state(location != null, "No private key location specified");
        Assert.state(location.exists(), () -> "Private key location '" + location + "' does not exist");
        try (InputStream inputStream = location.getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(inputStream);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private X509Certificate readCertificate(Resource location) {
        Assert.state(location != null, "No certificate location specified");
        Assert.state(location.exists(), () -> "Certificate  location '" + location + "' does not exist");
        try (InputStream inputStream = location.getInputStream()) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
