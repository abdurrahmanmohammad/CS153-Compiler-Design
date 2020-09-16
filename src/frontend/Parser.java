/**
 * Parser class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

import java.util.HashSet;

import frontend.Token.TokenType;
import intermediate.*;
import static frontend.Token.TokenType.*;
import static intermediate.Node.NodeType.*;

public class Parser
{
    private Scanner scanner;
    private Symtab symtab;
    private Token currentToken;
    private int lineNumber;
    private int errorCount;
    
    public Parser(Scanner scanner, Symtab symtab)
    {
        this.scanner = scanner;
        this.symtab  = symtab;
        this.currentToken = null;
        this.lineNumber = 1;
        this.errorCount = 0;
    }
    
    public int errorCount() { return errorCount; }
    
    public Node parseProgram()
    {
        Node programNode = new Node(Node.NodeType.PROGRAM);
        
        currentToken = scanner.nextToken();  // first token!
        
        if (currentToken.type == Token.TokenType.PROGRAM) 
        {
            currentToken = scanner.nextToken();  // consume PROGRAM
        }
        else syntaxError("Expecting PROGRAM");
        
        if (currentToken.type == IDENTIFIER) 
        {
            String programName = currentToken.text;
            symtab.enter(programName);
            programNode.text = programName;
            
            currentToken = scanner.nextToken();  // consume program name
        }
        else syntaxError("Expecting program name");
        
        if (currentToken.type == SEMICOLON) 
        {
            currentToken = scanner.nextToken();  // consume ;
        }
        else syntaxError("Missing ;");
        
        if (currentToken.type != BEGIN) syntaxError("Expecting BEGIN");
        
        // The PROGRAM node adopts the COMPOUND tree.
        programNode.adopt(parseCompoundStatement());
        
        if (currentToken.type != PERIOD) syntaxError("Expecting .");
        
        
        return programNode;
    }
    
    private static HashSet<Token.TokenType> statementStarters;
    private static HashSet<Token.TokenType> statementFollowers;
    private static HashSet<Token.TokenType> relationalOperators;
    private static HashSet<Token.TokenType> simpleExpressionOperators;
    private static HashSet<Token.TokenType> termOperators;

    static
    {
        statementStarters = new HashSet<Token.TokenType>();
        statementFollowers = new HashSet<Token.TokenType>();
        relationalOperators = new HashSet<Token.TokenType>();
        simpleExpressionOperators = new HashSet<Token.TokenType>();
        termOperators = new HashSet<Token.TokenType>();
        
        // Tokens that can start a statement.
        statementStarters.add(BEGIN);
        statementStarters.add(IDENTIFIER);
        statementStarters.add(REPEAT);
        statementStarters.add(DO);
        statementStarters.add(Token.TokenType.WRITE);
        statementStarters.add(Token.TokenType.WRITELN);
        statementStarters.add(Token.TokenType.CASE);
        statementStarters.add(Token.TokenType.IF);
        statementStarters.add(Token.TokenType.WHILE);
        
        // Tokens that can immediately follow a statement.
        statementFollowers.add(SEMICOLON);
        statementFollowers.add(PERIOD);
        
        statementFollowers.add(END);
        statementFollowers.add(UNTIL);
        statementFollowers.add(END_OF_FILE);
        
        relationalOperators.add(EQUALS);
        relationalOperators.add(NOT_EQUALS);
        relationalOperators.add(LESS_THAN);
        relationalOperators.add(GREATER_THAN);
        relationalOperators.add(LESS_EQUALS);
        relationalOperators.add(GREATER_EQUALS);
        
       
        simpleExpressionOperators.add(PLUS);
        simpleExpressionOperators.add(MINUS);
        simpleExpressionOperators.add(Token.TokenType.OR);

        
        termOperators.add(STAR);
        termOperators.add(SLASH);
        termOperators.add(Token.TokenType.DIV);
        termOperators.add(Token.TokenType.MOD);
        termOperators.add(Token.TokenType.AND);
    }
    
    private Node parseStatement()
    {
        Node stmtNode = null;
        int savedLineNumber = currentToken.lineNumber;
        lineNumber = savedLineNumber;
        
        switch (currentToken.type)
        {
            case IDENTIFIER : stmtNode = parseAssignmentStatement(); break;
            case BEGIN :      stmtNode = parseCompoundStatement();   break;
            case WHILE :      stmtNode = parseWhileStatement();      break;  
            case FOR :      stmtNode = parseForStatement();      break;
            case REPEAT :     stmtNode = parseRepeatStatement();     break;
            case WRITE :      stmtNode = parseWriteStatement();      break;
            case WRITELN :    stmtNode = parseWritelnStatement();    break;
            case CASE  :      stmtNode = parseCaseStatement();       break;
            case IF    :      stmtNode = parseIfStatement();         break;
            case SEMICOLON :  stmtNode = null; break;  // empty statement
            
            default : syntaxError("Unexpected token");
        }
        
        if (stmtNode != null) stmtNode.lineNumber = savedLineNumber;
        return stmtNode;
    }
    
    private Node parseAssignmentStatement()
    {
        // The current token should now be the left-hand-side variable name.
        
        Node assignmentNode = new Node(ASSIGN);
        
        // The assignment node adopts the variable node as its first child.
        Node lhsNode = new Node(VARIABLE);
        String variableName = currentToken.text;
        SymtabEntry variableId = symtab.enter(variableName.toLowerCase());
        
        lhsNode.text  = variableName;
        lhsNode.entry = variableId;
        assignmentNode.adopt(lhsNode);
        
        currentToken = scanner.nextToken();  // consume the LHS variable;
        
        if (currentToken.type == COLON_EQUALS) 
        {
            currentToken = scanner.nextToken();  // consume :=
        }
        else syntaxError("Missing :=");
        
        // The assignment node adopts the expression node as its second child.
        Node rhsNode = parseExpression();
        assignmentNode.adopt(rhsNode);
        
        return assignmentNode;
    }
    
    private Node parseCompoundStatement()
    {
        Node compoundNode = new Node(COMPOUND);
        compoundNode.lineNumber = currentToken.lineNumber;
        
        currentToken = scanner.nextToken();  // consume BEGIN
        parseStatementList(compoundNode, END);    
        
        if (currentToken.type == END) 
        {
            currentToken = scanner.nextToken();  // consume END
        }
        else syntaxError("Expecting END");
        
        return compoundNode;
    }
    
    private void parseStatementList(Node parentNode, Token.TokenType terminalType)
    {
        
        while ((currentToken.type != terminalType) && (currentToken.type != END_OF_FILE))
        {
            Node stmtNode = parseStatement();
            if (stmtNode != null) parentNode.adopt(stmtNode);
            // A semicolon separates statements.
            
            if (currentToken.type == SEMICOLON)
            {
                while (currentToken.type == SEMICOLON)
                {
                    currentToken = scanner.nextToken();  // consume ;                 
                    lineNumber = currentToken.lineNumber;
                }
            }
            else if (statementStarters.contains(currentToken.type))
            {
                syntaxError("Missing ;");
            }
        }
        
        
    }

    private Node parseRepeatStatement()
    {
        // The current token should now be REPEAT.
        
        // Create a LOOP node.
        Node loopNode = new Node(LOOP);
        currentToken = scanner.nextToken();  // consume REPEAT
        
        parseStatementList(loopNode, UNTIL);    
        
        if (currentToken.type == UNTIL) 
        {
            // Create a TEST node. It adopts the test expression node.
            Node testNode = new Node(TEST);       
            lineNumber = currentToken.lineNumber;
            testNode.lineNumber = lineNumber;
            currentToken = scanner.nextToken();  // consume UNTIL
            
            testNode.adopt(parseExpression());
            
            // The LOOP node adopts the TEST node as its final child.
            loopNode.adopt(testNode);
        }
        else syntaxError("Expecting UNTIL");
        
        return loopNode;
    }
    
    //-----------------------------------------------------
    
    private Node parseForStatement()
    {
        // The current token should now be FOR.
    	currentToken = scanner.nextToken(); // consume FOR (FOR ensured by caller function)
    	
    	// Create a COMPOUND node. (COMPOUND = ASSIGN + LOOP)
    	Node compoundNode = new Node(COMPOUND);
    	
    	// Parse assignment statement 
    	Node assign = parseAssignmentStatement(); // Create ASSIGN node to store assignment statement: i := 0
    	Node loopVar = assign.children.get(0); // Extract loop variable for later use
    	compoundNode.adopt(assign); // Add ASSIGN node to COMPOUND node
    	
        // Create a LOOP node. (LOOP = TEST + statement(s) + ASSIGN)
        Node loopNode = new Node(LOOP);
        
        // Parse test condition: TEST node adopts the GT node for TO and LT node for DOWNTO.
        Node testNode = new Node(TEST); // Create TEST node
        // Get next token (Should be TO or DOWNTO)
        Token.TokenType loopType = null; // Stores the token of the FOR loop type
        Node operator = null; // Stores the operator associated with TO (GT) and DOWNTO (LT)
    	// Determine if the for loop is of type TO or DOWNTO
    	if(currentToken.type == TO) {
    		loopType = TO;
    		operator = new Node(GT);
    	} else if(currentToken.type == DOWNTO) {
    		loopType = DOWNTO;
    		operator = new Node(LT);
    	} else {
    		syntaxError("Expecting TO or DOWNTO"); // If not TO or DOWNTO, invalid token
    		loopType = null;
    		if(currentToken.type != DO)
    			scanner.nextToken(); // Skip the next token (token after TO or DOWNTO but before DO) 
    	}
    	currentToken = scanner.nextToken(); // Consume TO or DOWNTO
        
    	// Parse and store test expression: GT or LT
    	if(loopType != null) {
        	operator.adopt(loopVar); // Reuse variable
            operator.adopt(parseIntegerConstant()); // Parse integer constant
            testNode.adopt(operator); // Add GT or LT test to TEST
    	}

        
        lineNumber = currentToken.lineNumber; // Retrieve line number
        testNode.lineNumber = lineNumber; // Set line number for TEST node
        loopNode.adopt(testNode); // Add TEST node to LOOP node
    	
        // Consume DO
        if(currentToken.type == DO) currentToken = scanner.nextToken();
        else syntaxError("Expecting DO"); // If not DO, invalid token
        
        // Parse statements
        if(currentToken.type == BEGIN) { // If loop has a block of statements
        	currentToken = scanner.nextToken(); // consume BEGIN
        	parseStatementList(loopNode, END); // Consume statements in block
        	currentToken = scanner.nextToken(); // Consume END
        } else loopNode.adopt(parseStatement()); // If loop is a single statement
        
        // Increment or decrement loop counter: i := i + 1
        Node postAssign = new Node(ASSIGN); // Create ASSIGN node for loop counter
        postAssign.adopt(loopVar); // Store the loop variable: i := i + 1
        // TO: add and DOWNTO: subtract
        Node increment = loopType == TO ? new Node(ADD)
        				: new Node(SUBTRACT);
        increment.adopt(loopVar); // The i in i + 1
        // Store a one to increment or decrement by 1
        Node integerNode = new Node(INTEGER_CONSTANT);
        integerNode.value = 1L; // Store 1 as a long since we use long to process values
        increment.adopt(integerNode); // Add this 1 to the ADD or SUBTRACT node
        
        postAssign.adopt(increment); // Add ADD or SUBTRACT node to LOOP node
        loopNode.adopt(postAssign); // Add ADD or SUBTRACT node to LOOP node
        
        
        compoundNode.adopt(loopNode);
        
        return compoundNode;
    }
    
    private Node parseWhileStatement()
    {
        // The current token should now be while.
        
        // Create a LOOP node.
        Node loopNode = new Node(LOOP);
 
        if (currentToken.type == WHILE) 
        {
            currentToken = scanner.nextToken();  // consume WHILE
            Node testNode = new Node(TEST);
            Node notNode = new Node(Node.NodeType.NOT);
            lineNumber = currentToken.lineNumber;
            testNode.lineNumber = lineNumber;
            // Create a TEST node. It adopts the NOT node which adopts the expression node.             
            testNode.adopt(notNode);
            notNode.adopt(parseExpression());
            // The LOOP node adopts the TEST node as its final child.
            loopNode.adopt(testNode);
        }
            if (currentToken.type == DO) 
            {
                currentToken = scanner.nextToken();  // consume DO    
                lineNumber = currentToken.lineNumber;
               
            }
            else syntaxError("Expecting DO");
            
            loopNode.adopt(parseStatement());
            
        return loopNode;
    }
    
    private Node parseWriteStatement()
    {
        // The current token should now be WRITE.
        
        // Create a WRITE node. It adopts the variable or string node.
        Node writeNode = new Node(Node.NodeType.WRITE);
        currentToken = scanner.nextToken();  // consume WRITE
        
        parseWriteArguments(writeNode);
        if (writeNode.children.size() == 0)
        {
            syntaxError("Invalid WRITE statement");
        }
        
        return writeNode;
    }
    
    private Node parseWritelnStatement()
    {
        // The current token should now be WRITELN.
        
        // Create a WRITELN node. It adopts the variable or string node.
        Node writelnNode = new Node(Node.NodeType.WRITELN);
        currentToken = scanner.nextToken();  // consume WRITELN
        
        if (currentToken.type == LPAREN) parseWriteArguments(writelnNode);
        return writelnNode;
    }
    
    private void parseWriteArguments(Node node)
    {
        // The current token should now be (
        
        boolean hasArgument = false;
        
        if (currentToken.type == LPAREN) 
        {
            currentToken = scanner.nextToken();  // consume (
        }
        else syntaxError("Missing left parenthesis");
        
        if (currentToken.type == IDENTIFIER)
        {
            node.adopt(parseVariable());
            hasArgument = true;
        }
        else if(currentToken.type == CHARACTER)
        {
            node.adopt(parseCharacterConstant());
            hasArgument = true;
        }
        else if (currentToken.type == STRING)
        {
            node.adopt(parseStringConstant());
            hasArgument = true;
        }
        else syntaxError("Invalid WRITE or WRITELN statement");
        
        // Look for a field width and a count of decimal places.
        if (hasArgument)
        {
            if (currentToken.type == COLON) 
            {
                currentToken = scanner.nextToken();  // consume ,
                
                if (currentToken.type == INTEGER)
                {
                    // Field width
                    node.adopt(parseIntegerConstant());
                    
                    if (currentToken.type == COLON) 
                    {
                        currentToken = scanner.nextToken();  // consume ,
                        
                        if (currentToken.type == INTEGER)
                        {
                            // Count of decimal places
                            node.adopt(parseIntegerConstant());
                        }
                        else syntaxError("Invalid count of decimal places");
                    }
                }
                else syntaxError("Invalid field width");
            }
        }
        
        if (currentToken.type == RPAREN) 
        {
            currentToken = scanner.nextToken();  // consume )
        }
        else syntaxError("Missing right parenthesis");
    }

    private Node parseExpression()
    {
        // The current token should now be an identifier or a number.
        
        // The expression's root node.
        Node exprNode = parseSimpleExpression();
        
        // The current token might now be a relational operator.
        if (relationalOperators.contains(currentToken.type))
        {
            Token.TokenType tokenType = currentToken.type;
            Node opNode = tokenType == EQUALS    ? new Node(EQ)
                        : tokenType == NOT_EQUALS? new Node(NE)
                        : tokenType == LESS_THAN ? new Node(LT)
                        : tokenType == GREATER_THAN ? new Node(GT)
                        : tokenType == LESS_EQUALS ? new Node(LE)
                        : tokenType == GREATER_EQUALS? new Node(GE)
                        :                          null;
            
            currentToken = scanner.nextToken();  // consume relational operator
            
            // The relational operator node adopts the first simple expression
            // node as its first child and the second simple expression node
            // as its second child. Then it becomes the expression's root node.
            if (opNode != null)
            {
                opNode.adopt(exprNode);
                opNode.adopt(parseSimpleExpression());
                exprNode = opNode;
            }
        }
        
        return exprNode;
    }
    
    private Node parseSimpleExpression()
    {
        // The current token should now be an identifier, number or a + or - token (unary operators).
        
        // The simple expression's root node.
        Node simpExprNode = null;
        
        switch(currentToken.type)
        {
            case PLUS: 
                simpExprNode = new Node(POSITIVE);
                currentToken = scanner.nextToken(); //consume + token
                simpExprNode.adopt(parseTerm()); //adopt the term that follows the + token
                break;
            case MINUS:          
                simpExprNode = new Node(NEGATIVE);
                currentToken = scanner.nextToken(); //consume - token
                simpExprNode.adopt(parseTerm()); //adopt the term that follows the - token
                break;
            
            default: simpExprNode = parseTerm(); //if there isn't a + or - unary operator, just parse the term.
        }
        
        // Keep parsing more terms as long as the current token
        // is a + or - operator.
        while (simpleExpressionOperators.contains(currentToken.type))
        {
            Node opNode = currentToken.type == Token.TokenType.OR ? new Node(Node.NodeType.OR)
            			: currentToken.type == PLUS ? new Node(ADD)
                        : new Node(SUBTRACT);
            
            currentToken = scanner.nextToken();  // consume the operator

            // The add or subtract node adopts the first term node as its
            // first child and the next term node as its second child. 
            // Then it becomes the simple expression's root node.
            opNode.adopt(simpExprNode);
            opNode.adopt(parseTerm());
            simpExprNode = opNode;
        }
        
        return simpExprNode;
    }
    
    private Node parseTerm()
    {
        // The current token should now be an identifier or a number.
        
        // The term's root node.
        Node termNode = parseFactor();
        
        // Keep parsing more factor as long as the current token
        // is a * or / operator.
        while (termOperators.contains(currentToken.type))
        {
            Node opNode = currentToken.type == STAR                 ? new Node(MULTIPLY) //current token is a *       
                        : currentToken.type == SLASH                ? new Node(DIVIDE)   //current token is a /
                        : currentToken.type == Token.TokenType.DIV  ? new Node(Node.NodeType.DIV)  //current token is DIV
                        : currentToken.type == Token.TokenType.AND  ? new Node(Node.NodeType.AND)  //current token is AND
                        :                                             new Node(Node.NodeType.MOD); //current token is MOD (by default)
            
            currentToken = scanner.nextToken();  // consume the operator

            // The operation node adopts the first factor node as its
            // as its first child and the next factor node as its second child. 
            // Then it becomes the term's root node.
            opNode.adopt(termNode);
            opNode.adopt(parseFactor());
            termNode = opNode;
        }
        
        return termNode;
    }
    
    private Node parseFactor()
    {   
        // The current token should now be an identifier or a number or (
       
        if      (currentToken.type == IDENTIFIER) return parseVariable();
        else if (currentToken.type == INTEGER)    return parseIntegerConstant();
        else if (currentToken.type == REAL)       return parseRealConstant();
        
        else if (currentToken.type == LPAREN)
        {
            currentToken = scanner.nextToken();  // consume (
            Node exprNode = parseExpression();
            
            if (currentToken.type == RPAREN)
            {
                currentToken = scanner.nextToken();  // consume )
            }
            else syntaxError("Expecting )");
            
            return exprNode;
        }
        
        else if (currentToken.type == Token.TokenType.NOT)
        {
            Node notNode = new Node(Node.NodeType.NOT);
            currentToken = scanner.nextToken(); //consume the NOT
            
            notNode.adopt(parseFactor()); //per the PASCAL syntax diagram, a factor follows a NODE
            
            return notNode;
        }
        
        else syntaxError("Unexpected token");
        return null;
    }
    
    private Node parseVariable()
    {
        // The current token should now be an identifier.
        
        String variableName = currentToken.text;
        SymtabEntry variableId = symtab.lookup(variableName.toLowerCase());
        
        if (variableId == null) semanticError("Undeclared identifier");
        
        Node node = new Node(VARIABLE);
        node.text = variableName;
        
        currentToken = scanner.nextToken();  // consume the identifier        
        return node;
    }

    private Node parseIntegerConstant()
    {
        // The current token should now be a number.
        
        Node integerNode = new Node(INTEGER_CONSTANT);
        integerNode.value = currentToken.value;
        
        currentToken = scanner.nextToken();  // consume the number        
        return integerNode;
    }

    private Node parseRealConstant()
    {
        // The current token should now be a number.
        
        Node realNode = new Node(REAL_CONSTANT);
        realNode.value = currentToken.value;
        
        currentToken = scanner.nextToken();  // consume the number        
        return realNode;
    }
    
    private Node parseStringConstant()
    {
        // The current token should now be STRING.
        
        Node stringNode = new Node(STRING_CONSTANT);
        stringNode.value = currentToken.value;
        
        currentToken = scanner.nextToken();  // consume the string        
        return stringNode;
    }
    
    private void syntaxError(String message)
    {
        System.out.println("SYNTAX ERROR at line " + lineNumber 
                           + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;
        
        // Recover by skipping the rest of the statement.
        // Skip to a statement follower token.
        while (! statementFollowers.contains(currentToken.type))
        {
            currentToken = scanner.nextToken();
        }
    }
    
    private void semanticError(String message)
    {
        System.out.println("SEMANTIC ERROR at line " + lineNumber 
                           + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;
    }
    
    //---------------- Added in SimpleJavaV2 ----------------\\ 
    
    private Node parseCharacterConstant()
    {
        // The current token should now be CHARACTER
        
        Node charNode = new Node(CHARACTER_CONSTANT);
        charNode.value = currentToken.value;
        
        currentToken = scanner.nextToken();  // consume the character        
        return charNode;
    }

    private Node parseIfStatement()
    {
        Node ifNode = new Node(Node.NodeType.IF);
        ifNode.lineNumber = currentToken.lineNumber;
        currentToken = scanner.nextToken(); //consume IF token
        
        ifNode.adopt(parseExpression()); //parses the expression after the IF statement
        
        if(currentToken.type == THEN)
        {
            currentToken = scanner.nextToken(); //consumes the THEN token
            ifNode.adopt(parseStatement()); //parses the statement that follows the THEN token
        }
        else 
            syntaxError("Expecting THEN");
        
        if(currentToken.type == Token.TokenType.ELSE) //if there is an ELSE token after the statement
        {
            currentToken = scanner.nextToken(); //consumes the ELSE token
            ifNode.adopt(parseStatement()); //parses the statement that follows the ELSE token
        }
        
        return ifNode;
    }
    
    private Node parseCaseStatement()
    {
        Node caseNode = new Node(Node.NodeType.CASE);
        caseNode.lineNumber = currentToken.lineNumber;
        currentToken = scanner.nextToken(); //Consume CASE token.
        
        caseNode.adopt(parseExpression()); //Adopts an expression node as first child node
        
        if(currentToken.type == OF)
            currentToken = scanner.nextToken(); //Consume OF token 
        else
            syntaxError("Expecting OF");            
        
        while(currentToken.type != END) //loop until we get to an END token
        {
            caseNode.adopt(parseCaseBranch()); //Adopts case branch nodes until the Parser reaches an END token
        }
        
        currentToken = scanner.nextToken(); //consume END token
        
    	return caseNode;
    }
    
    private Node parseCaseBranch()
    {
        Node caseBranchNode = new Node(Node.NodeType.CASE_BRANCH);
        caseBranchNode.lineNumber = currentToken.lineNumber;
        caseBranchNode.adopt(parseConstantList()); //Adopts a constant list node as the first node
        
        if(currentToken.type == COLON)
            currentToken = scanner.nextToken(); //Consume : token
        else
            syntaxError("Expecting :");
        
        Node statement = parseStatement(); //Adopts a statement as the second node
        
        if(statement != null) //Ensure that it isn't an empty statement
            caseBranchNode.adopt(statement);
        
        if(currentToken.type == SEMICOLON)
            currentToken = scanner.nextToken(); //Consume ; token
        else if(currentToken.type == END) 
            return caseBranchNode; //the last branch doesn't need to be ended with a semicolon, so the next token would just be END
        else
            syntaxError("Expecting ;");
        
        return caseBranchNode;
    }
    
    private Node parseConstantList()
    {
        Node constantListNode = new Node(Node.NodeType.CONSTANT_LIST);
        constantListNode.lineNumber = currentToken.lineNumber;
        constantListNode.adopt(parseConstant()); //Adopts a single constant node as the first child
        
        while(currentToken.type == COMMA)
        {
            currentToken = scanner.nextToken(); //consume COMMA token before constant
            constantListNode.adopt(parseConstant()); //Adopt a constant node (if there it is part of a list, the current node will be a comma after completion)
        }

    	return constantListNode;
    }
    
    private Node parseConstant()
    {
        Node constantNode = new Node(Node.NodeType.CONSTANT);
        constantNode.lineNumber = currentToken.lineNumber;
        switch(currentToken.type)
        {
            case STRING: constantNode.adopt(parseStringConstant()); break; 
            case CHARACTER: constantNode.adopt(parseCharacterConstant()); break;
            
            default: //neither a STRING nor CHARACTER
            {
                Node adopter = constantNode; //adopterNode is the constantNode by default.
                
                if(currentToken.type == PLUS)
                {
                    adopter = new Node(POSITIVE); //change the adopter node to a + Node
                    constantNode.adopt(adopter);
                    currentToken = scanner.nextToken(); //consume the + token
                }
                else if(currentToken.type == MINUS)
                {
                    adopter = new Node(NEGATIVE); //change the adopter node to a - Node
                    constantNode.adopt(adopter);
                    currentToken = scanner.nextToken(); //consume the - token
                }

                switch(currentToken.type) //adopter node adopts a VARIABLE, INTEGER, or REAL node, or it throws an error
                {
                    case IDENTIFIER: adopter.adopt(parseVariable()); break;
                    case INTEGER: adopter.adopt(parseIntegerConstant()); break;
                    case REAL: adopter.adopt(parseRealConstant()); break;
                
                    default: syntaxError("Expecting Constant (STRING, CHARACTER, IDENTIFIER, INTEGER, or REAL)");
                }
            }
        }
        
        
    	return constantNode;
    }
}
