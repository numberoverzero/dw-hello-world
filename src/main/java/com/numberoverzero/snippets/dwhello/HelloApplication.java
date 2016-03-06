package com.numberoverzero.snippets.dwhello;

import com.google.common.collect.Maps;
import com.numberoverzero.snippets.dwhello.annotations.OtherParam;
import com.numberoverzero.snippets.dwhello.annotations.ProviderParam;
import com.numberoverzero.snippets.dwhello.annotations.TokenParam;
import com.numberoverzero.snippets.dwhello.core.*;
import com.numberoverzero.snippets.dwhello.health.ShallowHealthCheck;
import com.numberoverzero.snippets.dwhello.providers.TokenProvider;
import com.numberoverzero.snippets.dwhello.resources.PersonResource;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

import java.util.Map;
import java.util.UUID;

public class HelloApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new HelloApplication().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        Map<String, Person> personStore = Maps.newHashMap();
        environment.jersey().register(new PersonResource(personStore::get, personStore::put));

        environment.healthChecks().register("shallow", new ShallowHealthCheck());

        SimpleInjector injector = new SimpleInjector(TokenParam.class, OtherParam.class, ProviderParam.class);
        environment.jersey().register(injector);

        injector.register(Token.class, () -> new Token("anon-" + UUID.randomUUID().toString()));
        injector.register(Token.class, () -> new Token("token-" + UUID.randomUUID().toString()), TokenParam.class);
        injector.register(Token.class, () -> new Token("other-" + UUID.randomUUID().toString()), OtherParam.class);

        TokenProvider provider = new TokenProvider();
        environment.jersey().register(provider);
        injector.register(Token.class, provider::getToken, ProviderParam.class);
    }

}
