package com.company;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-07
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public class Command
{
    private Movement movement;
    private Entity block;
    private int location;
    private Direction direction;
    private Entity relationBlock;

    public Entity getRelationBlock() {
        return relationBlock;
    }

    public void setRelationBlock(Entity relationBlock) {
        this.relationBlock = relationBlock;
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

    public Entity getBlock() {
        return block;
    }

    public void setBlock(Entity block) {
        this.block = block;
    }


    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }



    public Command()
    {
    }
}

enum Movement
{
   doNothing,
   move,
   pick,
   drop;
}

enum Direction
{
    right,
    left,
    here,
    under;
}

