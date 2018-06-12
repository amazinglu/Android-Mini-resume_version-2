package com.example.amazinglu.recycler_view_function_demo.model;

import java.util.UUID;

public class Element {
    public String id;
    public String text;

    public Element() {
        id = UUID.randomUUID().toString();
    }
}
