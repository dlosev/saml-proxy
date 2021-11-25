package com.ldv.samlproxy.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.ByteArrayInputStream;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/25/21
 */
public class SamlMetadataValidator implements ConstraintValidator<SamlMetadataConstraint, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlMetadataValidator.class);

    @Override
    public void initialize(SamlMetadataConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            RelyingPartyRegistrations
                    .fromMetadata(new ByteArrayInputStream(value.getBytes()))
                    .build();
        } catch (RuntimeException e) {
            LOGGER.debug("Unable to convert string to a SAML IDP metadata:", e);

            return false;
        }

        return true;
    }
}
