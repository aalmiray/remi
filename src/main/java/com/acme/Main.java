package com.acme;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Modules!");
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("key", "value");
        System.out.println(new ObjectMapper().writeValueAsString(map));
    }
}