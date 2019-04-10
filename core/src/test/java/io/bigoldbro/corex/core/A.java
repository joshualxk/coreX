package io.bigoldbro.corex.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joshua on 2019-04-11.
 */
public class A {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        List list = new ArrayList();
        list.add(1);
        list.add("234");
        A.class.getDeclaredMethods()[1].invoke(null, new Object[]{list});

    }


    public static void aa(List<String> list) {
        System.out.println(list.get(0).length());
        System.out.println(list);
    }
}
