package org.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotated with this annotation will be executed right after the
 * Ioc container has loaded all services and beans.
 * <p>
 * In order for it to work, you need to place it in the startup class and
 * the method should be void with zero params.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StartUp {

}
