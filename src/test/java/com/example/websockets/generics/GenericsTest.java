package com.example.websockets.generics;

public class GenericsTest {
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println(new Child1().type.getName());
        System.out.println(new Child2().type.getName());
    }

    private static class Base<T> {
        public final Class<T> type;

        public Base() throws ClassNotFoundException {
            type = (Class<T>) Class.forName(getClass().getGenericSuperclass().getTypeName().split("<")[1].replace(">", ""));
        }
    }

    private static class Child1 extends Base<Integer> {
        public Child1() throws ClassNotFoundException {
        }
    }

    private static class Child2 extends Base<String> {
        public Child2() throws ClassNotFoundException {
        }
    }
}
