package com.sensorweb.datacentergateway.entity;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {
    private int id;
    private String name;
    private String nameZh;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    @Override
    public String getAuthority() {
        return null;
    }
}
