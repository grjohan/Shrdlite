package com.company;

import java.util.*;

public class Planner {
    String world;
    Direction direction;
    int location;
    Entity relationBlock = null;
    HashMap<String, Entity> entities;
    Hashtable<String,Node> nodes;
    Command command = new Command();
    Entity[] blockToRemove;

    public Planner(String world, Command command,HashMap<String, Entity> entities) {
        this.world = world;
        this.command = command;
        this.entities = entities;
        nodes = new Hashtable<String, Node>();
        // Print the data
        // if we are holding something, set that as currently_holding
    }

    private void ConstructGoalWorld(Node start) {
        if(start.isHolding())
            blockToRemove = new Entity[1];
        else
            blockToRemove = new Entity[command.getBlocks().size()];
        String[] oldWorld = world.split(";");
        String[] newWorld = new String[oldWorld.length];
        world = "";
        direction = command.getDirection();
        location = command.getLocation();
        if (command.getMovement() != Movement.pick)
            relationBlock = command.getRelationBlocks().get(0);

        if(start.isHolding())
            blockToRemove[0] = entities.get(start.getHoldingBlock());
        else
        {
            for (int i = 0; i < command.getBlocks().size(); i++) {
                blockToRemove[i] = command.getBlocks().get(i);
            }
        }
        for (int i = 0; i < oldWorld.length; i++) {
            newWorld[i] = oldWorld[i];
            for (int j = 0; j < blockToRemove.length; j++) {
                String block = blockToRemove[j].getName();
                newWorld[i] = newWorld[i].replaceAll(block, "");
            }
        }
        if(direction == Direction.under)
        {
            // just put as many don't care terms under it as we are moving there
            String under,objects,top;
            under = newWorld[location].substring(0,newWorld[location].indexOf(command.getRelationBlocks().get(0).getName()));
            objects = "";
            for(Entity block : command.getBlocks())
            {
                objects += "*";
            }
            top = newWorld[location].substring(newWorld[location].indexOf(command.getRelationBlocks().get(0).getName()));
            newWorld[location] = under + objects + top;
        }
        if(direction == Direction.here)
        {
            Entity smallestStackingObject = command.getBlocks().get(0);
            boolean unstackable = false;
            for(Entity block : command.getBlocks())
            {
               if (block.getShape() == Shape.pyramid || block.getShape() == Shape.ball)
                   unstackable = true;
                if (block.getSize().getValue() < smallestStackingObject.getSize().getValue())
                    smallestStackingObject = block;
            }
            // check if there is a block on it
            if (newWorld[location].length() > command.getRelationBlocks().get(0).getIndexInStack() +1)
            {
                Entity blockToPutOver = entities.get(String.valueOf(newWorld[location].charAt(command.getRelationBlocks().get(0).getIndexInStack() +1)));
                if((smallestStackingObject.getSize().getValue() < blockToPutOver.getSize().getValue()) || unstackable)
                {
                    newWorld[location] = newWorld[location].substring(0,newWorld[location].indexOf(command.getRelationBlocks().get(0).getName()) + 1);
                }
                // if it works to put it back, we have to add it there in the goal world but with don't care term
                else
                {
                    String under,objects,top;
                    under = newWorld[location].substring(0,newWorld[location].indexOf(command.getRelationBlocks().get(0).getName()) + 1);
                    objects = "";
                    for(Entity block : command.getBlocks())
                    {
                        objects += "*";
                    }
                    top = newWorld[location].substring(newWorld[location].indexOf(command.getRelationBlocks().get(0).getName()) + 1);
                    newWorld[location] = under + objects + top;
                }
            }
        }
        for (String temp : newWorld)
        {
            world += temp + ";";
        }
        world = world.substring(0, world.length() - 1);

        // Check if this world is actually possible, only impossible thing would be putting to many unstackables left or right of something
        int numberOfStacks = 1,numberOfUnstackables = 0;
        if(direction == Direction.right)
        {
            numberOfStacks = 9 - location;
            for (int i = 9; i > location; i--)
            {
                Entity topBlock = entities.get(newWorld[i].substring(newWorld[i].length()-1));
                if(topBlock.getShape() == Shape.pyramid || topBlock.getShape() == Shape.ball)
                    numberOfUnstackables++;
            }
        }
        if(direction == Direction.left)
        {
            numberOfStacks = location;
            for (int i = 0; i < location; i++)
            {
                Entity topBlock = entities.get(newWorld[i].substring(newWorld[i].length()-1));
                if(topBlock.getShape() == Shape.pyramid || topBlock.getShape() == Shape.ball)
                    numberOfUnstackables++;
            }
        }
        for(Entity block : command.getBlocks())
        {
            if(block.getShape() == Shape.pyramid || block.getShape() == Shape.ball)
                numberOfUnstackables++;
        }
        if(numberOfStacks < numberOfUnstackables)
        {
            System.out.println("Cannot place that many unstackable objects in that space");
            System.exit(1);
        }
        if(numberOfStacks == 0)
        {
            System.out.println("There is no room to preform this action");
            System.exit(1);
        }
    }

