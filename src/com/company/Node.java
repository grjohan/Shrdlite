package com.company;

import java.util.Dictionary;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-12
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class Node implements Comparable<Node>{
    private Dictionary<Node, String> neighbors;

    public int compareTo(Node o1)
    {
        return o1.getValue() - this.getValue();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Dictionary<Node, String> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Dictionary<Node, String> neighbors) {
        this.neighbors = neighbors;
    }

    private String state;

    public Node getFromNode() {
        return fromNode;
    }

    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }

    private Node fromNode;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    int value;

    public boolean isHolding() {
        return holding;
    }

    public void setHolding(boolean holding) {
        this.holding = holding;
    }

    boolean holding;
    public Node(){}

}
