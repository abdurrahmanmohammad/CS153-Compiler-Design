package backend.interpreter;

import intermediate.symtab.*;

import java.util.ArrayList;
import java.util.List;

import antlr4.*;

public class Executor extends Pcl4BaseVisitor<Object>
{
    private Symtab symtab;
    
    public Executor(Symtab symtab)
    {
        this.symtab = symtab;
    }
    
    @Override
    public Object visitProgram(Pcl4Parser.ProgramContext node)
    {
        //System.out.println("Visiting program");
        return visit(node.block());
    }
    
    @Override
    public Object visitCompoundStatement(Pcl4Parser.CompoundStatementContext node)
    {
        //System.out.println("Visiting Compound Statement");
        return visitChildren(node);
    }
    
    
    //---------------- Repeat Statement ----------------\\
    
    
    @Override
    public Object visitRepeatStatement(Pcl4Parser.RepeatStatementContext node)
    {
        do
        {
            visit(node.statementList());
        }
        while(! (Boolean) visitExpression(node.expression()));
        return null;
    }
    //---------------- WHILE Statement ----------------\\
    @Override 
    public Object visitWhileStatement(Pcl4Parser.WhileStatementContext ctx) {
        while((Boolean) visitExpression(ctx.expression()))
        {  
           visit(ctx.statement());
           
      }     
        return null; 
        }  
    

    // ---------------- IF Statement ----------------\\
    @Override
    public Object visitIfStatement(Pcl4Parser.IfStatementContext ctx) {
        // Retrieve boolean expression and test
        // If expression is false and ELSE exists, execute statements in ELSE
        if ((Boolean) visitExpression(ctx.expression())) // If expression is true
            visit(ctx.statement(0)); // Execute statements in IF statement
        else if (ctx.ELSE() != null)
            visit(ctx.statement(1)); // Execute statements in ELSE statement
        return null;
    }
    
    // ---------------- FOR Statement ----------------\\
    @Override
    public Object visitForStatement(Pcl4Parser.ForStatementContext ctx) {
        // Assignment statement
        SymtabEntry lhs = (SymtabEntry) visitLhs(ctx.assignmentStatement().lhs()); // Get variable
        Object rhs = visitRhs(ctx.assignmentStatement().rhs()); // Get initial value
        lhs.setValue((Double) rhs); // Do the assignment
        // Terminating number
        double end = (Double) visitExpression(ctx.expression());
        // Determine if TO or DOWNTO then loop statements
        if (ctx.TO() != null) // TO statement
            while (lhs.getValue() != end + 1) {
                visit(ctx.statement());
                lhs.setValue(lhs.getValue() + 1); // Do the increment/decrement
            }
        else // DOWNTO statement
            while (lhs.getValue() != end - 1) {
                visit(ctx.statement());
                lhs.setValue(lhs.getValue() - 1); // Do the increment/decrement
            }
        return null;
    }
    
    //---------------- Assignment Statement ----------------\\
    
    
    @Override
    public Object visitAssignmentStatement(Pcl4Parser.AssignmentStatementContext node)
    {
        SymtabEntry lhs = (SymtabEntry) visitLhs(node.lhs());
        Object rhs = visitRhs(node.rhs());
        
        lhs.setValue((Double) rhs);
        return null;
    }
    
    @Override
    public Object visitLhs(Pcl4Parser.LhsContext node)
    {
        String id = (String) visitVariable(node.variable());
        SymtabEntry lhs = symtab.lookup(id);
        
        if(lhs == null)
            lhs = symtab.enter(id);

        return lhs;
    }
    
    @Override
    public Object visitRhs(Pcl4Parser.RhsContext node)
    {
        return visitExpression(node.expression());
    }
    
    
    //---------------- Case Statement ----------------\\
    
    
    @Override
    public Object visitCaseStatement(Pcl4Parser.CaseStatementContext node)
    {
        Object expValue = visitExpression(node.expression());
        
        for(int i = 0; node.constantList(i) != null; i++)
        {
            List<Object> constList = (List<Object>) visitConstantList(node.constantList(i));
            for(Object constValue : constList)
            {
                if(expValue.equals(constValue))
                {
                    visitStatement(node.statement(i));
                    return null;
                }
            }
        }
        
        return null;
    }
    
    @Override
    public Object visitConstantList(Pcl4Parser.ConstantListContext node)
    {
        ArrayList<Object> constList = new ArrayList<>();
        
        for(Pcl4Parser.ConstantContext constNode : node.constant())
            constList.add(visitConstant(constNode));
        
        return constList;
    }
    
    @Override
    public Object visitConstant(Pcl4Parser.ConstantContext node)
    {
        Object constValue = null; 
        
        Double sign = Double.valueOf(1); //sign that may or may not be used
        if(node.sign() != null && node.sign().getText().equals("-")) //+ doesn't do anything, so just check if a sign exists and if it is -
            sign = Double.valueOf(-1);
        
        if(node.IDENTIFIER() != null)
        {
            SymtabEntry var = symtab.lookup(node.IDENTIFIER().getText());
            
            if(var == null)
                runtimeError(node.start.getLine(), "Undeclared variable", node.getText());
            
            constValue = sign * (Double) var.getValue();
        }
        else if(node.unsignedNumber() != null)
        {
            constValue = sign * (Double) visitUnsignedNumber(node.unsignedNumber());
        }
        else if(node.STRING() != null)
        {
            constValue = node.STRING().getText().substring(1,  node.STRING().getText().length() - 1);
        }
        
        return constValue;
    }
    //---------------- Write ----------------\\
    
