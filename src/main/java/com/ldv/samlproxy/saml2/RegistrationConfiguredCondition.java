package com.ldv.samlproxy.saml2;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collections;
import java.util.Map;

/**
 * Duplicates {@link org.springframework.boot.autoconfigure.security.saml2}.RegistrationConfiguredCondition functionality
 * to be able to override {@link org.springframework.boot.autoconfigure.security.saml2}.Saml2RelyingPartyRegistrationConfiguration.
 *
 * @author Dmitry Losev ldv42@yandex.ru
 * @since 11/21/21
 */
public class RegistrationConfiguredCondition extends SpringBootCondition {

    private static final String PROPERTY = "spring.security.saml2.relyingparty.registration";

    private static final Bindable<Map<String, Saml2RelyingPartyProperties.Registration>> STRING_REGISTRATION_MAP = Bindable.mapOf(String.class,
            Saml2RelyingPartyProperties.Registration.class);

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("Relying Party Registration Condition");
        Map<String, Saml2RelyingPartyProperties.Registration> registrations = getRegistrations(context.getEnvironment());
        if (registrations.isEmpty()) {
            return ConditionOutcome.noMatch(message.didNotFind("any registrations").atAll());
        }
        return ConditionOutcome.match(message.found("registration", "registrations").items(registrations.keySet()));
    }

    private Map<String, Saml2RelyingPartyProperties.Registration> getRegistrations(Environment environment) {
        return Binder.get(environment).bind(PROPERTY, STRING_REGISTRATION_MAP).orElse(Collections.emptyMap());
    }
}
