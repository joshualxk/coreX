package io.bigoldbro.corex;

import io.bigoldbro.corex.exception.CoreException;
import io.bigoldbro.corex.utils.ConfigUtil;

import java.util.Map;

/**
 * Who are we?
 * Just a speck of dusk within the galaxy.
 */
public class Starter {

    public static void start(String[] args) throws CoreException {

        String configFile = ConfigUtil.DEF_CONF_FILE;
        for (int i = 0; i < args.length; ++i) {
            if ("-config".equals(args[i])) {
                if (++i >= args.length) {
                    throw new CoreException("命令行参数错误!");
                }

                configFile = args[i];
            }
        }

        Map<String, String> configs = ConfigUtil.readConfig(configFile);
        System.out.println(configs);

    }

    public static void main(String[] args) {
        start(args);
    }

}