     public String GraphSearch(Node start)
     {
       Comparator<Node> compareNodes = new Comparator<Node>() {
             @Override
             public int compare(Node o1, Node o2) {
                 return o1.getValue() - o2.getValue();
             }
         };
       PriorityQueue<Node> frontier = new PriorityQueue<Node>(100000,compareNodes);
       ConstructGoalWorld(start);
       start.setValue(SetNodeValue(start,0));
       start.setFromNode(null);
       if(start.getValue() == 0)
       return "This is already true";
       frontier.add(start);
       ArrayList<Node> explored = new ArrayList<Node>();
       while(true)
       {
           if(frontier.size() == 0)
           {
               return "No plan found";
           }

           Node test = frontier.poll();
           if(test.getValue() == test.getWeightUntilHere())
           {
               String actions = "";
               while (test.getFromNode() != null)
               { Node from = test.getFromNode();
                 actions += from.getNeighbors().get(test);
                 test = from;
               }
               return actions;
           }
           explored.add(test);
           ArrayList<Node> expanded = ExpandNode(test);
           for (Node node : expanded)
           {
               if (!explored.contains(node))
               {
                   if(!frontier.contains(node))
                   {
                       node.setFromNode(test);
                       node.setWeightUntilHere(test.getWeightUntilHere()+1);
                       node.setValue(SetNodeValue(node, node.getWeightUntilHere()));
                       frontier.add(node);
                   }
                   else
                   {
                       int temp = SetNodeValue(node , test.getWeightUntilHere()+1);
                       if(node.getValue() > temp )
                       {
                           node.setFromNode(test);
                           node.setWeightUntilHere(test.getWeightUntilHere() + 1);
                           node.setValue(temp);
                       }
                   }
               }
           }
       }
     }

