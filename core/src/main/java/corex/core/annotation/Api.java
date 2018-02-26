package corex.core.annotation;

import corex.core.define.ConstDefine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Joshua on 2018/3/8.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Api {

    String value();

    int type() default ConstDefine.AUTH_TYPE_CLIENT;
}
