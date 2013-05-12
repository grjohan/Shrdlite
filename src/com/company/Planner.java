package com.company;

import java.util.*;

public class Planner {
    String holding;
    Entity currentlyHolding;
    String world;
    String goal;
    int q = 0;
    HashMap<String, Entity> entities;
    Hashtable<String,Node> nodes;
    ArrayList<ArrayList<Entity>> entityWorld;
    String[] trees;
    ArrayList<Command> commands = new ArrayList<Command>();
    Parser parser;

    public Planner(String holding, String world, String goal,HashMap<String, Entity> entities,ArrayList<ArrayList<Entity>> entityWorld ) {
        this.holding = holding;
        this.world = world;
        this.goal = goal;
        this.entities = entities;
        this.entityWorld = entityWorld;
        nodes = new Hashtable<String, Node>();
        // Print the data
        // if we are holding something, set that as currently_holding
        if (!holding.equals("")) {
            for (String name : entities.keySet()) {
                if (holding.equals(name))
                    currentlyHolding = entities.get(name);
            }
        }
    }

    // --------------- here is code to carry out commands -----------------
    private void CarryOutCommand(Command command) {    // find out where exactly my block is if I want to move or take something
        ArrayList<Integer> allowedStacks = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            allowedStacks.add(i);
        }
        if (command.getMovement().equals(Movement.drop)) {
            if (holding.equals(""))
                return;
            if (command.getDirection().equals(Direction.here)) {
                PlaceBlock(command.getLocation(), currentlyHolding);
                return;
            } else if (command.getDirection().equals(Direction.right)) {
                for (int i = command.getLocation(); i > -1; i--) {
                    allowedStacks.remove((Integer) i);
                }
            } else if (command.getDirection().equals(Direction.left)) {
                for (int i = command.getLocation(); i < 10; i++) {
                    allowedStacks.remove((Integer) i);
                }
            }
            int stack = FindSupportingStack(currentlyHolding, command.getLocation(), allowedStacks);
            DropBlock(stack);
            return;
        }
        int stack = command.getBlock().getStack();
        int indexInStack = command.getBlock().getIndexInStack();

