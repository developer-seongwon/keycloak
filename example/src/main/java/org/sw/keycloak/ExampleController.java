package org.sw.keycloak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExampleController {
    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);



    @RequestMapping("/login/oauth2/code/{id}")
    public ResponseEntity<String> a(@PathVariable String id) {
        System.out.println(id);
        return ResponseEntity.ok("{}");
    }


    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OAuth2User user) {
        return "Hello " + user.getAttribute("preferred_username");
    }

    @GetMapping("/")
    public String index() {
        return "<a href='/oauth2/authorization/heviton'>Login with Keycloak</a>";
    }

}