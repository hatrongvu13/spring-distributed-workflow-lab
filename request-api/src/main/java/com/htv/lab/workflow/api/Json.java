package com.htv.lab.workflow.api;

import tools.jackson.databind.ObjectMapper;

public final class Json {
    private static final ObjectMapper M = new ObjectMapper();

    private Json() {
    }

    public static String write(Object o) {
        return M.writeValueAsString(o);
    }

    public static <T> T read(String s, Class<T> t) {
        return M.readValue(s, t);
    }
}