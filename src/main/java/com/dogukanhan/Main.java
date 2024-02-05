package com.dogukanhan;

import com.dogukanhan.scanner.ControllerScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class Main {

    static HttpServer server;

    public static void main(String[] args) throws Exception {

        server = HttpServer.create(
                new InetSocketAddress(8080), // todo fix port from settings
                0
        );

        server.start();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectMapper om = new ObjectMapper();

        var routes = ControllerScanner.findControllers();
        routes.forEach(route -> {
            // todo add logger
            System.out.println(route);
            var directRoutes = route.getMethods()
                    .stream()
                    .filter(y -> y.getPath() == null || y.getPath().isEmpty())
                    .toList();

            route.getMethods()
                    .stream()
                    .filter(y -> y.getPath() != null && !y.getPath().isEmpty())
                    .forEach(innerRoute -> {
                        // todo Multiple contexes in same route may give error fix.
                        var context = server.createContext(route.getValue() + innerRoute.getPath());
                        context.setHandler(z -> {
                            if (z.getRequestMethod().equals(innerRoute.getHttpMethod())) {
                                try {
                                    var method = innerRoute.getMethod();
                                    var result = method.invoke(null);
                                    String json = ow.writeValueAsString(result);
                                    process(z, json.getBytes());
                                    // todo send methot not allowed;

                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                    // todo send json error
                                }
                            }
                            // todo send methot not allowed.
                        });
                    });

            if (directRoutes.size() > 0) {
                var context = server.createContext(route.getValue());
                context.setHandler(y -> {
                    // todo fix deperformance search each request.
                    var requestMethod = route.getMethods()
                            .stream()
                            .filter(x -> x.getHttpMethod().equals(y.getRequestMethod()))
                            .findFirst();
                    try {
                        if (requestMethod.isPresent()) {
                            var method = requestMethod.get();

                            if (method.getParameters().length >0) {
                                // todo support multiple parameters?
                                var firstParam = method.getParameters()[0];
                                String body = new String(y.getRequestBody().readAllBytes());
                                Object link = om.readValue(body, firstParam);
                                var result = method.getMethod().invoke(null, link);
                                String json = ow.writeValueAsString(result);
                                process(y, json.getBytes());
                            } else {
                                // todo duplicate code fix.
                                var result = method.getMethod().invoke(null);
                                String json = ow.writeValueAsString(result);
                                process(y, json.getBytes());
                            }
                        }
                        // todo send methot not allowed;

                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.err.println(e);
                        throw new RuntimeException(e);
                        // todo send json error
                    }
                });
            }
        });
    }

    public static void process(HttpExchange exchange, byte[] output) throws IOException {
        // todo fix headers.
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, output.length);
        var os = exchange.getResponseBody();
        os.write(output);
        os.close();
    }

    public static void setContext(String path, Method get, Method post) {
        var context = server.createContext(path);
        context.setHandler(y -> {
            try {
                String result = "failed";
                switch (y.getRequestMethod()) {
                    case "GET":
                        result = (String) get.invoke(null, y);
                        break;
                    case "PUT":
                        post.invoke(null, y);
                        break;
                    case "POST":
                        post.invoke(null, y);
                        break;
                }
                process(y, result.getBytes());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setSingleContext(String path, String type, Method method) {
        var context = server.createContext(path);
        var ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        context.setHandler(y -> {
            try {
                String result = "failed";
                if (y.getRequestMethod().equals(type)) {
                    var methodR = method.invoke(null, y);
                    result = ow.writeValueAsString(methodR);
                }
                process(y, result.getBytes());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

}