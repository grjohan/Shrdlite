package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static String world;
    static HashMap<String, Entity> entities;
    static ArrayList<ArrayList<Entity>> entityWorld;
    static Entity floor =  new Entity(Shape.floor, Colour.black, Size.floor, "X");
    public static void main(String[] args) throws ParserErrorException {

       String[] trees;
       String holding;
       holding = args[0];
       world = args[1];
       trees = args[2].split(";");

        System.out.println("# Group 19's Decent Java Planner!");
        System.out.println("# Holding: " + holding);
        System.out.println("# World: " + world);
        for (String t : trees) {
            System.out.println("# Tree: " + t);
        }

        entityWorld = new ArrayList<ArrayList<Entity>>();
        CreateEntities();
        PlaceEntities();
        String[] newWorld = world.split(";");
        world = "";
        for(int i = 0; i < newWorld.length; i++)
        {
            newWorld[i] = "X" + newWorld[i].replaceAll(",","").trim();
            world += newWorld[i] + ";";
        }
        world = world.substring(0,world.length()-1);
        ArrayList<String> workingTrees = new ArrayList<String>();
        HashMap<String,Command> treeToCommand = new HashMap<String, Command>();
        ArrayList<String> errorMessages = new ArrayList<String>();
        for (String tree : trees) {
            try {
                Parser parser = new Parser(tree);
                ExpressionParser exp = new ExpressionParser(entities, entities.get(holding), newWorld);
                Expression ex = parser.MakeExpressions();
                Command command = exp.ParseExpression(ex);
                workingTrees.add(tree);
                treeToCommand.put(tree, command);
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }
        if (workingTrees.size() == 0)
        {
            for(String message : errorMessages)
            System.out.println(message);
            System.exit(0);
        }
        Node start = new Node();
        start.setState(world);
        start.setWeightUntilHere(0);
        if(!holding.equals(""))
        {
            start.setHolding(true);
            start.setHoldingBlock(holding);
        }
        Planner planner = new Planner(world,treeToCommand.get(workingTrees.get(0)),entities);
        String[] actions = planner.GraphSearch(start).split(";");
        for(int i = actions.length-1; i >= 0; i--)
        {
            System.out.println(actions[i]);
        }
    }

    // ------------------------- below here is parsing ---------------------------------

    private static void PlaceEntities() {
        for (int i = 0; i < 10; i++) {
            ArrayList<Entity> temp = new ArrayList<Entity>();
            temp.add(floor);
            entityWorld.add(temp);
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
        // put "the floor" as the place with the least blocks on it
        int minStack = Integer.MAX_VALUE, minStackValue = Integer.MAX_VALUE;
        for (int i = 0; i < stacks.length ; i++)
        {
            if (stacks[i].length() < minStackValue)
            {
                minStack = i;
                minStackValue = stacks[i].length();
            }
        }
        entities.get("X").setStack(minStack);
        entities.get("X").setIndexInStack(0);
    }

    private static void AddEntity(String name, int stack, int index) {
        entities.get(name).setStack(stack);
        entities.get(name).setIndexInStack(index+1);
        entityWorld.get(stack).add(entities.get(name));
    }

    // Only hard coded things that have to do with the world below this point
    private static void CreateEntities() {   // create the entities given in world.js
        entities = new HashMap<String, Entity>();
        entities.put("a", new Entity(Shape.rectangle, Colour.blue, Size.tall, "a"));
        entities.put("b", new Entity(Shape.ball, Colour.white, Size.small, "b"));
        entities.put("c", new Entity(Shape.square, Colour.red, Size.large, "c"));
        entities.put("d", new Entity(Shape.pyramid, Colour.green, Size.large, "d"));
        entities.put("e", new Entity(Shape.box, Colour.white, Size.large, "e"));
        entities.put("f", new Entity(Shape.rectangle, Colour.black, Size.wide, "f"));
        entities.put("g", new Entity(Shape.rectangle, Colour.blue, Size.wide, "g"));
        entities.put("h", new Entity(Shape.rectangle, Colour.red, Size.wide, "h"));
        entities.put("i", new Entity(Shape.pyramid, Colour.yellow, Size.medium, "i"));
        entities.put("j", new Entity(Shape.box, Colour.red, Size.large, "j"));
        entities.put("k", new Entity(Shape.ball, Colour.yellow, Size.small, "k"));
        entities.put("l", new Entity(Shape.box, Colour.red, Size.medium, "l"));
        entities.put("m", new Entity(Shape.ball, Colour.blue, Size.medium, "m"));
        entities.put("X", floor);
    }
}
