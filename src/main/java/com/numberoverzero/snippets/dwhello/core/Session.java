package com.numberoverzero.snippets.dwhello.core;

import java.util.UUID;

public class Session {

    public final String id;
    public int calls;

    public Session(String id) {
        this.id = id;
    }

    public static Session unique() {
        return new Session(UUID.randomUUID().toString());
    }
}