    private int SetNodeValue(Node node, int weightUntilHere)
    {
        int value = 0;
        String[] state = node.getState().split(";");
        String[] goals = world.split(";");
        // calculate differences with all the concerned blocks just gone
        for (int i=0; i< goals.length; i++)
        {
            // go through goals list, if something is in a place where it is not in initialState, wrong!
            for (int j = 0; j < goals[i].length(); j++) {
                char one = goals[i].charAt(j);
                char two = 'Q';
                if (state[i].length() > j) {
                    two = state[i].charAt(j);
                }
                if (one != two && one != '*')
                    value++;
            }
        }
        // calculate number of concerned blocks in the wrong place
        value += blockToRemove.length;
        if (direction == null && node.isHolding())
        {
            if (node.getHoldingBlock().equals(blockToRemove[0].getName()))
                value--;

        }
        else if (direction == Direction.here) {
            for (int i = 0; i < state[location].length() ; i++)
            {
                for(Entity block: blockToRemove)
                {
                    String name = block.getName();
                    if (state[location].charAt(i) == name.charAt(0))
                        value--;
                }
            }
        } else if (direction == Direction.under) {
           for(int i = 0; i < state[location].length(); i++)
            {
                char one = state[location].charAt(i);
                if(one == relationBlock.getName().charAt(0))
                    break;
                for(Entity block : blockToRemove)
                {
                    String name = block.getName();
                    if (state[location].charAt(i) == name.charAt(0))
                        value--;
                }
            }


        } else if (direction == Direction.right) {
           for(int i = state.length-1; i > location; i-- )
           {
               for(int j = 0; j < state[i].length(); j++)
               {
                   for(Entity block: blockToRemove)
                   {
                       String name = block.getName();
                       if (state[i].charAt(j) == name.charAt(0))
                           value--;
                   }
               }
           }
        } else if (direction == Direction.left) {
            for(int i = 0; i < location; i++)
            {
                for(int j = 0; j < state[i].length(); j++)
                {
                    for(Entity block: blockToRemove)
                    {
                        String name = block.getName();
                        if (state[i].charAt(j) == name.charAt(0))
                            value--;
                    }
                }
            }
        }
        return value+weightUntilHere;
    }

