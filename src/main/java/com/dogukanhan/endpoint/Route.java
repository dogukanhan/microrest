package com.dogukanhan.endpoint;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private String value;

    private List<RouteMethod> methods;

    public Route(String value) {
        this.value = value;
        this.methods = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<RouteMethod> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return "Route{" +
                "value='" + value + '\'' +
                ", methods=" + methods +
                '}';
    }
}
