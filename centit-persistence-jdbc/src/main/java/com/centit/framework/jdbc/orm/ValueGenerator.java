package com.centit.framework.jdbc.orm;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by codefan on 17-8-29.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValueGenerator {
    GeneratorType strategy() default GeneratorType.AUTO;
    GeneratorTime occasion() default GeneratorTime.NEW;
    GeneratorCondition condition() default GeneratorCondition.IFNULL;
    String value() default "";
}
