package com.numberoverzero.snippets.dwhello.injection;

import com.google.common.collect.Maps;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Supplier;

public class AnyFactoryProvider implements ValueFactoryProvider {

    public final AbstractBinder bind;
    private final Class<? extends Annotation> annotationClass;
    private final Map<Class<?>, Factory<?>> factories;

    public <T extends Annotation> AnyFactoryProvider(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
        this.factories = Maps.newHashMap();
        this.bind = makeBinder();
    }

    public <T> void register(final Class<T> factoryClass, final Supplier<T> factoryFunction) {
        factories.put(factoryClass, makeFactory(factoryFunction));
    }

    @Override
    public Factory<?> getValueFactory(Parameter parameter) {
        if (!parameter.isAnnotationPresent(this.annotationClass)) {
            // Defer to other injectors
            return null;
        }

        final Class<?> expectedClass = parameter.getRawType();
        final Factory factory = factories.get(expectedClass);
        if (factory == null) {
            // Annotated for this injector but no factory exists
            throw new RuntimeException(String.format("No factory registered for class '%s'", expectedClass));
        }
        System.out.println("GET FACTORY");
        return factory;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }

    private <T> Factory makeFactory(Supplier<T> factoryFunction) {
        return new Factory() {
            @Override
            public Object provide() {
                return factoryFunction.get();
            }

            @Override
            public void dispose(Object o) {/* not used */}
        };
    }

    private AbstractBinder makeBinder() {
        final AnyFactoryProvider self = this;
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(self).to(ValueFactoryProvider.class);
                bind(InjectionResolver.class).to(self.annotationClass);
            }
        };
    }

}