    private ArrayList<Node> ExpandNode(Node node) {
        String currentState = node.getState();
        String[] stacks = currentState.split(";");
        ArrayList<Node> neighborsToReturn = new ArrayList<Node>();
        Dictionary<Node, String> neighbors = new Hashtable<Node, String>();
        // loop through all stacks, we can only take an action for the top of each stack
        for (int i = 0; i < stacks.length; i++) {
            String modifiedStacks = "";
            if ((stacks[i].length() >= 1)) {
                if(!node.isHolding()  && (stacks[i].length() > 1))
                {
                    String currentStack = stacks[i];
                    Node temp;
                    String holdingBlock = currentStack.substring(currentStack.length()-1);
                    // actions include are
                    // lifting it and holding it, create a node for that
                    for (int j = 0; j < stacks.length; j++) {
                        if (i != j)
                            modifiedStacks += stacks[j] + ";";
                        else
                            modifiedStacks += currentStack.substring(0, currentStack.length() - 1) + ";";
                    }
                    if (nodes.containsKey(modifiedStacks)) {
                        temp = nodes.get(modifiedStacks);
                        temp.setHolding(true);
                        temp.setHoldingBlock(holdingBlock);
                        neighbors.put(temp, "I pick up " + BlockString(entities.get(holdingBlock)) + "\npick " + i + ";");
                        neighborsToReturn.add(temp);
                    } else {

                        temp = new Node();
                        temp.setState(modifiedStacks);
                        temp.setHolding(true);
                        temp.setHoldingBlock(holdingBlock);
                        nodes.put(modifiedStacks,temp);
                        neighbors.put(temp, "I pick up " + BlockString(entities.get(holdingBlock)) + "\npick " + i + ";");
                        neighborsToReturn.add(temp);
                    }
                    String thisStack = stacks[i].substring(0, stacks[i].length() - 1);
                    Entity topBlock = entities.get(stacks[i].substring(stacks[i].length() - 1));
                    for (int j = 0; j < stacks.length; j++) {
                        if (!(j == i)) {
                            modifiedStacks = "";
                            String stackToPlaceAt = stacks[j];
                            Entity thisStacksTop = entities.get(stacks[j].substring(stacks[j].length() - 1));
                            // if it is not a pyramid, ball and is equal or less size than that stacks top
                            if (CanPutOn(topBlock,thisStacksTop)) {
                                    stackToPlaceAt += topBlock.getName();
                                    for (int z = 0; z < stacks.length; z++) {
                                        if (z == j)
                                            modifiedStacks += stackToPlaceAt + ";";
                                        else if (z == i)
                                            modifiedStacks += thisStack + ";";
                                        else
                                            modifiedStacks += stacks[z] + ";";
                                    }
                                    String onInside = " on top of ";
                                    if (thisStacksTop.getShape() == Shape.box)
                                        onInside = " inside of ";
                                    if (nodes.containsKey(modifiedStacks)) {
                                        temp = nodes.get(modifiedStacks);
                                        topBlock.setStack(j);
                                        topBlock.setIndexInStack(thisStacksTop.getIndexInStack()+1);
                                        neighbors.put(temp,"I move " +BlockString(topBlock) + onInside + BlockString(thisStacksTop) + "\npick " + i + "\ndrop " + j + ";");
                                        neighborsToReturn.add(temp);
                                    } else {

                                        temp = new Node();
                                        temp.setState(modifiedStacks);
                                        topBlock.setStack(j);
                                        topBlock.setIndexInStack(thisStacksTop.getIndexInStack()+1);
                                        neighbors.put(temp,"I move " +BlockString(topBlock) + onInside + BlockString(thisStacksTop) + "\npick " + i + "\ndrop " + j + ";");
                                        nodes.put(modifiedStacks,temp);
                                        neighborsToReturn.add(temp);
                                    }

                            }
                        }
                    }
                }
                else if (node.isHolding())
                {
                    String currentStack = stacks[i];
                    Node temp;
                    // since we are holding something, the only action we can take is dropping it
                    // can only be done if it adheres to the rules
                    Entity currentlyHolding = entities.get(node.getHoldingBlock());
                    Entity blockToPutAt = entities.get(currentStack.substring(currentStack.length()-1));
                    if (CanPutOn(currentlyHolding, blockToPutAt)) {
                            for (int j = 0; j < stacks.length; j++) {
                                if (i != j)
                                    modifiedStacks += stacks[j] + ";";
                                else
                                    modifiedStacks += currentStack + node.getHoldingBlock() + ";";
                            }
                            if (nodes.containsKey(modifiedStacks)) {
                                temp = nodes.get(modifiedStacks);
                                temp.setHolding(false);
                                temp.setHoldingBlock("");
                                neighbors.put(temp, "I put " + BlockString(currentlyHolding) + "on " + BlockString(blockToPutAt) + "\ndrop " + i + ";");
                                currentlyHolding.setStack(i);
                                currentlyHolding.setIndexInStack(blockToPutAt.getIndexInStack()+1);
                                neighborsToReturn.add(temp);
                            } else {

                                temp = new Node();
                                temp.setState(modifiedStacks);
                                temp.setHolding(false);
                                temp.setHoldingBlock("");
                                neighbors.put(temp, "I put " + BlockString(currentlyHolding) + "on " + BlockString(blockToPutAt) + "\ndrop " + i + ";");
                                currentlyHolding.setStack(i);
                                currentlyHolding.setIndexInStack(blockToPutAt.getIndexInStack()+1);
                                neighborsToReturn.add(temp);
                                nodes.put(modifiedStacks,temp);
                            }


                    }

                }
            }
        }
        node.setNeighbors(neighbors);
        return neighborsToReturn;
    }

    private Boolean CanPutOn(Entity blockToPlace, Entity blockToPlaceAt)
    {
        if(blockToPlaceAt.getShape() == Shape.pyramid || blockToPlaceAt.getShape() == Shape.ball)
            return false;
        if(blockToPlace.getShape() == Shape.box && blockToPlaceAt.getSize().getValue() <= blockToPlace.getSize().getValue())
            return false;
        if(blockToPlace.getSize().getValue() > blockToPlaceAt.getSize().getValue())
            return false;
        return true;
    }

    private String BlockString(Entity block)
    {
        if (block.getShape() == Shape.floor)
            return "the floor";
        return "the " + block.getSize() + " " + block.getColour() + " " + block.getShape();
    }

}
