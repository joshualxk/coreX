package io.bigoldbro.corex.utils;

import io.bigoldbro.corex.exception.CoreException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Joshua on 2019/4/7
 */
public final class ConfigUtil {

    public static final String DEF_CONF_FILE = System.getProperty("io.bigoldbro.corex.config", "io.bigoldbro.corex.conf");

    public static Map<String, String> readConfig(String fileName) throws CoreException {
        Map<String, String> configs;

        Properties prop = new Properties();
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileName))) {
            prop.load(in);

            Set<String> keys = prop.stringPropertyNames();
            configs = new HashMap<>(keys.size());

            for (String k : keys) {
                String v = prop.getProperty(k);
                configs.put(k, v);
            }

        } catch (IOException e) {
            throw new CoreException("读取配置文件失败：" + fileName);
        }

        return Collections.unmodifiableMap(configs);
    }

    public static void main(String[] args) {
        System.out.println(readConfig(DEF_CONF_FILE));
    }
}
