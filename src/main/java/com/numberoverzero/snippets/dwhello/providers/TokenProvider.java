package com.numberoverzero.snippets.dwhello.providers;

import com.numberoverzero.snippets.dwhello.core.Token;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class TokenProvider {
    @Context
    protected HttpHeaders headers;

    public Token getToken() {
        return new Token(headers.getHeaderString("date"));
    }

}
