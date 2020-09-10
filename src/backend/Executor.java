/**
 * Executor class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package backend;

import java.util.ArrayList;
import java.util.HashSet;

import intermediate.*;
import static intermediate.Node.NodeType.*;

public class Executor
{
    private int lineNumber;
    private Symtab symtab;
    
    private static HashSet<Node.NodeType> singletons;
    private static HashSet<Node.NodeType> relationals;
    
    static
    {
        singletons  = new HashSet<Node.NodeType>();
        relationals = new HashSet<Node.NodeType>();
        
        singletons.add(VARIABLE);
        singletons.add(INTEGER_CONSTANT);
        singletons.add(REAL_CONSTANT);
        singletons.add(STRING_CONSTANT);
        singletons.add(CHARACTER_CONSTANT);
        
        relationals.add(EQ);
        relationals.add(LT);
        relationals.add(GT);
    }
    
    public Executor(Symtab symtab)
    {
        this.symtab = symtab;
    }
    
    public Object visit(Node node)
    {
        switch (node.type)
        {
            case PROGRAM :  return visitProgram(node);
            
            case COMPOUND : 
            case ASSIGN :   
            case LOOP : 
            case WRITE :
            case WRITELN :  
            case CASE:      return visitStatement(node);
            
            case TEST:      return visitTest(node);

            
            default :       return visitExpression(node);
        }
    }
    
    private Object visitProgram(Node programNode)
    {
        Node compoundNode = programNode.children.get(0);
        return visit(compoundNode);
    }
    
    private Object visitStatement(Node statementNode)
    {
        lineNumber = statementNode.lineNumber;
        
        switch (statementNode.type)
        {
            case COMPOUND :  return visitCompound(statementNode);
            case ASSIGN :    return visitAssign(statementNode);
            case LOOP :      return visitLoop(statementNode);
            case WRITE :     return visitWrite(statementNode);
            case WRITELN :   return visitWriteln(statementNode);
            case CASE  :     return visitCase(statementNode);
            default :        return null;
        }
    }
    
    private Object visitCompound(Node compoundNode)
    {
        for (Node statementNode : compoundNode.children) visit(statementNode);
        
        return null;
    }
    
    private Object visitAssign(Node assignNode)
    {
        Node lhs = assignNode.children.get(0);
        Node rhs = assignNode.children.get(1);
        
        // Evaluate the right-hand-side expression;
        Double value = (Double) visit(rhs);
        
        // Store the value into the variable's symbol table entry.
        String variableName = lhs.text;
        SymtabEntry variableId = symtab.lookup(variableName);
        variableId.setValue(value);
        
        return null;
    }
    
    
    
    
    private Object visitLoop(Node loopNode)
    {        
        boolean b = false;
        do
        {
            for (Node node : loopNode.children)
            {
                Object value = visit(node);  // statement or test
                
             
                // Evaluate the test condition. Stop looping if true.
                b = (node.type == TEST) && !((boolean) value);             
                if (b) 
                    break;
            }
        } while (!b);
        
        return null;
    }
    
    private Object visitTest(Node testNode)
    {
        return (Boolean) visit(testNode.children.get(0));
    }

    
    private Object visitWrite(Node writeNode)
    {
        printValue(writeNode.children);
        return null;
    }
    
    private Object visitWriteln(Node writelnNode)
    {
        if (writelnNode.children.size() > 0) printValue(writelnNode.children);
        System.out.println();
        
        return null;
    }

    private void printValue(ArrayList<Node> children)
    {
        long fieldWidth    = -1;
        long decimalPlaces = 0;
        
        // Use any specified field width and count of decimal places.
        if (children.size() > 1)
        {
            double fw = (Double) visit(children.get(1));
            fieldWidth = (long) fw;
            
            if (children.size() > 2) 
            {
                double dp = (Double) visit(children.get(2));
                decimalPlaces = (long) dp;
            }
        }
        
        // Print the value with a format.
        Node valueNode = children.get(0);
        if (valueNode.type == VARIABLE)
        {
            String format = "%";
            if (fieldWidth >= 0)    format += fieldWidth;
            if (decimalPlaces >= 0) format += "." + decimalPlaces;
            format += "f";
            
            Double value = (Double) visit(valueNode);
            System.out.printf(format, value);
        }
        else  // node type STRING_CONSTANT or CHARACTER_CONSTANT
        {
            String format = "%";
            if (fieldWidth > 0) format += fieldWidth;
            format += "s";
            
            String value = "" +  visit(valueNode);
            System.out.printf(format, value);
        }
    }

    private Object visitExpression(Node expressionNode)
    {
        // Single-operand expressions.
        if (singletons.contains(expressionNode.type))
        {
            switch (expressionNode.type)
            {
                case VARIABLE         : return visitVariable(expressionNode);
                case INTEGER_CONSTANT : return visitIntegerConstant(expressionNode);
                case REAL_CONSTANT    : return visitRealConstant(expressionNode);
                case STRING_CONSTANT  : return visitStringConstant(expressionNode);
                case CHARACTER_CONSTANT: return visitCharacterConstant(expressionNode);
                
                default: return null;
            }
        }
        
        // Binary expressions.
        double value1 = (Double) visit(expressionNode.children.get(0));
        double value2 = (Double) visit(expressionNode.children.get(1));
        
        // Relational expressions.
        if (relationals.contains(expressionNode.type))
        {
            boolean value = false;
            switch (expressionNode.type)
            {
                case EQ : value = value1 == value2; break;
                case LT : value = value1 <  value2; break;
                case GT : value = value1 >  value2; break;
                default : break;
            }
            
            return value;
        }
           
        double value = 0.0;
        
        // Arithmetic expressions.
        switch (expressionNode.type)
        {
            case ADD :      value = value1 + value2; break;
            case SUBTRACT : value = value1 - value2; break;
            case MULTIPLY : value = value1 * value2; break;
                
            case DIVIDE :
            {
                if (value2 != 0.0) value = value1/value2;
                else
                {
                    runtimeError(expressionNode, "Division by zero");
                    return 0.0;
                }
                
                break;
            }
            
            default : break;
        }
        
        return Double.valueOf(value);
    }
    
    private Object visitVariable(Node variableNode)
    {
        // Obtain the variable's value from its symbol table entry.
        String variableName = variableNode.text;
        SymtabEntry variableId = symtab.lookup(variableName);
        Double value = variableId.getValue();
        
        return value;
    }
    
    private Object visitIntegerConstant(Node integerConstantNode)
    {
        long value = (Long) integerConstantNode.value;
        return (double) value;
    }
    
    private Object visitRealConstant(Node realConstantNode)
    {
        return (Double) realConstantNode.value;
    }
    
    private Object visitStringConstant(Node stringConstantNode)
    {
        return (String) stringConstantNode.value;
    }

    private void runtimeError(Node node, String message)
    {
        System.out.printf("RUNTIME ERROR at line %d: %s: %s\n", 
                          lineNumber, message, node.text);
        System.exit(-2);
    }
    
    //---------------- Added in SimpleJavaV2 ----------------\\ 
    
    private Object visitCharacterConstant(Node characterConstantNode)
    {
        return (Character) characterConstantNode.value;
    }
    
    private Object visitCase(Node node)
    {
        Node exp = node.children.get(0);
        Object value = visitExpression(exp);
        
        for(int i = 1; i < node.children.size(); i++) //loops through the branches
        {
            Node branch = node.children.get(i);
            Node constList = branch.children.get(0);
            
            for(Node constant: constList.children) //loops through the constants in the constant list
            {
                if(value.equals(visitConstant(constant))) //checks if a value matches a constant
                {
                    if(branch.children.size() > 1) //ensures that the branch node has a statement
                    {
                        visitStatement(branch.children.get(1));
                        return null;
                    }
                }
            }
            
        }
        
        return null;
    }
    
    private Object visitConstant(Node node)
    {
        Node child0 = node.children.get(0);
        
        switch(child0.type)
        {
            case STRING_CONSTANT:    return visitStringConstant(child0);
            case CHARACTER_CONSTANT: return visitCharacterConstant(child0);
            case INTEGER_CONSTANT:   return visitIntegerConstant(child0);
            case REAL_CONSTANT:      return visitRealConstant(child0);
            case VARIABLE:           return visitVariable(child0);
            case POSITIVE:
            case NEGATIVE: {
                
                int sign = (child0.type == POSITIVE)? 1: -1; //+1 if there was a + token, -1 if it there was a - token
                Node child1 = node.children.get(1);
                
                switch(child1.type)
                {
                    case INTEGER_CONSTANT:   return sign * (Integer) visitIntegerConstant(child0);
                    case REAL_CONSTANT:      return sign * (Double) visitRealConstant(child0);
                    case VARIABLE:           return sign * (Double) visitVariable(child0);
                }
            }
                
            default: return null;
        }
    }
}