        if (command.getMovement().equals(Movement.pick)) {
            // check if we are already holding something, in that case drop it
            if (!holding.equals("")) {
                DropBlock(FindSupportingStack(currentlyHolding, stack, allowedStacks));
            }

            if (!(indexInStack == entityWorld.get(stack).size() - 1)) {
                // move all the thing above it to an empty space
                for (int i = entityWorld.get(stack).size() - 1; i > indexInStack; i--) {
                    int moveTo = FindSupportingStack(entityWorld.get(stack).get(i), stack, allowedStacks);
                    PickBlock(entityWorld.get(stack).get(i), stack);
                    DropBlock(moveTo);
                }

            }
            PickBlock(entityWorld.get(stack).get(indexInStack), stack);

        }
    }

    private void PlaceBlock(int stack, Entity block) {
        ArrayList<Integer> allowedStacks = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            allowedStacks.add(i);
        }
        Entity currentTopBlock = entityWorld.get(stack).get(entityWorld.get(stack).size() - 1);
        if (!CanSupport(block, currentTopBlock)) {
            Entity blockToDrop = currentlyHolding;
            int whereWeDropped = FindSupportingStack(currentlyHolding, stack, allowedStacks);
            DropBlock(whereWeDropped);
            allowedStacks.remove((Integer) whereWeDropped);
            while (!CanSupport(blockToDrop, currentTopBlock)) {
                int moveTo = FindSupportingStack(currentTopBlock, stack, allowedStacks);
                PickBlock(currentTopBlock, stack);
                DropBlock(moveTo);
                currentTopBlock = entityWorld.get(stack).get(entityWorld.get(stack).size() - 1);
            }
            PickBlock(block, block.getStack());
        }

        DropBlock(stack);
    }

    private void PickBlock(Entity block, int stack) {
        holding = block.getName();
        currentlyHolding = block;
        entityWorld.get(stack).remove(entityWorld.get(stack).size() - 1);
        System.out.println("pick " + stack);
    }

    private void DropBlock(int stack) {
        System.out.println("drop " + stack);
        currentlyHolding.setStack(stack);
        currentlyHolding.setIndexInStack(entityWorld.get(stack).size() - 1);
        entityWorld.get(stack).add(currentlyHolding);
        holding = "";
        currentlyHolding = null;
    }

    private int FindSupportingStack(Entity block, int currentStack, ArrayList<Integer> allowedStacks) {
        int plusMinus = 1;
        while (plusMinus < 8) {
            if (allowedStacks.contains(currentStack - plusMinus)) {
                if (entityWorld.get(currentStack - plusMinus).size() == 0) {
                    return currentStack - plusMinus;
                }
                if (CanSupport(block, entityWorld.get(currentStack - plusMinus).get(entityWorld.get(currentStack - plusMinus).size() - 1))) {
                    return currentStack - plusMinus;
                }
            }
            if (allowedStacks.contains(currentStack + plusMinus)) {
                if (entityWorld.get(currentStack + plusMinus).size() == 0) {
                    return currentStack + plusMinus;
                }
                if (CanSupport(block, entityWorld.get(currentStack + plusMinus).get(entityWorld.get(currentStack + plusMinus).size() - 1))) {
                    return currentStack + plusMinus;
                }
            }
            plusMinus++;
        }
        return Integer.MAX_VALUE; // if we are here to block cannot be placed anywhere
        // check the stacks next to it to see if we can put it there, put it in the closes accepting stack
    }

    // Returns true if the block1 can be supported by the block2
    private Boolean CanSupport(Entity block1, Entity block2) {
        if (block2.getShape() == Shape.Ball || block2.getShape() == Shape.Pyramid)
            return false;
        return block2.getSize().getValue() >= block1.getSize().getValue();
    }


     public String GraphSearch(Node start, String goal)
     {
         Comparator<Node> compareNodes = new Comparator<Node>() {
             @Override
             public int compare(Node o1, Node o2) {
                 return o1.getValue() - o2.getValue();
             }
         };
       PriorityQueue<Node> frontier = new PriorityQueue<Node>(100000,compareNodes);

       //ArrayList<Node> frontier =  new ArrayList<Node>();
       start.setFromNode(null);
       frontier.add(start);
       ArrayList<Node> explored = new ArrayList<Node>();
       while(true)
       {
           if(frontier.size() == 0)
               return "No plan found";
           Node test = frontier.poll();
           if (++q == 10000)
           {
               int x = 1;
           }

           if(test.getState().equals(goal))
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
                   SetNodeValue(node,goal);
                   frontier.add(node);
               }
           }
       }
     }

    private void SetNodeValue(Node node, String goal)
    {
       int value = 0;
       String[] state = node.getState().split(";");
       String[] goals = goal.split(";");
       for (int i = 0; i < state.length - 1; i++)
       {
         char one,two;
         int min = Math.min(state[i].length(),goals[i].length());
         int max = Math.max(state[i].length(),goals[i].length());
         value += max-min;
         for(int j = 0; j < min; j++)
         {
             one = state[i].charAt(j);
             two = goals[i].charAt(j);
             if (one != two)
                 value++;
         }
       }
        node.setValue(value);
    }

    private ArrayList<Node> ExpandNode(Node node)
    {
        String currentState = node.getState();
        String[] stacks = currentState.split(";");
        ArrayList<Node> neighborsToReturn = new ArrayList<Node>();
        Dictionary<Node, String> neighbors = new Hashtable<Node, String>();
        // loop through all stacks, we can only take an action for the top of each stack
        for (int i = 0; i < stacks.length - 1; i++) {
            String modifiedStacks = "";
            if ((stacks[i].length() > 1)) {
                String currentStack = stacks[i];
                Node temp;
                // actions include are
                // lifting it and holding it, create a node for that
                if(! node.isHolding())
                {
                    for (int j = 0; j < stacks.length; j++) {
                        if (i != j)
                            modifiedStacks += stacks[j] + ";";
                        else
                            modifiedStacks += currentStack.substring(0, currentStack.length() - 1) + ";";
                    }

                    if (nodes.containsKey(modifiedStacks)) {
                        temp = nodes.get(modifiedStacks);
                        temp.setHolding(true);
                        neighbors.put(temp, "pick " + i);
                        neighborsToReturn.add(temp);
                    } else {

                        temp = new Node();
                        temp.setState(modifiedStacks);
                        temp.setHolding(true);
                        neighbors.put(temp, "pick " + i);
                        neighborsToReturn.add(temp);
                    }
                }

                String thisStack = stacks[i].substring(0, stacks[i].length() - 1);
                Entity topBlock = entities.get(stacks[i].substring(stacks[i].length() - 1));
                for (int j = 0; j < stacks.length - 1; j++) {
                    modifiedStacks = "";
                    String stackToPlaceAt = stacks[j];
                    Entity thisStacksTop = entities.get(stacks[j].substring(stacks[j].length() - 1));
                    // if it is not a pyramid, ball and is equal or less size than that stacks top
                    if( (thisStacksTop.getShape() != Shape.Pyramid || thisStacksTop.getShape() != Shape.Ball) && topBlock.getSize().getValue() <= thisStacksTop.getSize().getValue())
                    {
                        //    if it is a box, it has to be strictly less than that stacks top
                        if( !(topBlock.getShape() == Shape.Box && topBlock.getSize().getValue() > thisStacksTop.getSize().getValue()))
                        {
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
                                neighborsToReturn.add(temp);
                            }
                        }
                    }
                }
            }
        }
      node.setNeighbors(neighbors);
      return neighborsToReturn;
    }

}
