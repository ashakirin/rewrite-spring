package org.gradle.rewrite.spring

import org.junit.jupiter.api.Test
import org.openrewrite.Parser

class NoRequestMappingAnnotationTest : Parser(dependenciesFromClasspath("spring-web")) {
    @Test
    fun requestMapping() {
        val controller = parse("""
            import java.util.*;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.*;
            import static org.springframework.web.bind.annotation.RequestMethod.GET;
            import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
            
            @RestController
            @RequestMapping("/users")
            public class UsersController {
                @RequestMapping(method = HEAD)
                public ResponseEntity<List<String>> getUsersHead() {
                    return null;
                }
            
                @RequestMapping(method = GET)
                public ResponseEntity<List<String>> getUsers() {
                    return null;
                }

                @RequestMapping(path = "/{id}", method = RequestMethod.GET)
                public ResponseEntity<String> getUser(@PathVariable("id") Long id) {
                    return null;
                }
                
                @RequestMapping
                public ResponseEntity<List<String>> getUsersNoRequestMethod() {
                    return null;
                }
            }
        """.trimIndent())

        val fixed = controller.refactor().visit(NoRequestMappingAnnotation()).fix().fixed

        assertRefactored(fixed, """
            import java.util.*;
            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.*;
            import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
            
            @RestController
            @RequestMapping("/users")
            public class UsersController {
                @RequestMapping(method = HEAD)
                public ResponseEntity<List<String>> getUsersHead() {
                    return null;
                }
            
                @GetMapping
                public ResponseEntity<List<String>> getUsers() {
                    return null;
                }

                @GetMapping("/{id}")
                public ResponseEntity<String> getUser(@PathVariable("id") Long id) {
                    return null;
                }
                
                @GetMapping
                public ResponseEntity<List<String>> getUsersNoRequestMethod() {
                    return null;
                }
            }
        """)
    }
}