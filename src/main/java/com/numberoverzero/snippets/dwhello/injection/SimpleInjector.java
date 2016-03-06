package com.numberoverzero.snippets.dwhello.injection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
 * environment.jersey().register(injector);
 *
 * // Default injector for instances of User
 * injector.register(User.class, () -> new User("DefaultInject"));
 *
 * // Specific injector for @CustomInject User
 * injector.register(User.class, () -> new User("CustomInject"), CustomInject.class);
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
     * Negate a predicate cleanly.  Why isn't this part of the stdlib. :(
     */
    private static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    /**
     * Register a Supplier (factory function) for a given class.  If any annotations are provided, the factory function
     * will only be used to create instances of the class when the annotation matches.  If no annotations are provided,
     * the function will be used when there are no annotation-specific functions available.
     *
     * @param factoryClass      The class to create instances of
     * @param factoryFunction   Function that returns an instance of the class for injection
     * @param annotationClasses Any annotations to associate this factory function with.
     */
    @SafeVarargs
    public final void register(
            Class<?> factoryClass, Supplier<?> factoryFunction, Class<? extends Annotation>... annotationClasses) {
        if (annotationClasses.length == 0) {
            insertFactory(anonymousFactories, factoryClass, factoryFunction);
        } else {
            verifyAnnotationsAllowed(annotationClasses);
            Arrays.stream(annotationClasses)
                    .forEach(annotation ->
                            insertFactory(annotatedFactories.get(annotation), factoryClass, factoryFunction));
        }
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
                throw new RuntimeException(String.format(
                        "SimpleInjector expected factory for class '%s' but none provided", expectedClass));
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
    }

    /**
     * Insert into the mapping an anonymous {@link Factory} that wraps the factoryFunction
     */
    protected void insertFactory(
            Map<Class<?>, Factory<?>> mapping, Class<?> factoryClass, Supplier<?> factoryFunction) {
        mapping.put(factoryClass, new Factory() {
            @Override
            public Object provide() {
                return factoryFunction.get();
            }

            @Override
            public void dispose(Object o) {/* not used */}
        });
    }

    /**
     * Throws RuntimeException if any of the annotations are not available for injection
     */
    protected void verifyAnnotationsAllowed(Class<? extends Annotation>[] annotationClasses) {
        Set<Class<? extends Annotation>> allowedAnnotations = ImmutableSet.copyOf(this.annotationClasses);
        Set<Class<? extends Annotation>> disallowedAnnotations = Arrays.stream(annotationClasses)
                .filter(not(allowedAnnotations::contains)).collect(Collectors.toSet());
        if (!disallowedAnnotations.isEmpty()) {
            throw new RuntimeException(
                    "Cannot configure injection for unmapped annotations: " + disallowedAnnotations.toString());
        }
    }
}
