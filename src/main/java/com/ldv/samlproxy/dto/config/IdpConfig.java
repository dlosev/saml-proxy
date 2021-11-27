package com.ldv.samlproxy.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ldv.samlproxy.validation.SamlMetadataConstraint;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/27/21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdpConfig {

    @JsonProperty("spring.security.saml2.relyingparty.registration.idp.identityprovider.metadata-uri")
    private String idpMetadataUri;

    @JsonIgnore
    @SamlMetadataConstraint
    private String idpMetadata;

    public String getIdpMetadataUri() {
        return idpMetadataUri;
    }

    public void setIdpMetadataUri(String idpMetadataUri) {
        this.idpMetadataUri = idpMetadataUri;
    }

    public String getIdpMetadata() {
        return idpMetadata;
    }

    public void setIdpMetadata(String idpMetadata) {
        this.idpMetadata = idpMetadata;
    }
}
