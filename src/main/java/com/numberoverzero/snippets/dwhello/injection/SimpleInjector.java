package com.numberoverzero.snippets.dwhello.injection;

import com.google.common.collect.Maps;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Greatly reduce the complexity of injecting parameters into methods.
 * <p>
 * <pre>{@code
 * // Thing to inject
 * public class User {
 *     public final String name;
 *     public User(String name) { this.name = name; }
 * }
 *
 * // An annotation to hook the injector
 * public @interface CustomInject {}
 *
 * // Usage in the resource
 * public class MyResource {
 *     public String sayHello(@CustomInject User user) {
 *         return String.format("Hello, %s!", user.name);
 *     }
 * }
 *
 * // Wherever you set up your environment
 * SimpleInjector injector = new SimpleInjector(CustomInject.class);
 * injector.register(User.class, () -> new User("Injected!"));
 * environment.jersey().register(injector);
 * }</pre>
 */
public class SimpleInjector extends AbstractBinder implements ValueFactoryProvider {

    protected final Class<? extends Annotation>[] annotationClasses;
    protected final Map<Class<?>, Factory<?>> anonymousFactories;
    protected final Map<Class<? extends Annotation>, Map<Class<?>, Factory<?>>> annotatedFactories;

    /**
     * Injects registered factories for the given annotations
     *
     * @param annotationClasses Any number of annotations to provide factories for
     */
    @SafeVarargs
    public SimpleInjector(Class<? extends Annotation>... annotationClasses) {
        this.annotationClasses = annotationClasses;
        this.anonymousFactories = Maps.newHashMap();
        this.annotatedFactories = Maps.newHashMap();
        Arrays.stream(annotationClasses)
                .forEach((annotation) -> this.annotatedFactories.put(annotation, Maps.newHashMap()));
    }

    /**
     * Only use the given factory when both annotation and class match.
     *
     * @param annotation      The annotation to match
     * @param factoryClass    The class to create instances of
     * @param factoryFunction Returns an instance of the class for injection when annotation matches
     */
    public void register(Class<? extends Annotation> annotation, Class<?> factoryClass, Supplier<?> factoryFunction) {
        insertFactory(annotatedFactories.get(annotation), factoryClass, factoryFunction);
    }

    /**
     * Only use the given factory when there is no annotation-specific factory for the given class.
     *
     * @param factoryClass    The class to create an instance of
     * @param factoryFunction Returns an instance of the class for injection
     */
    public void register(Class<?> factoryClass, Supplier<?> factoryFunction) {
        insertFactory(anonymousFactories, factoryClass, factoryFunction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Factory<?> getValueFactory(Parameter parameter) {
        Optional<Class<? extends Annotation>> annotation = Arrays
                .stream(annotationClasses)
                .filter(parameter::isAnnotationPresent)
                .findFirst();
        if (!annotation.isPresent()) {
            // No matching annotations - defer to other injectors
            return null;
        }

        // First look for an annotation-specific factory, then fall back to anonymous factories.
        final Class<?> expectedClass = parameter.getRawType();
        Factory<?> factory = annotatedFactories.get(annotation.get()).get(expectedClass);
        if (factory == null) {
            factory = anonymousFactories.get(expectedClass);
            if (factory == null) {
                // Annotated for this injector, but no factory exists
                throw new RuntimeException(String.format("No factory registered for class '%s'", expectedClass));
            }
        }
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind((ValueFactoryProvider) this).to(ValueFactoryProvider.class);
        Arrays.stream(this.annotationClasses).forEach(bind(InjectionResolver.class)::to);
    }

    /**
     * Insert into the mapping an anonymous {@link Factory} that wraps the factoryFunction
     */
    protected <T> void insertFactory(
            Map<Class<?>, Factory<?>> mapping, Class<?> factoryClass, Supplier<T> factoryFunction) {
        mapping.put(factoryClass, new Factory() {
            @Override
            public Object provide() {
                return factoryFunction.get();
            }

            @Override
            public void dispose(Object o) {/* not used */}
        });
    }
}
