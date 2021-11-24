package com.ldv.samlproxy.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.InputStream;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/24/21
 */
public class RSAPrivateKeyValidator extends PKIValidator implements ConstraintValidator<RSAPrivateKeyConstraint, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAPrivateKeyValidator.class);

    @Override
    public void initialize(RSAPrivateKeyConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.hasText(s)) {
            try (InputStream is = asInputStream(s)) {
                RsaKeyConverters.pkcs8().convert(is);
            } catch (Exception e) {
                LOGGER.debug("Unable to convert string to a RSA PKCS8 private key:", e);

                return false;
            }
        }

        return true;
    }
}
