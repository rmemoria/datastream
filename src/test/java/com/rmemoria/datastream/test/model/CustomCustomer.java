package com.rmemoria.datastream.test.model;

import java.util.Date;

/**
 * Created by rmemoria on 10/11/15.
 */
public class CustomCustomer extends Customer {
    private int age;
    private Date birthDate;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
