package com.ldv.samlproxy.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ldv.samlproxy.validation.RSAPrivateKeyConstraint;
import com.ldv.samlproxy.validation.X509CertificateConstraint;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/27/21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpConfig {

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.entity-id")
    private String spEntityId;

    @JsonIgnore
    @X509CertificateConstraint
    private String spSigningX509Certificate;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.signing.credentials[0].certificate-location")
    private String spSigningX509CertificateLocation;

    @JsonIgnore
    @RSAPrivateKeyConstraint
    private String spSigningPrivateKey;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.signing.credentials[0].private-key-location")
    private String spSigningPrivateKeyLocation;

    public String getSpEntityId() {
        return spEntityId;
    }

    public void setSpEntityId(String spEntityId) {
        this.spEntityId = spEntityId;
    }

    public String getSpSigningX509Certificate() {
        return spSigningX509Certificate;
    }

    public void setSpSigningX509Certificate(String spSigningX509Certificate) {
        this.spSigningX509Certificate = spSigningX509Certificate;
    }

    public String getSpSigningX509CertificateLocation() {
        return spSigningX509CertificateLocation;
    }

    public void setSpSigningX509CertificateLocation(String spSigningX509CertificateLocation) {
        this.spSigningX509CertificateLocation = spSigningX509CertificateLocation;
    }

    public String getSpSigningPrivateKey() {
        return spSigningPrivateKey;
    }

    public void setSpSigningPrivateKey(String spSigningPrivateKey) {
        this.spSigningPrivateKey = spSigningPrivateKey;
    }

    public String getSpSigningPrivateKeyLocation() {
        return spSigningPrivateKeyLocation;
    }

    public void setSpSigningPrivateKeyLocation(String spSigningPrivateKeyLocation) {
        this.spSigningPrivateKeyLocation = spSigningPrivateKeyLocation;
    }
}
