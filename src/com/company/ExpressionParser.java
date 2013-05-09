package com.company;

import com.sun.media.sound.DirectAudioDeviceProvider;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-09
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionParser {
    HashMap<String,Entity> entities;
    public ExpressionParser(HashMap<String,Entity> entities)
    {
        this.entities = entities;
    }

    public ArrayList<Command> ParseExpression(Expression expression, boolean many)
    {
        ArrayList<Command> commands = new ArrayList<Command>();
        String thisExpression = expression.getThisCommand();
        if (thisExpression.equalsIgnoreCase("take") ||thisExpression.equalsIgnoreCase("put"))
        {
            // if take and the next is all, that is an error
            Movement movement = Movement.pick;
            if (thisExpression.equalsIgnoreCase("put"))
                movement = Movement.drop;
            commands = ParseExpression(expression.getSubExpressionOne(), many);
            if (thisExpression.equalsIgnoreCase("take") && commands.size() > 1)
                ExitWithError("It is not possible to pick \"All\"");
            for(Command command : commands)
                command.setMovement(movement);
            return commands;
        }

        else if(thisExpression.equalsIgnoreCase("move"))
        {
            ArrayList<Command> from = ParseExpression(expression.getSubExpressionOne(), many);
            Command to = ParseExpression(expression.getSubExpressionTwo(), many).get(0);
            for (Command command : from)
            {   Shape shape = command.getBlock().getShape();
                if ((shape.equals(Shape.Pyramid) || shape.equals(Shape.Ball)) && to.getDirection().equals(Direction.under))
                    ExitWithError("Can not place pyramids or balls under anything.");
                command.setMovement(Movement.move);
                command.setLocation(to.getLocation());
                command.setDirection(to.getDirection());
                command.setRelationBlock(to.getRelationBlock());
            }
            return from;

        }
        else if (thisExpression.equalsIgnoreCase("thatis"))
        {
            String[] blockDescription = expression.getSubExpressionOne().getThisCommand().split(" ");
            ArrayList<Entity> blocks = FindBlocks(blockDescription[2], blockDescription[3], blockDescription[1]);
            ArrayList<Entity> blocksThatFit = new ArrayList<Entity>();
            Command command = ParseExpression(expression.getSubExpressionTwo(), many).get(0);
            Direction direction = command.getDirection();
            if(direction.equals(Direction.here))
            {
                for (Entity block : blocks)
                {
                     if(block.getStack() == command.getBlock().getStack() && block.getIndexInStack() > command.getBlock().getIndexInStack())
                         blocksThatFit.add(block);
                }
            }
            else if(direction.equals(Direction.left))
            {
               for (Entity block : blocks)
               {
                   if (block.getStack() < command.getBlock().getStack())
                       blocksThatFit.add(block);
               }
            }
            else if(direction.equals(Direction.right))
            {
                for (Entity block : blocks)
                {
                    if (block.getStack() > command.getBlock().getStack())
                        blocksThatFit.add(block);
                }
            }

            if(blocksThatFit.size() == 0)
            {
                ExitWithError("There were not blocks that fit that description");
            }
            for (Entity block : blocksThatFit)
            {
                Command temp = new Command();
                command.setBlock(block);
                commands.add(command);
            }
            if(!many)
            {
                Command temp = commands.get(0);
                commands.clear();
                commands.add(temp);
            }
            return commands;
        }
        else if (thisExpression.equalsIgnoreCase("leftof") ||thisExpression.equalsIgnoreCase("inside") || thisExpression.equalsIgnoreCase("ontop") || thisExpression.equalsIgnoreCase("rightof") ||thisExpression.equalsIgnoreCase("under") )
        {
            Direction direction = Direction.here;
            if(thisExpression.equalsIgnoreCase("leftof"))
                direction = Direction.left;
            else if(thisExpression.equalsIgnoreCase("rightof"))
                direction = Direction.right;
            else if(thisExpression.equalsIgnoreCase("under"))
                direction = Direction.under;
            Command temp =  ParseLocation(expression.getSubExpressionOne(),direction);
            commands.add(temp);
            return commands;
        }
        else if (thisExpression.contains("block"))
        {
            String[] blockDescription = thisExpression.split(" ");
            ArrayList<Entity> blocks = FindBlocks(blockDescription[2], blockDescription[3], blockDescription[1]);
            for (Entity block : blocks)
            {
                Command command = new Command();
                command.setBlock(block);
                commands.add(command);
            }
            return commands;
        }
        else if (thisExpression.equalsIgnoreCase("the") ||thisExpression.equalsIgnoreCase("any") )
        {
            return ParseExpression(expression.getSubExpressionOne(),false);
        }
        else if (thisExpression.equalsIgnoreCase("all"))
        {
            return ParseExpression(expression.getSubExpressionOne(),true);
        }
        ExitWithError("Something went terribly wrong, Thank god!");
        return null;  // we should never ever get here
    }

    private Command ParseLocation(Expression expression, Direction direction)
    {
      // a location is the/any/all
        Command command = new Command();
        command.setDirection(direction);
        String thisExpression = expression.getThisCommand();
        String[] block = expression.getSubExpressionOne().getThisCommand().split(" ");
        ArrayList<Entity> blocks = FindBlocks(block[2], block[3], block[1]);
        if (thisExpression.equalsIgnoreCase("all"))
        {
            if (direction.equals(Direction.left))
            {
                command.setLocation(blocks.get(0).getStack());
            }
            else if (direction.equals(Direction.here))
            {
                ExitWithError("It is not possibly to put a block inside \"all\" of something");
            }
            else if (direction.equals(Direction.right))
            {
                command.setLocation(blocks.get(blocks.size()-1).getStack());
            }
            else if (direction.equals(Direction.under))
            {
                Entity temp = blocks.get(0);
                command.setLocation(temp.getStack());
                command.setRelationBlock(temp);
            }
        }
        else
        {
            if (direction.equals(Direction.left))
            {
                command.setLocation(blocks.get(blocks.size()-1).getStack());
            }
            else if (direction.equals(Direction.here))
            {
                command.setDirection(Direction.here);
                if(block[1].equals("pyramid") || block[1].equals("ball"))
                    ExitWithError("Can not put objects on/ balls or pyramids");
                command.setLocation(blocks.get(0).getStack());
            }
            else if (direction.equals(Direction.right))
            {
                command.setLocation(blocks.get(0).getStack());
            }
            else if (direction.equals(Direction.under))
            {
                Entity temp = blocks.get(0);
                command.setLocation(temp.getStack());
                command.setRelationBlock(temp);
            }
        }
        return command;
    }


    private ArrayList<Entity> FindBlocks(String size, String colour, String kind)
    {
        ArrayList<Entity> possibilities = new ArrayList<Entity>(entities.values());

        if(kind.equalsIgnoreCase("pyramid"))
            possibilities =  RemoveShape(possibilities, Shape.Pyramid);
        else if(kind.equalsIgnoreCase("box"))
            possibilities = RemoveShape(possibilities, Shape.Box);
        else if(kind.equalsIgnoreCase("ball"))
            possibilities = RemoveShape(possibilities, Shape.Ball);
        else if(kind.equalsIgnoreCase("rectangle"))
            possibilities = RemoveShape(possibilities, Shape.Rectangle);
        else if(kind.equalsIgnoreCase("square"))
            possibilities = RemoveShape(possibilities, Shape.Square);

        if(colour.equalsIgnoreCase("red"))
            possibilities = RemoveColour(possibilities, Colour.Red);
        else if(colour.equalsIgnoreCase("black"))
            possibilities = RemoveColour(possibilities, Colour.Black);
        else if(colour.equalsIgnoreCase("blue"))
            possibilities = RemoveColour(possibilities, Colour.Blue);
        else if(colour.equalsIgnoreCase("green"))
            possibilities = RemoveColour(possibilities, Colour.Green);
        else if(colour.equalsIgnoreCase("yellow"))
            possibilities = RemoveColour(possibilities, Colour.Yellow);
        else if(colour.equalsIgnoreCase("white"))
            possibilities = RemoveColour(possibilities, Colour.White);

        if(size.equalsIgnoreCase("large"))
            possibilities = RemoveSize(possibilities, Size.large);
        else if(size.equalsIgnoreCase("medium"))
            possibilities = RemoveSize(possibilities, Size.medium);
        else if(size.equalsIgnoreCase("small"))
            possibilities = RemoveSize(possibilities, Size.small);
        else if(size.equalsIgnoreCase("tall"))
            possibilities = RemoveSize(possibilities, Size.tall);
        else if(size.equalsIgnoreCase("wide"))
            possibilities = RemoveSize(possibilities, Size.wide);

        return possibilities;

    }

    private ArrayList<Entity> RemoveShape(ArrayList<Entity> possibilities, Shape shape) {
        Iterator<Entity> iterator = possibilities.iterator();
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getShape().equals(shape))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private ArrayList<Entity> RemoveColour(ArrayList<Entity> possibilities, Colour colour) {
        Iterator<Entity> iterator = possibilities.iterator();
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getColour().equals(colour))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private ArrayList<Entity> RemoveSize(ArrayList<Entity> possibilities, Size size) {
        Iterator<Entity> iterator = possibilities.iterator();
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getSize().equals(size))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private void ExitWithError(String errorMessage)
    {
        System.out.println(errorMessage);
        System.exit(0);
    }
}
