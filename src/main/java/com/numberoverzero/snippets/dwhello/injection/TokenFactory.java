package com.numberoverzero.snippets.dwhello.injection;

import com.numberoverzero.snippets.dwhello.core.Token;
import org.glassfish.hk2.api.Factory;

import java.util.UUID;

public class TokenFactory implements Factory<Token> {

    public TokenFactory() {}

    @Override
    public Token provide() {
        return new Token("token-" + UUID.randomUUID().toString());
    }

    @Override
    public void dispose(Token token) {
        // not used
    }
}
