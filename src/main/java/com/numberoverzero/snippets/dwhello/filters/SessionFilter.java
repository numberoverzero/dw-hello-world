package com.numberoverzero.snippets.dwhello.filters;

import com.google.common.collect.Maps;
import com.numberoverzero.snippets.dwhello.core.Session;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class SessionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private final Map<String, Session> sessionStore;

    public SessionFilter() {
        this.sessionStore = Maps.newHashMap();
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Session session;
        Optional<String> sessionId = getSessionId(containerRequestContext);
        if (sessionId.isPresent()) {
            Optional<Session> existingSession = loadSession(sessionId.get());
            if(existingSession.isPresent()) {
                session = existingSession.get();
            } else {
                session = createSession();
            }
        } else {
            session = createSession();
        }
        containerRequestContext.getHeaders().putSingle("session", session.id);
        containerRequestContext.setProperty("session", session);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Session session = (Session) containerRequestContext.getProperty("session");
        containerResponseContext.getHeaders().putSingle("session", session.id);
    }

    private Optional<String> getSessionId(ContainerRequestContext ctx) {
        if (ctx.getHeaders().containsKey("session")) {
            return Optional.of(ctx.getHeaders().getFirst("session"));
        }
        return Optional.empty();
    }

    private Optional<Session> loadSession(String sessionId) {
        Session session = sessionStore.get(sessionId);
        if (session != null) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    private Session createSession() {
        Session session = Session.unique();
        sessionStore.put(session.id, session);
        return session;
    }
}
