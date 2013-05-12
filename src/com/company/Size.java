package com.company;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-06
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public enum Size {
    tall(1),
    small(1),
    large(3),
    wide(3),
    medium(2),
    floor(4);
    private int value;

    private Size(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
