package com.dogukanhan.endpoint;

import java.lang.reflect.Method;

public class RouteMethod {

    private Class<?>[] parameters;

    private String path;

    private String httpMethod;

    private Method method;

    public RouteMethod(String path, String httpMethod, Method method, Class<?>[] parameters) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.method = method;
        this.parameters = parameters;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }

    public void setParameters(Class<?>[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RouteMethod{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                '}';
    }
}
