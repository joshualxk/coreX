package io.bigoldbro.corex.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Joshua on 2018/3/8.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {

    String address();

    String version() default "1.0";

    BlockControl bc() default BlockControl.NON_BLOCK;

}
