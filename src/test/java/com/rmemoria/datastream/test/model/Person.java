package com.rmemoria.datastream.test.model;

import java.util.Date;

/**
 * Created by rmemoria on 9/10/15.
 */
public class Person {
    private String name;

    private Address address;

    private Date birthDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
