package io.bigoldbro.corex.impl;

import io.bigoldbro.corex.annotation.Value;
import io.bigoldbro.corex.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Created by Joshua on 2018/2/28.
 */
public class CoreXConfig {

    protected static final Logger logger = LoggerFactory.getLogger(CoreXConfig.class);

    public static final String DEF_CONF_FILE = System.getProperty("corex.config", "corex.conf");

    private final Properties prop;

    private CoreXConfig(Properties prop) {
        this.prop = prop;
    }

    public static CoreXConfig readConfig() throws CoreException {
        return readConfig(DEF_CONF_FILE);
    }

    public static CoreXConfig readConfig(String fileName) throws CoreException {
        Properties prop = new Properties();
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileName))) {
            prop.load(in);

            return new CoreXConfig(prop);

        } catch (IOException e) {
            throw new CoreException("读取配置文件失败：" + fileName);
        }
    }

    public void init(Object holder) {
        Value value;
        for (Class<?> clz = holder.getClass(); clz != null && clz != Object.class; clz = clz.getSuperclass()) {
            for (Field field : clz.getDeclaredFields()) {
                if ((value = field.getAnnotation(Value.class)) != null) {
                    setField(holder, field, value);
                }
            }
        }
    }

    private void setField(Object obj, Field field, Value value) {
        Class<?> clz = field.getType();
        String propName = value.value();
        Object val;
        if (clz == boolean.class || clz == Boolean.class) {
            val = getBoolean(propName);
        } else if (clz == short.class || clz == Short.class) {
            val = getShort(propName);
        } else if (clz == int.class || clz == Integer.class) {
            val = getInt(propName);
        } else if (clz == long.class || clz == Long.class) {
            val = getLong(propName);
        } else if (clz == String.class) {
            val = getString(propName);
        } else {
            throw new CoreException("不支持的属性类型:" + clz.getName());
        }
        try {
            field.setAccessible(true);
            field.set(obj, val);
        } catch (IllegalAccessException e) {
            throw new CoreException(e);
        }

        logger.info("set {}.{} -> {}", obj.getClass().getName(), field.getName(), val);
    }

    public Boolean getBoolean(String k) {
        String v = prop.getProperty(k);
        return Boolean.valueOf(v);
    }

    public Short getShort(String k) {
        String v = prop.getProperty(k);
        return Short.valueOf(v);
    }

    public Integer getInt(String k) {
        String v = prop.getProperty(k);
        return Integer.valueOf(v);
    }

    public Long getLong(String k) {
        String v = prop.getProperty(k);
        return Long.valueOf(v);
    }

    public String getString(String k) {
        String v = prop.getProperty(k);
        return v;
    }

    @Override
    public String toString() {
        return "CoreXConfig{" +
                "prop=" + prop +
                '}';
    }
}
