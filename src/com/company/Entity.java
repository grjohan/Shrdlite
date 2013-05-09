package com.company;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-06
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class Entity {
    private Shape shape;
    private Size size;
    private Colour colour;

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    public int getIndexInStack() {
        return indexInStack;
    }

    public void setIndexInStack(int indexInStack) {
        this.indexInStack = indexInStack;
    }

    int stack;
    int indexInStack;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public Shape getShape() {
        return shape;
    }

    public Size getSize() {
        return size;
    }

    public Colour getColour() {
        return colour;
    }

    public Entity(Shape shape, Colour colour, Size size, String name) {
        this.shape = shape;
        this.colour = colour;
        this.size = size;
        this.name = name;
    }
}
