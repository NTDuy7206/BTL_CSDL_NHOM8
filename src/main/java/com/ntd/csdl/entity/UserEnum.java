package com.ntd.csdl.entity;

public enum UserEnum {
    // Keep the first two positions stable because this enum is stored by ordinal.
    USER, ADMIN, OWNER_PENDING, TENANT, OWNER
}
