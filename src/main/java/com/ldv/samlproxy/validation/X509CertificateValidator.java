package com.ldv.samlproxy.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.InputStream;
import java.security.cert.CertificateFactory;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/24/21
 */
public class X509CertificateValidator extends PKIValidator implements ConstraintValidator<X509CertificateConstraint, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(X509CertificateValidator.class);

    @Override
    public void initialize(X509CertificateConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.hasText(s)) {
            try (InputStream is = asInputStream(s)) {
                CertificateFactory.getInstance("X.509").generateCertificate(is);
            } catch (Exception e) {
                LOGGER.debug("Unable to convert string to a X509 certificate:", e);

                return false;
            }
        }

        return true;
    }
}
