package com.jibi.common;

import org.apache.commons.lang3.EnumUtils;

public enum Algorithm {

    MD2("MD2", 32), MD5("MD5", 32), SHA("SHA", 40), SHA224("SHA-224", 56),
    SHA256("SHA-256", 64), SHA384("SHA-384", 96), SHA512("SHA-512", 128);

    private String value;
    private int length;

    public String getValue() {
        return this.value;
    }

    public int getLength() {
        return this.length;
    }

    private Algorithm(String value, int length) {
        this.value = value;
        this.length = length;
    }

    public static boolean isValidAlgo(String algoSelected) {
        return EnumUtils.isValidEnum(Algorithm.class, algoSelected);
    }

    public static Algorithm getAlgo(String algoSelected) {
        return EnumUtils.getEnum(Algorithm.class, algoSelected);
    }
}
