package intermediate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import backend.compiler.CodeGenerator;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.ArgumentList;
import intermediate.type.RoutineSpec;
import intermediate.type.TypeSpec;

public class Predefined
{
    public static String INT = "integer";
    public static String STRING = "string";
    public static String REAL = "real";
    public static String BOOL = "bool";
    public static String ROUTINE = "routine";
    public static String ARRAY = "array";
    public static String LIST = "list";
    
    public static String UNARY_MINUS = "operator_minus";
    public static String UNARY_NOT = "operator_not";
    
    public static String OPERATOR_STAR = "operator_star";
    public static String OPERATOR_SLASH = "operator_slash";
    public static String OPERATOR_MOD = "operator_mod";
    
    public static String OPERATOR_PLUS = "operator_plus";
    public static String OPERATOR_MINUS = UNARY_MINUS;
    
    public static String OPERATOR_EQUALS = "operator_equals";
    public static String OPERATOR_NOT_EQUALS = "operator_not_equals";
    public static String OPERATOR_LESS_EQUALS = "operator_less_equals";
    public static String OPERATOR_GREATER_EQUALS = "operator_greater_equals";
    public static String OPERATOR_LESS_THAN= "operator_less_than";
    public static String OPERATOR_GREATER_THAN= "operator_greater_than";
    
    public static String OPERATOR_AND = "operator_and";
    public static String OPERATOR_OR = "operator_or";
    
    public static String OPERATOR_PAREN = "operator_parenthesis";
    public static String OPERATOR_BRACKET = "operator_brackets";
    
    public static String OPERATOR_ASSIGN = "operator_assignment";
    
    //defines the types
    public static TypeSpec intType = new TypeSpec(INT);
    public static TypeSpec realType = new TypeSpec(REAL);
    public static TypeSpec stringType = new TypeSpec(STRING);
    public static TypeSpec boolType = new TypeSpec(BOOL);
    public static TypeSpec routineType = new TypeSpec(ROUTINE);
    
    //printType and readType are special types that aren't routines
    public static TypeSpec printType = new TypeSpec("print");
    public static TypeSpec readType = new TypeSpec("read");
    
    //defines the world scope's routines
    public static ScopeEntry printEntry;
    public static ScopeEntry readEntry;
    
    public static void addPredefinedTypesToScope(Scope scope)
    {
        //adds the types to the scope
        scope.addType(intType);
        scope.addType(realType);
        scope.addType(stringType);
        scope.addType(boolType);
        scope.addType(routineType);
        scope.addType(printType);
        scope.addType(readType);
        //scope.addType(arrayType);
        //scope.addType(listType);
        
        //adds the available routines to the types
        
        RoutineSpec intEqualsInt = new RoutineSpec(OPERATOR_EQUALS, new ArgumentList(intType), boolType); //int.operator_equals(int) returns bool which means int == int returns bool (allows us to do x = y = 1)
        RoutineSpec intAddInt = new RoutineSpec(OPERATOR_PLUS, new ArgumentList(intType), intType); //int.operator_add(int) returns int which means int + int returns int
        RoutineSpec intAddReal = new RoutineSpec(OPERATOR_PLUS, new ArgumentList(realType), intType); //int.operator_add(int) returns real which means int + real returns int
        RoutineSpec intUnaryMinus = new RoutineSpec(UNARY_MINUS, new ArgumentList(intType), intType); 
        
        intType.addRoutine(intEqualsInt);
        intType.addRoutine(intAddInt);
        intType.addRoutine(intAddReal);
        intType.addRoutine(intUnaryMinus);
        
        RoutineSpec stringParenInt = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(intType), stringType);
        stringType.addStaticRoutine(stringParenInt);
        
    }
    
    public static void addPredefinedRoutinesToScope(Scope scope)
    {
        RoutineSpec print = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(stringType), null);
        TypeSpec printType = Predefined.printType; //print has to be a special type
        printType.setPath("print");
        printType.addRoutine(print);
        printEntry = new ScopeEntry("print", printType);
        scope.addEntry(printEntry);
        
        RoutineSpec read = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(), stringType);
        TypeSpec readType = Predefined.readType; //print is a special type
        readType.setPath("read");
        readType.addRoutine(read);
        readEntry = new ScopeEntry("read", readType);
        scope.addEntry(readEntry);
    }
    
    public static void emitPredefinedRoutines(CodeGenerator code)
    {
        //emit print
        code.emitConstructorCall(printEntry, null);
        code.emitStoreEntry(printEntry);
    }
    
    public static void movePredefinedTypes(String binFolder)
    {
        routineType.setPath(binFolder + ROUTINE);
        intType.setPath("library/" + Predefined.INT);
        realType.setPath("library/" + Predefined.REAL);
        boolType.setPath("library/" + Predefined.BOOL);
        stringType.setPath("library/" + Predefined.STRING);
        
        printEntry.type.setPath("library/print");
        readEntry.type.setPath("library/read");
        
        new File("library").mkdir();
        
        for(File predefinedFile : new File("bin/library").listFiles())
        {
            if(predefinedFile.getName().endsWith(".class"))
            {
                Path sourcePath = Paths.get("bin/library/" + predefinedFile.getName());
                Path destinationpath = Paths.get("library/" + predefinedFile.getName());
                try
                {
                    Files.copy(sourcePath, destinationpath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
