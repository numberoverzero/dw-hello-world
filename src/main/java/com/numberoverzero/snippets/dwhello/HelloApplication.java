package com.numberoverzero.snippets.dwhello;

import com.google.common.collect.Maps;
import com.numberoverzero.snippets.dwhello.core.Person;
import com.numberoverzero.snippets.dwhello.health.ShallowHealthCheck;
import com.numberoverzero.snippets.dwhello.resources.PersonResource;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

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
    }
}
