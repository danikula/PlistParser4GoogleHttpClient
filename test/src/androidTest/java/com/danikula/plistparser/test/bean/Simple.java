package com.danikula.plistparser.test.bean;

import com.google.api.client.util.Key;

public class Simple {

    @Key("value")
    private String value;

    public String getValue() {
        return value;
    }
}
