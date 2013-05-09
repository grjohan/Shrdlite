package com.company;

import java.util.*;

public class Planner {
    String holding;
    Entity currentlyHolding;
    String world;
    HashMap<String, Entity> entities;
    ArrayList<ArrayList<Entity>> entityWorld;
    String[] trees;
    ArrayList<Command> commands = new ArrayList<Command>();
    Parser parser;

    public Planner(String args[]) {
        holding = "";
        world = "; a,b ; c,d ; ; e,f,g,h,i ; ; ; j,k ; ; l,m";
        trees = "( move ( all ( block rectangle _ _ ) ) ( leftof ( the ( block ball _ white ) ) ) )".split(";");
        // holding = args[0];
        // world = args[1];
        // trees = args[2].split(";");
        entityWorld = new ArrayList<ArrayList<Entity>>();

        // Print the data
        System.out.println("# Group 19's Stupid Java Planner!");
        System.out.println("# Holding: " + holding);
        System.out.println("# World: " + world);
        for (String t : trees) {
            System.out.println("# Tree: " + t);
        }
        parser = new Parser(trees[0]);
        CreateEntities();
        PlaceEntities();
        // if we are holding something, set that as currently_holding
        if (!holding.equals("")) {
            for (String name : entities.keySet()) {
                if (holding.equals(name))
                    currentlyHolding = entities.get(name);
            }
        }

        ExpressionParser exp = new ExpressionParser(entities);
        Expression ex = parser.MakeExpressions();
        ArrayList<Command> commands = exp.ParseExpression(ex, false);
        for (Command command : commands)
            CarryOutCommand(command);
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

    // ------------------------- below here is parsing ---------------------------------

    private void PlaceEntities() {
        for (int i = 0; i < 10; i++) {
            entityWorld.add(new ArrayList<Entity>());
        }

        String[] stacks = world.split(";");
        for (int j = 0; j < stacks.length; j++) {
            String stack = stacks[j].replaceAll("\\s|,", "");
            for (int i = 0; i < stack.length(); i++) {
                switch (stack.charAt(i)) {
                    case 'a':
                        AddEntity("a", j, i);
                        break;
                    case 'b':
                        AddEntity("b", j, i);
                        break;
                    case 'c':
                        AddEntity("c", j, i);
                        break;
                    case 'd':
                        AddEntity("d", j, i);
                        break;
                    case 'e':
                        AddEntity("e", j, i);
                        break;
                    case 'f':
                        AddEntity("f", j, i);
                        break;
                    case 'g':
                        AddEntity("g", j, i);
                        break;
                    case 'h':
                        AddEntity("h", j, i);
                        break;
                    case 'i':
                        AddEntity("i", j, i);
                        break;
                    case 'j':
                        AddEntity("j", j, i);
                        break;
                    case 'k':
                        AddEntity("k", j, i);
                        break;
                    case 'l':
                        AddEntity("l", j, i);
                        break;
                    case 'm':
                        AddEntity("m", j, i);
                        break;
                }

            }
        }
    }

    private void AddEntity(String name, int stack, int index) {
        entities.get(name).setStack(stack);
        entities.get(name).setIndexInStack(index);
        entityWorld.get(stack).add(entities.get(name));
    }

    // Only hard coded things that have to do with the world below this point
    private void CreateEntities() {   // create the entities given in world.js
        entities = new HashMap<String, Entity>();
        entities.put("a", new Entity(Shape.Rectangle, Colour.Blue, Size.tall, "a"));
        entities.put("b", new Entity(Shape.Ball, Colour.White, Size.small, "b"));
        entities.put("c", new Entity(Shape.Square, Colour.Red, Size.large, "c"));
        entities.put("d", new Entity(Shape.Pyramid, Colour.Green, Size.large, "d"));
        entities.put("e", new Entity(Shape.Box, Colour.White, Size.large, "e"));
        entities.put("f", new Entity(Shape.Rectangle, Colour.Black, Size.wide, "f"));
        entities.put("g", new Entity(Shape.Rectangle, Colour.Blue, Size.wide, "g"));
        entities.put("h", new Entity(Shape.Rectangle, Colour.Red, Size.wide, "h"));
        entities.put("i", new Entity(Shape.Pyramid, Colour.Yellow, Size.medium, "i"));
        entities.put("j", new Entity(Shape.Box, Colour.Red, Size.large, "j"));
        entities.put("k", new Entity(Shape.Ball, Colour.Yellow, Size.small, "k"));
        entities.put("l", new Entity(Shape.Box, Colour.Red, Size.medium, "l"));
        entities.put("m", new Entity(Shape.Ball, Colour.Blue, Size.medium, "m"));
    }


}
