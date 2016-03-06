package com.numberoverzero.snippets.dwhello;

import com.google.common.collect.Maps;
import com.numberoverzero.snippets.dwhello.core.Person;
import com.numberoverzero.snippets.dwhello.health.ShallowHealthCheck;
import com.numberoverzero.snippets.dwhello.injection.TokenFactory;
import com.numberoverzero.snippets.dwhello.injection.TokenFactoryProvider;
import com.numberoverzero.snippets.dwhello.injection.TokenParam;
import com.numberoverzero.snippets.dwhello.injection.TokenParamInjectionResolver;
import com.numberoverzero.snippets.dwhello.resources.PersonResource;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Singleton;
import java.util.Map;

public class HelloApplication extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new HelloApplication().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        Map<String, Person> personStore = Maps.newHashMap();
        environment.jersey().register(new PersonResource(personStore::get, personStore::put));

        environment.healthChecks().register("shallow", new ShallowHealthCheck());

        environment.jersey().register(new AbstractBinder() {

            @Override
            protected void configure() {
                bind(TokenFactory.class).to(TokenFactory.class).in(Singleton.class);
                bind(TokenFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(TokenParamInjectionResolver.class)
                        .to(new TypeLiteral<InjectionResolver<TokenParam>>(){})
                        .in(Singleton.class);
            }
        });
    }

}
