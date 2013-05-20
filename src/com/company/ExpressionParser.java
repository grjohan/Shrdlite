package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpressionParser {
    HashMap<String, Entity> entities;
    Entity currentlyHolding;
    String[] world;

    public ExpressionParser(HashMap<String, Entity> entities, Entity currentlyHolding, String[] world) {
        this.entities = entities;
        this.currentlyHolding = currentlyHolding;
        this.world = world;
    }

    public Command ParseExpression(Expression expression) throws ParserErrorException {
        String thisExpression = expression.getThisCommand();
        if (thisExpression.equalsIgnoreCase("take") || thisExpression.equalsIgnoreCase("put")) {

            Movement movement = Movement.pick;
            if (thisExpression.equalsIgnoreCase("put"))
            {
                movement = Movement.drop;
            }
            Command command = ParseExpression(expression.getSubExpressionOne());
            // if the command was take, and we are given more than one object, there is an error
            if (thisExpression.equalsIgnoreCase("take") && command.getBlockDeterminer().equalsIgnoreCase("all"))
                ExitWithError("It is not possible to pick \"All\"");
               if (movement == Movement.drop)
               {
                   Entity blockToPut = currentlyHolding;
                   // special case if you want to put something under all blocks
                   if (command.getRelationBlocksDeterminer().equalsIgnoreCase("all")) {
                       // check so that all of them are in a stack then apply rules to bottom one
                       int stack = Integer.MAX_VALUE;
                       int lowestStackIndex = Integer.MAX_VALUE;
                       int highestStackIndex = Integer.MAX_VALUE;
                       Entity lowestBlock = command.getRelationBlocks().get(0);
                       Entity highestBlock = command.getRelationBlocks().get(0);
                       for (Entity possiblyStackedBlock : command.getRelationBlocks()) {
                           if (lowestStackIndex > possiblyStackedBlock.getIndexInStack()) {
                               lowestStackIndex = possiblyStackedBlock.getIndexInStack();
                               lowestBlock = possiblyStackedBlock;
                           }
                           if (highestStackIndex < possiblyStackedBlock.getIndexInStack()) {
                               highestStackIndex = possiblyStackedBlock.getIndexInStack();
                               highestBlock = possiblyStackedBlock;
                           }
                           if (stack == Integer.MAX_VALUE)
                               stack = possiblyStackedBlock.getStack();
                           else if (stack != possiblyStackedBlock.getStack())
                               ExitWithError("Cannot place it under \"All\" since they are not in the same stack");
                       }
                       if(command.getDirection() == Direction.under)
                       {
                           ArrayList<Entity> temp = new ArrayList<Entity>();
                           temp.add(lowestBlock);
                           command.setRelationBlocks(temp);
                       } else if (command.getDirection() == Direction.here)
                       {
                           ArrayList<Entity> temp = new ArrayList<Entity>();
                           temp.add(highestBlock);
                           command.setRelationBlocks(temp);
                       }

                   }
                   else if (command.getRelationBlocksDeterminer().equalsIgnoreCase("any"))
                   {
                       //Check which block follows the rules, and pick one of those.
                       ArrayList<Entity> blocksThatWork = new ArrayList<Entity>();
                       for(Entity temp : command.getRelationBlocks())
                       {
                           String ans = CheckRules(blockToPut,temp,command.getDirection());
                           if(ans.equalsIgnoreCase("OK"))
                                blocksThatWork.add(temp);
                       }
                       if(blocksThatWork.size() == 0)
                           ExitWithError("There are no blocks that work, sorry");
                       else
                       {
                           ArrayList<Entity> temp = new ArrayList<Entity>();
                           temp.add(blocksThatWork.get(0));
                           command.setRelationBlocks(temp);
                           System.out.println("Since the word any was used : " + BlockString(temp.get(0)) + " was picked, if another one is desired, please rephrase");
                           command.setLocation(temp.get(0).getStack());
                       }
                   }


               }
                else if( movement == Movement.pick)
               {
                   if (command.getBlockDeterminer().equalsIgnoreCase("all"))
                   {
                        ExitWithError("Cannot pick up more than one block");
                   }
                   else if (command.getBlockDeterminer().equalsIgnoreCase("any")) {
                       ArrayList<Entity> temp = new ArrayList<Entity>();
                       temp.add(FindMostAccesibleBlock(command.getBlocks()));
                       command.setBlocks(temp);
                   }

               }
                command.setMovement(movement);
                if(movement == Movement.drop)
                    command.addBlock(currentlyHolding);
            return command;
        } else if (thisExpression.equalsIgnoreCase("move")) {
            Command from = ParseExpression(expression.getSubExpressionOne());
            Command to = ParseExpression(expression.getSubExpressionTwo());
            for (Entity block : from.getBlocks()) {
                ArrayList<Entity> entitiesThatFit = new ArrayList<Entity>();
                for(Entity blockDestination : to.getRelationBlocks())
                {
                    String ans = CheckRules(block,blockDestination,to.getDirection());
                    if(ans.equalsIgnoreCase("ok"))
                    {
                        entitiesThatFit.add(blockDestination);
                    }
                }
                if(entitiesThatFit.size() == 0)
                {
                    ExitWithError("There was nothing that fit this description");
                }
                to.setLocation(entitiesThatFit.get(0).getStack());
                to.setRelationBlocks(entitiesThatFit);
            }
            if (to.getRelationBlocksDeterminer().equalsIgnoreCase("any") && to.getRelationBlocks().size() >1)
            {
                System.out.println("There were many blocks that fit this description that worked, so " + BlockString(to.getRelationBlocks().get(0)) + " was chosen. If a different one is desired please rephrase.");
            }
            from.setMovement(Movement.move);
            from.setLocation(to.getLocation());
            from.setDirection(to.getDirection());
            ArrayList<Entity> temp = new ArrayList<Entity>();
            for (Entity block : to.getRelationBlocks()) {
                temp.add(block);
            }
            from.setRelationBlocks(temp);
            return from;

        } else if (thisExpression.equalsIgnoreCase("thatis")) {
            String[] blockDescription = expression.getSubExpressionOne().getThisCommand().split(" ");
            ArrayList<Entity> blocks = FindBlocks(blockDescription[2], blockDescription[3], blockDescription[1]);
            ArrayList<Entity> blocksThatFit = new ArrayList<Entity>();
            Command command = ParseExpression(expression.getSubExpressionTwo());
            Entity LimiterBlock = command.getRelationBlocks().get(0);
            Direction direction = command.getDirection();
            if (direction.equals(Direction.here)) {
                for (Entity block : blocks) {
                    if (block.getStack() == LimiterBlock.getStack() && block.getIndexInStack() > LimiterBlock.getIndexInStack())
                        blocksThatFit.add(block);
                }
            } else if (direction.equals(Direction.left)) {
                for (Entity block : blocks) {
                    if (block.getStack() < command.getLocation())
                        blocksThatFit.add(block);
                }
            } else if (direction.equals(Direction.right)) {
                for (Entity block : blocks) {
                    if (block.getStack() > command.getLocation())
                        blocksThatFit.add(block);
                }
            }

            if (blocksThatFit.size() == 0) {
                ExitWithError("There were no blocks that fit that description");
            }
            command.setBlocks(blocksThatFit);
            return command;
        } else if (thisExpression.equalsIgnoreCase("leftof") || thisExpression.equalsIgnoreCase("inside") || thisExpression.equalsIgnoreCase("ontop") || thisExpression.equalsIgnoreCase("rightof") || thisExpression.equalsIgnoreCase("under")) {
            Direction direction = Direction.here;
            if (thisExpression.equalsIgnoreCase("leftof"))
                direction = Direction.left;
            else if (thisExpression.equalsIgnoreCase("rightof"))
                direction = Direction.right;
            else if (thisExpression.equalsIgnoreCase("under"))
                direction = Direction.under;
            return ParseLocation(expression.getSubExpressionOne(), direction);
        } else if (thisExpression.contains("block")) {
            String[] blockDescription = thisExpression.split(" ");
            ArrayList<Entity> blocks = FindBlocks(blockDescription[2], blockDescription[3], blockDescription[1]);
            Command command = new Command();
            command.setBlocks(blocks);
            return command;
        } else if (thisExpression.equalsIgnoreCase("the") || thisExpression.equalsIgnoreCase("any") || thisExpression.equalsIgnoreCase("all") ) {
            Command temp = ParseExpression(expression.getSubExpressionOne());
            temp.setBlockDeterminer(thisExpression);
            if (thisExpression.equalsIgnoreCase("the") && temp.getBlocks().size() > 1) {
                String errorString = "Please clarify if you meant the ";
                for (Entity block : temp.getBlocks()) {
                    errorString += BlockString(block) + " or the ";
                }
                errorString = errorString.substring(0, errorString.length() - 8);
                ExitWithError(errorString);
            }
            return temp;
        }else if (thisExpression.equalsIgnoreCase("ontop floor")){
            Command command = new Command();
            Entity block = entities.get("X");
            command.setLocation(block.getStack());
            command.addRelationBlock(block);
            command.setDirection(Direction.here);
            return command;
        } else if (thisExpression.equalsIgnoreCase("take floor")){
            ExitWithError("Cannot take the floor");
        } else if (thisExpression.equalsIgnoreCase("under floor")){
            ExitWithError("Cannot put anything under the floor");
        }

        return null;  // we should never ever get here
    }

    private Command ParseLocation(Expression expression, Direction direction) throws ParserErrorException {
        // a location is the/any/all
        Command command = new Command();
        if( expression.getSubExpressionOne().thisCommand.equalsIgnoreCase("thatis"))
            return ParseExpression(expression.getSubExpressionOne());
        command.setDirection(direction);
        String thisExpression = expression.getThisCommand();
        String[] block = expression.getSubExpressionOne().getThisCommand().split(" ");
        ArrayList<Entity> blocks = FindBlocks(block[2], block[3], block[1]);
        if (thisExpression.equalsIgnoreCase("all")) {
            command.setRelationBlocksDeterminer("all");
            if (direction.equals(Direction.left)) {
                ArrayList<Entity> temp = new ArrayList<Entity>();
                temp.add(blocks.get(0));
                command.setLocation(blocks.get(0).getStack());
                command.setRelationBlocks(temp);
            } else if (direction.equals(Direction.here)) {
                ExitWithError("It is not possibly to put a block inside/on top of  \"all\" of something");
            } else if (direction.equals(Direction.right)) {
                ArrayList<Entity> temp = new ArrayList<Entity>();
                temp.add(blocks.get(blocks.size() - 1));
                command.setLocation(blocks.get(blocks.size() - 1).getStack());
                command.setRelationBlocks(temp);
            } else if (direction.equals(Direction.under) || direction.equals(Direction.here)) {
                command.setRelationBlocks(blocks);
            }
        } else {
            if(thisExpression.equalsIgnoreCase("the"))
                command.setRelationBlocksDeterminer("the");
            else
                command.setRelationBlocksDeterminer("any");
            if(thisExpression.equalsIgnoreCase("the") && blocks.size() > 1)
            {
                String errorString = "There was more than one choice: did you mean ";
                for(Entity temp : blocks)
                {
                    errorString += BlockString(temp) + " or ";
                }
                errorString = errorString.substring(0,errorString.length()-4);
                ExitWithError(errorString);
            }
            if (direction.equals(Direction.left)) {
                ArrayList<Entity> temp = new ArrayList<Entity>();
                temp.add(blocks.get(blocks.size() - 1));
                command.setRelationBlocks(temp);
                command.setLocation(blocks.get(blocks.size() - 1).getStack());
            } else if (direction.equals(Direction.here)) {
                command.setDirection(Direction.here);
                if (block[1].equals("pyramid") || block[1].equals("ball"))
                    ExitWithError("Can not put objects on/ balls or pyramids");

                command.setRelationBlocks(blocks);
            } else if (direction.equals(Direction.right)) {
                ArrayList<Entity> temp = new ArrayList<Entity>();
                temp.add(blocks.get(0));
                command.setLocation(blocks.get(0).getStack());
                command.setRelationBlocks(temp);
            } else if (direction.equals(Direction.under)) {
                command.setDirection(Direction.under);
                if(thisExpression.equalsIgnoreCase("the"))
                    command.setRelationBlocksDeterminer("the");
                else
                    command.setRelationBlocksDeterminer("any");
                command.setRelationBlocks(blocks);
            }
        }
        return command;
    }


    private ArrayList<Entity> FindBlocks(String size, String colour, String kind) {
        ArrayList<Entity> possibilities = new ArrayList<Entity>(entities.values());

        if(kind.equalsIgnoreCase("floor"))
        {
            possibilities.clear();
            possibilities.add(entities.get("X"));
            return possibilities;
        }
        if (kind.equalsIgnoreCase("pyramid"))
            possibilities = RemoveShape(possibilities, Shape.pyramid);
        else if (kind.equalsIgnoreCase("box"))
            possibilities = RemoveShape(possibilities, Shape.box);
        else if (kind.equalsIgnoreCase("ball"))
            possibilities = RemoveShape(possibilities, Shape.ball);
        else if (kind.equalsIgnoreCase("rectangle"))
            possibilities = RemoveShape(possibilities, Shape.rectangle);
        else if (kind.equalsIgnoreCase("square"))
            possibilities = RemoveShape(possibilities, Shape.square);

        if (colour.equalsIgnoreCase("red"))
            possibilities = RemoveColour(possibilities, Colour.red);
        else if (colour.equalsIgnoreCase("black"))
            possibilities = RemoveColour(possibilities, Colour.black);
        else if (colour.equalsIgnoreCase("blue"))
            possibilities = RemoveColour(possibilities, Colour.blue);
        else if (colour.equalsIgnoreCase("green"))
            possibilities = RemoveColour(possibilities, Colour.green);
        else if (colour.equalsIgnoreCase("yellow"))
            possibilities = RemoveColour(possibilities, Colour.yellow);
        else if (colour.equalsIgnoreCase("white"))
            possibilities = RemoveColour(possibilities, Colour.white);

        if (size.equalsIgnoreCase("large"))
            possibilities = RemoveSize(possibilities, Size.large);
        else if (size.equalsIgnoreCase("medium"))
            possibilities = RemoveSize(possibilities, Size.medium);
        else if (size.equalsIgnoreCase("small"))
            possibilities = RemoveSize(possibilities, Size.small);
        else if (size.equalsIgnoreCase("tall"))
            possibilities = RemoveSize(possibilities, Size.tall);
        else if (size.equalsIgnoreCase("wide"))
            possibilities = RemoveSize(possibilities, Size.wide);

        return possibilities;

    }

    private ArrayList<Entity> RemoveShape(ArrayList<Entity> possibilities, Shape shape) {
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getShape().equals(shape))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private ArrayList<Entity> RemoveColour(ArrayList<Entity> possibilities, Colour colour) {
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getColour().equals(colour))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private ArrayList<Entity> RemoveSize(ArrayList<Entity> possibilities, Size size) {
        ArrayList<Entity> possibilitiesLeft = new ArrayList<Entity>();
        for (Entity possibility : possibilities)
            if (possibility.getSize().equals(size))
                possibilitiesLeft.add(possibility);
        return possibilitiesLeft;
    }

    private void ExitWithError(String errorMessage) throws ParserErrorException {
        throw new ParserErrorException(errorMessage);
    }

    private String CheckRules(Entity blockOne, Entity blockTwo, Direction direction) {
        Shape shape = blockOne.getShape();
        String errorString = "Ok";
        if (direction == Direction.under) {
            if (shape == Shape.floor)
                errorString = "Can not put anything under the floor";
            if ((shape.equals(Shape.pyramid) || shape.equals(Shape.ball)))
                errorString = "Can not place pyramids or balls under anything.";
            if (blockOne.getSize().getValue() < blockTwo.getSize().getValue())
                errorString = "Can not place something smaller underneath something bigger";
            if (blockTwo.getShape() == Shape.box && !(blockOne.getSize().getValue() > blockTwo.getSize().getValue() ))
                errorString = "Can only place something strictly bigger under boxes";
        } else if (direction == Direction.here) {
            if (!(blockOne.getSize().getValue() <= blockTwo.getSize().getValue()))
                errorString = "Cannot put something bigger on/inside something smaller";
            if (blockOne.getShape() == Shape.box && !(blockOne.getSize().getValue() > blockTwo.getSize().getValue()))
                errorString = "A box has can only be put on something strictly bigger";
        }
        if (direction != Direction.here && blockTwo == entities.get("X")) {
            errorString = "Can not do anything with the floor except put objects on it";
        }
        return errorString;
    }

    private Entity FindMostAccesibleBlock(ArrayList<Entity> blocks)
    {
        Entity mostAccesibleBlock = blocks.get(0);
        int blocksOverIt = Integer.MAX_VALUE;
        for (Entity current : blocks) {
            if (world[current.getStack()].length() - 1 - current.getIndexInStack() < blocksOverIt) {
                mostAccesibleBlock = current;
                blocksOverIt = world[current.getStack()].length() - 1 - current.getIndexInStack();
            }
        }

        return mostAccesibleBlock;
    }

    private String BlockString(Entity block)
    {
        return " the " + block.getSize() + " " + block.getColour() + " " + block.getShape();
    }
}
