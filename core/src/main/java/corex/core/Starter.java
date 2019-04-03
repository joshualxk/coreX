package corex.core;

import corex.core.exception.CoreException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Who are we?
 * Just a speck of dusk within the galaxy.
 */
public class Starter {

    private static String configFile = "corex.conf";

    public static void start(String[] args) throws CoreException {
        for (int i = 0; i < args.length; ++i) {
            if ("-config".equals(args[i])) {
                if (++i >= args.length) {
                    throw new CoreException("命令行参数错误!");
                }

                configFile = args[i];
            }
        }

        Map<String, String> configs = readConfig(configFile);

    }

    public static Map<String, String> readConfig(String fileName) throws CoreException {
        File f = new File(fileName);
        if (!f.isFile()) {
            throw new CoreException("命令行参数错误!");
        }

        Map<String, String> configs = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            for (int lineN = 1; ; ++lineN) {
                String line = reader.readLine();
                String strs[] = line.split(":", 2);

                if (strs.length != 2) {
                    throw new CoreException("配置错误, 文件名:" + fileName + ", 行号:" + lineN);
                }

                configs.put(strs[0], strs[1]);

                if (reader.read() == -1) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new CoreException(e);
        }

        return configs;
    }
}
