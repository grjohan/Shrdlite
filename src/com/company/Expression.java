package com.company;

/**
 * Created with IntelliJ IDEA.
 * User: Johan
 * Date: 2013-05-08
 * Time: 07:15
 * To change this template use File | Settings | File Templates.
 */
public class Expression {
    String thisCommand;
    Expression subExpressionOne;
    Expression subExpressionTwo;
    public String getThisCommand() {
        return thisCommand;
    }

    public void AppendThisCommand(String thisCommand) {
        if (this.thisCommand.length() != 0)
            thisCommand = " " + thisCommand;
        this.thisCommand += thisCommand;
    }

    public Expression getSubExpressionOne() {
        return subExpressionOne;
    }

    public void setSubExpressionOne(Expression subExpression) {
        this.subExpressionOne = subExpression;
    }



    public Expression getSubExpressionTwo() {
        return subExpressionTwo;
    }

    public void setSubExpressionTwo(Expression subExpressionTwo) {
        this.subExpressionTwo = subExpressionTwo;
    }



    public Expression()
    {
        thisCommand = "";
    }

}