    @Override
    public Object visitWriteArgumentListOn(Pcl4Parser.WriteArgumentListOnContext node)
    {
        System.out.print(visitWriteArgumentList(node.writeArgumentList()));
        
        return null;
    }
    
    // ---------------- Writeln ----------------\\
    @Override
    public Object visitWritelnStatement(Pcl4Parser.WritelnStatementContext ctx) {
        // Check if writeln('...') or writeln:
        if (ctx.writeArgumentsLn() != null) // Print line followed by newline: writeln('...');
            // ctx.writeArgumentsLn() returns a WriteArgumentsLnContext
            visitWriteArgumentsLn(ctx.writeArgumentsLn());
        else
            System.out.println(); // Print newline: writeln;
        return null;
    }
    @Override
    public Object visitWriteArgumentsLn(Pcl4Parser.WriteArgumentsLnContext ctx) {
        // ctx.writeArgumentListLn() returns a WriteArgumentListLnContext
        visitWriteArgumentListLn(ctx.writeArgumentListLn());
        return null;
    }
    @Override
    public Object visitWriteArgumentListLn(Pcl4Parser.WriteArgumentListLnContext node) {
        System.out.println(visitWriteArgumentList(node.writeArgumentList())); // Print writeln('..');
        return null;
    }
    
    
    @Override
    public Object visitWriteArgumentList(Pcl4Parser.WriteArgumentListContext node)
    {
        String output = "";
        
        for(Pcl4Parser.WriteArgumentContext childNode : node.writeArgument())
            output += visitWriteArgument(childNode);
        
        return output;
    }
    
    @Override
    public Object visitWriteArgument(Pcl4Parser.WriteArgumentContext node)
    {
        Object value = visitExpression(node.expression());
        
        String format = "%";
        
        if(node.fieldWidth() != null) //if there is a fieldWidth (which could contain the decimal places) add it to the format
            format += (String) visitFieldWidth(node.fieldWidth());

        if(value instanceof Double) //handles the type character that needs to be included with the format
        {
            if(!format.contains(".")) //true if the number of decimal places hasn't been specified
                format += ".0";
            
            format += "f";
        }
        else 
            format += "s";
        
        value = String.format(format, value);
        
        return value;
    }
    
    @Override
    /**
     * @return a string containing the field width and decimal places (if any)
     */
    public Object visitFieldWidth(Pcl4Parser.FieldWidthContext node)
    {
        String format = "";
        
        Double width = (Double) visitIntegerConstant(node.integerConstant());
        
        if(node.sign() != null && node.sign().getText().equals("-")) //handles the sign in the Field Width
            width = -1 * width;
        
        if(width >= 0)
            format += width.intValue(); //have to take the integer value because doubles are represented with a trailing .0 in strings
        
        if(node.decimalPlaces() != null)
            format += visitDecimalPlaces(node.decimalPlaces());
            
        return format; 
    }
    
    @Override
    public Object visitDecimalPlaces(Pcl4Parser.DecimalPlacesContext node)
    {
        String format = ".";
        
        Double places = (Double) visitIntegerConstant(node.integerConstant());
        
        format += places.intValue(); //have to take the integer value because doubles are represented with a trailing .0 in strings
        
        return format;
    }
    
    //---------------- Expression ----------------\\
    
    
    @Override
    public Object visitExpression(Pcl4Parser.ExpressionContext node)
    {
        Object value1 = visitSimpleExpression(node.simpleExpression(0));
        
        if(node.relOp() != null) //checks if there is a second simple expression
        {
            Object value2 = visitSimpleExpression(node.simpleExpression(1));
            
            switch(node.relOp().getText())
            {
                case "="  : return value1.equals(value2); 
                case "<>" : return !value1.equals(value2); 
                case "<"  : return (Double) value1 <  (Double) value2;
                case "<=" : return (Double) value1 <= (Double) value2;
                case ">"  : return (Double) value1 >  (Double) value2;
                case ">=" : return (Double) value1 >= (Double) value2;
            }
        }
        
        return value1;
    }
    
    @Override
    public Object visitSimpleExpression(Pcl4Parser.SimpleExpressionContext node)
    {
        Object value1 = visitTerm(node.term(0));
        
        if(node.sign() != null && node.sign().getText().equals("-")) //right now, + doesn't do anything, so we can just check if the sign exists and if it is -
            value1 = -1 * (Double) value1;

        for(int i = 1; node.term(i) != null; i++) //keep looping while there is still another term
        {
            Object valueN = visitTerm(node.term(i));
            
            switch(node.addOp(i - 1).getText().toUpperCase()) //there is one fewer addOp than there is term so we have to index it with i - 1
            {
                case "+" : value1 = (Double) value1 + (Double) valueN; break;
                case "-" : value1 = (Double) value1 - (Double) valueN; break;
                case "OR" : value1 = (Boolean) value1 || (Boolean) valueN; break;
            }
        }
        
        return value1;
    }
    
