package com.company;

import java.util.*;

public class Planner {
    String world;
    Direction direction;
    int location;
    Entity relationBlock = null;
    HashMap<String, Entity> entities;
    Hashtable<String,Node> nodes;
    ArrayList<Command> commands = new ArrayList<Command>();
    Entity[] blockToRemove;

    public Planner(String world, ArrayList<Command> commands,HashMap<String, Entity> entities) {
        this.world = world;
        this.commands = commands;
        this.entities = entities;
        nodes = new Hashtable<String, Node>();
        // Print the data
        // if we are holding something, set that as currently_holding
    }

    private void ConstructGoalWorld(Node start) {
        blockToRemove = new Entity[commands.size()];
        String[] oldWorld = world.split(";");
        String[] newWorld = new String[oldWorld.length];
        world = "";
        direction = commands.get(0).getDirection();
        location = commands.get(0).getLocation();
        relationBlock = commands.get(0).getRelationBlock();
        for (int i = 0; i < commands.size(); i++) {
            blockToRemove[i] = commands.get(i).getBlock();
        }
        if(start.isHolding())
            blockToRemove[0] = entities.get(start.getHoldingBlock());
        for (int i = 0; i < oldWorld.length; i++) {
            newWorld[i] = oldWorld[i];
            for (int j = 0; j < blockToRemove.length; j++) {
                String block = blockToRemove[j].getName();
                newWorld[i] = newWorld[i].replaceAll(block, "");
            }
            world += newWorld[i] + ";";
        }
        world = world.substring(0, world.length() - 1);

        // TODO fix so that ontop/under command has blocks on top as don't care terms if they do not fit back on top
        // TODO fix in parser so that it understands the command floor
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
       SetNodeValue(start);
       start.setFromNode(null);
       frontier.add(start);
       ArrayList<Node> explored = new ArrayList<Node>();
       while(true)
       {
           if(frontier.size() == 0)
               return "No plan found";
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
               if (! frontier.contains(node) && !explored.contains(node))
               {
                   node.setFromNode(test);
                   node.setWeightUntilHere(test.getWeightUntilHere()+1);
                   SetNodeValue(node);
                   frontier.add(node);
               }
           }
       }
     }

    private void SetNodeValue(Node node)
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
                if (one != two)
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
        node.setValue(value+node.getWeightUntilHere());
    }

    private ArrayList<Node> ExpandNode(Node node) {
        String currentState = node.getState();
        String[] stacks = currentState.split(";");
        ArrayList<Node> neighborsToReturn = new ArrayList<Node>();
        Dictionary<Node, String> neighbors = new Hashtable<Node, String>();
        // loop through all stacks, we can only take an action for the top of each stack
        for (int i = 0; i < stacks.length; i++) {
            String modifiedStacks = "";
            if ((stacks[i].length() > 1)) {
                if(!node.isHolding())
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
                        neighbors.put(temp, "pick " + i + ";");
                        neighborsToReturn.add(temp);
                    } else {

                        temp = new Node();
                        temp.setState(modifiedStacks);
                        temp.setHolding(true);
                        temp.setHoldingBlock(holdingBlock);
                        nodes.put(modifiedStacks,temp);
                        neighbors.put(temp, "pick " + i + ";");
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
                                    if (nodes.containsKey(modifiedStacks)) {
                                        temp = nodes.get(modifiedStacks);
                                        neighbors.put(temp, "pick " + i + "\ndrop " + j + ";");
                                        neighborsToReturn.add(temp);
                                    } else {

                                        temp = new Node();
                                        temp.setState(modifiedStacks);
                                        neighbors.put(temp, "pick " + i + "\ndrop " + j + ";");
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
                                neighbors.put(temp, "drop " + i + ";");
                                neighborsToReturn.add(temp);
                            } else {

                                temp = new Node();
                                temp.setState(modifiedStacks);
                                temp.setHolding(false);
                                temp.setHoldingBlock("");
                                neighbors.put(temp, "drop " + i + ";");
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
        if(blockToPlaceAt.getShape() == Shape.Pyramid || blockToPlaceAt.getShape() == Shape.Ball)
            return false;
        if(blockToPlace.getShape() == Shape.Box && blockToPlaceAt.getSize().getValue() <= blockToPlace.getSize().getValue())
            return false;
        if(blockToPlace.getSize().getValue() > blockToPlaceAt.getSize().getValue())
            return false;
        return true;
    }

}
