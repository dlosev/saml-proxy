package com.ldv.samlproxy.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/24/21
 */
@Constraint(validatedBy = X509CertificateValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface X509CertificateConstraint {

    String message() default "Invalid X509 certificate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
