package com.dogukanhan.scanner;

import com.dogukanhan.annotations.Controller;
import com.dogukanhan.annotations.GetMapping;
import com.dogukanhan.annotations.PostMapping;
import com.dogukanhan.endpoint.Route;
import com.dogukanhan.endpoint.RouteMethod;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ControllerScanner {

    private static List<Route> routes = new ArrayList<>();

    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }


    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                doLogic(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private static void doLogic(Class clazz) {
        if (clazz.getAnnotation(Controller.class) != null) {
            doController(clazz);
        }
    }

    private static void doController(Class<?> clazz) {
        var a = clazz.getAnnotation(Controller.class);

        var methods = clazz.getMethods();
        var route = new Route(a.value());
        routes.add(route);

        for (Method method : methods) {
            var annotations = method.getAnnotations();
            for (var anno : annotations) {
                setHttpMapping(anno, route, method);
            }
        }
    }

    public static List<Route> findControllers() throws Exception {
        String packageName = "com.dogukanhan";
        getClasses(packageName);
        return routes;
    }

    private static void setHttpMapping(Annotation anno, Route route, Method method) {
        String methodName = null;
        String pathValue = null;

        if (GetMapping.class.equals(anno.annotationType())) {
            pathValue = ((GetMapping) anno).value();
            methodName = "GET";
        }
        if (PostMapping.class.equals(anno.annotationType())) {
            pathValue = ((PostMapping) anno).value();
            methodName = "POST";
        }

        if (methodName != null) {
            route.getMethods().add(new RouteMethod(
                    pathValue, methodName, method, method.getParameterTypes()
            ));
        }
    }
}
