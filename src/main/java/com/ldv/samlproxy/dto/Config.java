package com.ldv.samlproxy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.logging.LogLevel;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/19/21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("custom.admin.username")
    private String customAdminUsername;

    @JsonProperty("custom.admin.password")
    private String customAdminPassword;

    @JsonProperty("custom.login-redirect-url")
    private String customLoginRedirectUrl;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.identityprovider.singlesignon.sign-request")
    private boolean idpSignRequests;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.identityprovider.metadata-uri")
    private String idpMetadataUri;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.identityprovider.entity-id")
    private String idpId;

    @JsonProperty("logging.level.root")
    private LogLevel loggingLevel;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.entity-id")
    private String spEntityId;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.signing.credentials[0].certificate-location")
    private String spSigningX509Certificate;

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.signing.credentials[0].private-key-location")
    private String spSigningPrivateKey;

    public String getCustomAdminUsername() {
        return customAdminUsername;
    }

    public void setCustomAdminUsername(String customAdminUsername) {
        this.customAdminUsername = customAdminUsername;
    }

    public String getCustomAdminPassword() {
        return customAdminPassword;
    }

    public void setCustomAdminPassword(String customAdminPassword) {
        this.customAdminPassword = customAdminPassword;
    }

    public String getCustomLoginRedirectUrl() {
        return customLoginRedirectUrl;
    }

    public void setCustomLoginRedirectUrl(String customLoginRedirectUrl) {
        this.customLoginRedirectUrl = customLoginRedirectUrl;
    }

    public boolean isIdpSignRequests() {
        return idpSignRequests;
    }

    public void setIdpSignRequests(boolean idpSignRequests) {
        this.idpSignRequests = idpSignRequests;
    }

    public String getIdpMetadataUri() {
        return idpMetadataUri;
    }

    public void setIdpMetadataUri(String idpMetadataUri) {
        this.idpMetadataUri = idpMetadataUri;
    }

    public String getIdpId() {
        return idpId;
    }

    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }

    public LogLevel getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(LogLevel loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    @JsonIgnore
    public boolean isLoggingDebug() {
        return LogLevel.DEBUG.equals(loggingLevel);
    }

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

    public String getSpSigningPrivateKey() {
        return spSigningPrivateKey;
    }

    public void setSpSigningPrivateKey(String spSigningPrivateKey) {
        this.spSigningPrivateKey = spSigningPrivateKey;
    }
}
