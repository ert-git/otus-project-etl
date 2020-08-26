package ru.otus.etl.web.controllers;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/rest/auth")
@Slf4j
public class AuthController {

    @GetMapping
    public String get() throws IOException {
        return "ok";
    }

    @GetMapping(value = "/ping")
    public String ping() throws IOException {
        return "ok";
    }
}
