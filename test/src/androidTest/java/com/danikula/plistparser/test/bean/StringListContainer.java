package com.danikula.plistparser.test.bean;

import java.io.Serializable;
import java.util.List;

import com.danikula.plistparser.ListContainer;
import com.google.api.client.util.Key;

public class StringListContainer implements Serializable, ListContainer<String> {

    @Key
    private List<String> list;

    public List<String> getList() {
        return list;
    }
}
