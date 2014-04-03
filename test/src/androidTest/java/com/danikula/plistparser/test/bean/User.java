package com.danikula.plistparser.test.bean;

import java.util.Date;
import java.util.List;

import com.google.api.client.util.Key;

public class User {

    @Key("name")
    private String name;

    @Key("age")
    private int age;

    @Key("salary")
    private Integer salary;

    @Key("notExisted")
    private String notExisted;

    @Key("handsome")
    private boolean handsome;

    @Key("weight")
    private double weight;

    @Key("birthday")
    private Date birthday;

    @Key("key")
    private byte[] key;

    @Key("devices")
    private List<String> devices;

    @Key("favoriteNumbers")
    private List<Integer> favoriteNumbers;

    @Key("sister")
    private User sister;

    @Key("parents")
    private List<User> parent;

    public List<User> getParents() {
        return parent;
    }

    public User getSister() {
        return sister;
    }

    public List<Integer> getFavoriteNumbers() {
        return favoriteNumbers;
    }

    public List<String> getDevices() {
        return devices;
    }

    public byte[] getKey() {
        return key;
    }

    public Date getBirthday() {
        return birthday;
    }

    public boolean isHandsome() {
        return handsome;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getNotExisted() {
        return notExisted;
    }

    public Integer getSalary() {
        return salary;
    }
}