    @Override
    public Object visitTerm(Pcl4Parser.TermContext node)
    {
        Object value1 = visit(node.factor(0));
        
        for(int i = 1; node.factor(i) != null; i++) //keep looping while there is still another term
        {
            Object valueN = visit(node.factor(i));
            
            switch(node.mulOp(i - 1).getText().toUpperCase()) //there is one fewer addOp than there is term so we have to index it with i - 1
            {
                case "*" : value1 = (Double) value1 * (Double) valueN; break;
                case "/" :  
                {
                    if( (Double) valueN == 0.0) //if the divisor is 0, throw a runtime error
                        runtimeError(node.factor(i).start.getLine(), "Division by 0", node.factor(i).getText());
                    
                    value1 = (Double) value1 / (Double) valueN;
                } break;
                case "DIV" :
                {
                    if( (Double) valueN == 0.0) //if the divisor is 0, throw a runtime error
                        runtimeError(node.factor(i).start.getLine(), "Division by 0", node.factor(i).getText());
                    
                    value1 = Math.floor((Double) value1 / (Double) valueN); 
                } break;
                case "MOD" : value1 = (Double) value1 % (Double) valueN; break;
                case "AND" : value1 = (Boolean) value1 && (Boolean) valueN; break;
            }
        }
        return value1;
    }
    
    @Override 
    public Object visitVariableExpression(Pcl4Parser.VariableExpressionContext node)
    {
        String id = (String) visitVariable(node.variable());
        
        SymtabEntry var = symtab.lookup(id);
        
        if(var == null) //if the variable id doesn't exist in the symbol table, throw a runtime error
            runtimeError(node.variable().start.getLine(), "Undeclared variable", node.variable().getText());
        
        return var.getValue();
    }
    
    @Override
    public Object visitNumberExpression(Pcl4Parser.NumberExpressionContext node)
    {
        return visitNumber(node.number());
    }
    
    @Override
    public Object visitCharacterFactor(Pcl4Parser.CharacterFactorContext node)
    {   
        return visitCharacterConstant(node.characterConstant());
    }
    
    @Override
    public Object visitStringFactor(Pcl4Parser.StringFactorContext node)
    {
        return visitStringConstant(node.stringConstant());
    }
    
    @Override
    public Object visitNotFactor(Pcl4Parser.NotFactorContext node)
    {
        return ! (Boolean) visit(node.factor());
    }
    
    @Override
    public Object visitParenthesizedExpression(Pcl4Parser.ParenthesizedExpressionContext node)
    {
        return visitExpression(node.expression());
    }
    
    
    //---------------- Variable/Constants ----------------\\
    
    
    @Override
    public Object visitVariable(Pcl4Parser.VariableContext node)
    {
        return node.IDENTIFIER().getText();
    }
    
    @Override
    public Object visitNumber(Pcl4Parser.NumberContext node)
    {
        Double value = (Double) visitUnsignedNumber(node.unsignedNumber());
        
        if(node.sign() != null && node.sign().getText().equals("-")) //right now, + doesn't do anything, so we can just check if the sign exists and if it is -
            value = -1 * (Double) value;
        
        return value;
    }
    
    @Override 
    public Object visitUnsignedNumber(Pcl4Parser.UnsignedNumberContext node)
    {
        if(node.integerConstant() != null) //if it has an integer constant, return it
            return visitIntegerConstant(node.integerConstant());
        else //otherwise, return the real constant
            return visitRealConstant(node.realConstant());
    }
    
    @Override
    public Object visitIntegerConstant(Pcl4Parser.IntegerConstantContext node)
    {
        return Double.valueOf(node.INTEGER().getText()); //due to our hack, we store everything as a double, even if it is technically supposed to be an integer
    }
    
    @Override
    public Object visitRealConstant(Pcl4Parser.RealConstantContext node)
    {
        return Double.valueOf(node.REAL().getText());
    }
    
    @Override
    public Object visitCharacterConstant(Pcl4Parser.CharacterConstantContext node)
    {
        //Since we aren't dealing with types yet, we can just treat characters as strings with size 1
        //We also have to call substring because the text of the CHARACTER also includes the opening and closing '
        return node.CHARACTER().getText().substring(1,2); 
    }
    
    @Override
    public Object visitStringConstant(Pcl4Parser.StringConstantContext node)
    {
        String str =node.STRING().getText();
        str = str.substring(1, str.length() - 1); //We also have to call substring because the text of the STRING also includes the opening and closing '
        return str;
    }
    
    //---------------- Errors ----------------\\
    
    private void runtimeError(int lineNum, String message, String nodeText)
    {
        System.out.printf("RUNTIME ERROR at line %d: %s: \'%s\'\n", 
                lineNum, message, nodeText);
        System.exit(-2);
    }
    
}
