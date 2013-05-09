package com.company;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-07
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public class Parser {
    private String[] tokens;
    int atIndex;
    public Parser(String tree)
    {
        tree = tree.substring(1,tree.lastIndexOf(')'));
        tokens = tree.split(" ");
        atIndex = 0;
    }

    public Expression MakeExpressions()
    {
        if (Peek().equals(""))
            NextToken();
        Expression expression = new Expression();
        while (!(Peek().equals("(") || Peek().equals(")")))
        {
            expression.AppendThisCommand(NextToken());
        }
        String temp = NextToken();
        if(temp.equals("("))
            expression.setSubExpressionOne(MakeExpressions());
        else if(temp.equals(")"))
        {
            return expression;
        }
        if(HasNext())
        {
            if (Peek().equals("("))
            {
                NextToken();
                expression.setSubExpressionTwo(MakeExpressions());
            }
            if (Peek().equals(")"))
            {
                NextToken();
                return expression;
            }
        }


        return expression;
    }

    public String NextToken()
    {
        String temp = tokens[atIndex];
        atIndex++;
        return  temp;
    }

    public String Peek()
    {
        return tokens[atIndex];
    }

    public Boolean HasNext()
    {
        return atIndex < tokens.length -1;
    }

}
