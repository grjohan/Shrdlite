package com.company;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-07
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public class Command {
    private Movement movement;

    public String getBlockDeterminer() {
        return blockDeterminer;
    }

    public void setBlockDeterminer(String blockDeterminer) {
        this.blockDeterminer = blockDeterminer;
    }

    public String getRelationBlocksDeterminer() {
        return relationBlocksDeterminer;
    }

    public void setRelationBlocksDeterminer(String relationBlocksDeterminer) {
        this.relationBlocksDeterminer = relationBlocksDeterminer;
    }

    private String blockDeterminer;
    private ArrayList<Entity> blocks;
    private int location;
    private Direction direction;
    // TODO change to list of relation blocks
    private String relationBlocksDeterminer;
    private ArrayList<Entity> relationBlocks;

    public ArrayList<Entity> getRelationBlocks() {
        return relationBlocks;
    }

    public void addRelationBlock(Entity relationBlock) {
        this.relationBlocks.add(relationBlock);
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public ArrayList<Entity> getBlocks() {
        return blocks;
    }

    public void addBlock(Entity block) {
        this.blocks.add(block);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setBlocks(ArrayList<Entity> blocks)
    {
        this.blocks = blocks;
    }

    public void setRelationBlocks (ArrayList<Entity> relationBlocks)
    {
        this.relationBlocks = relationBlocks;
    }

    public Command() {
        blocks = new ArrayList<Entity>();
        relationBlocks = new ArrayList<Entity>();
    }
}

enum Movement {
    move,
    pick,
    drop;
}

enum Direction {
    right,
    left,
    here,
    under;
}

