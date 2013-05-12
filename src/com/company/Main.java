package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static String world;
    static HashMap<String, Entity> entities;
    static ArrayList<ArrayList<Entity>> entityWorld;
    static Entity floor =  new Entity(Shape.Floor, Colour.Black, Size.floor, "X");
    public static void main(String[] args) {

        String[] trees;
        String holding;
        holding = "";
        world = "; a,b ; c,d ; ; e,f,g,h,i ; ; ; j,k ; ; l,m";
        trees = "( move ( all ( block rectangle _ _ ) ) ( leftof ( the ( block ball _ white ) ) ) )".split(";");
        String goal = "";
        System.out.println("# Group 19's Stupid Java Planner!");
        System.out.println("# Holding: " + holding);
        System.out.println("# World: " + world);
        for (String t : trees) {
            System.out.println("# Tree: " + t);
        }
        // holding = args[0];
        // world = args[1];
        // trees = args[2].split(";");
        entityWorld = new ArrayList<ArrayList<Entity>>();
        CreateEntities();
        PlaceEntities();
        Parser parser = new Parser(trees[0]);
        ExpressionParser exp = new ExpressionParser(entities);
        Expression ex = parser.MakeExpressions();
        ArrayList<Command> commands = exp.ParseExpression(ex, false);
        Planner planner = new Planner(holding,world,goal,entities,entityWorld);
        Node start = new Node();
        start.setState("X;Xab;Xcd;X;Xefghi;X;X;Xjk;X;Xlm");
        goal = "Xfgh;Xab;Xcd;X;Xei;X;X;Xjk;X;Xlm;";
        String[] actions = planner.GraphSearch(start,goal).split(";");
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
    }

    private static void AddEntity(String name, int stack, int index) {
        entities.get(name).setStack(stack);
        entities.get(name).setIndexInStack(index);
        entityWorld.get(stack).add(entities.get(name));
    }

    // Only hard coded things that have to do with the world below this point
    private static void CreateEntities() {   // create the entities given in world.js
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
        entities.put("X", floor);
    }
}
