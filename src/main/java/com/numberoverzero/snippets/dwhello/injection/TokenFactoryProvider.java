package com.numberoverzero.snippets.dwhello.injection;

import com.numberoverzero.snippets.dwhello.core.Token;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;

public class TokenFactoryProvider implements ValueFactoryProvider {

    private final TokenFactory tokenFactory;

    @Inject
    public TokenFactoryProvider(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    @Override
    public Factory<?> getValueFactory(Parameter parameter) {
        Class<?> paramType = parameter.getRawType();
        TokenParam annotation = parameter.getAnnotation(TokenParam.class);
        if (annotation != null && paramType.isAssignableFrom(Token.class)) {
            return tokenFactory;
        }
        return null;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }
}
