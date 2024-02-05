package com.dogukanhan.example;

import com.dogukanhan.annotations.Controller;
import com.dogukanhan.annotations.GetMapping;
import com.dogukanhan.annotations.PostMapping;

@Controller("/api")
public class DeltaController {
    @GetMapping()
    public static UserResponse handleRequest() {
        return new UserResponse("dogukan", 25);
    }

    @PostMapping()
    public static UserResponse handleRequestTwo(UserResponse userResponse) {
        return userResponse;
    }

    @GetMapping("/test1")
    public static UserResponse handle2() {
        return new UserResponse("dogukan", 25);
    }
}
