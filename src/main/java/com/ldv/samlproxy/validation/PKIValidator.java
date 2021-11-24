package com.ldv.samlproxy.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * .
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/24/21
 */
public abstract class PKIValidator {

    protected InputStream asInputStream(String value) {
        return new ByteArrayInputStream(value.replaceAll("^/|^\\\\", "").getBytes());
    }
}
