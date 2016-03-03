package com.numberoverzero.snippets.dwhello.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {
    @JsonProperty
    public final String name;

    @JsonProperty
    public final int age;

    public Person(@JsonProperty("name") String name, @JsonProperty("age") int age) {
        this.name = name;
        this.age = age;
    }

}
