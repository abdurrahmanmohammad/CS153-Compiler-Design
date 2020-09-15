/**
 * Parse tree node class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package intermediate;

import java.util.ArrayList;

public class Node
{
    public enum NodeType
    {
        PROGRAM, COMPOUND, ASSIGN, LOOP, TEST, NOT, WRITE, WRITELN,
        IF, ELSE, CASE, CASE_BRANCH, CONSTANT_LIST, CONSTANT,AND,OR,
        POSITIVE, NEGATIVE, ADD, SUBTRACT, MULTIPLY, DIVIDE,DIV, EQ, NE, LT, GT, LE, GE,
        VARIABLE, INTEGER_CONSTANT, REAL_CONSTANT, STRING_CONSTANT, CHARACTER_CONSTANT
    }

    public NodeType type;
    public int lineNumber;
    public String text;
    public SymtabEntry entry;
    public Object value;
    public ArrayList<Node> children;
    
    public Node(NodeType type)
    {
        this.type = type;
        this.lineNumber = 0;
        this.text = null;
        this.value = null;
        this.children = new ArrayList<Node>();
    }
    
    public void adopt(Node child) { children.add(child); }
}