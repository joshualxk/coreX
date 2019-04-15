package io.bigoldbro.corex.core;

import io.bigoldbro.corex.annotation.Value;
import io.bigoldbro.corex.impl.CoreXConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Joshua on 2019/04/17.
 */
public class CoreXConfigTest {

    static class TestConfig {
        @Value("test.shortVal1")
        short shortVal1;

        @Value("test.shortVal2")
        short shortVal2;

        @Value("test.intVal1")
        int intVal1;

        @Value("test.intVal2")
        Integer intVal2;

        @Value("test.strVal1")
        String strVal1;

        @Value("test.longVal1")
        long longVal1;

        @Value("test.longVal2")
        long longVal2;

        @Value("test.boolVal1")
        boolean boolVal1;

        @Value("test.boolVal2")
        Boolean boolVal2;

        @Override
        public String toString() {
            return "TestConfig{" +
                    "shortVal1=" + shortVal1 +
                    ", shortVal2=" + shortVal2 +
                    ", intVal1=" + intVal1 +
                    ", intVal2=" + intVal2 +
                    ", strVal1='" + strVal1 + '\'' +
                    ", longVal1=" + longVal1 +
                    ", longVal2=" + longVal2 +
                    ", boolVal1=" + boolVal1 +
                    ", boolVal2=" + boolVal2 +
                    '}';
        }
    }

    @Test
    public void testEqual() {
        String path = "src/test/resources/testcorex.conf";
        CoreXConfig coreXConfig = CoreXConfig.readConfig(path);

        TestConfig testConfig = new TestConfig();
        coreXConfig.init(testConfig);

//        System.out.println(testConfig);

        Assert.assertEquals(testConfig.toString(), "TestConfig{shortVal1=-32768, shortVal2=32767, intVal1=-2147483648, intVal2=2147483647, strVal1='\"hahaha\"', longVal1=-9223372036854775808, longVal2=9223372036854775807, boolVal1=true, boolVal2=false}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExist() {
        class TestConfig {
            @Value("test.nonexist")
            int nonexist;
        }
        String path = "src/test/resources/testcorex.conf";
        CoreXConfig coreXConfig = CoreXConfig.readConfig(path);

        TestConfig testConfig = new TestConfig();
        coreXConfig.init(testConfig);
    }
}
