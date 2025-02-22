package backend.compiler;

import java.util.ArrayList;

import antlr4.PascalParser;
import intermediate.symtab.Symtab;
import intermediate.symtab.SymtabEntry;
import intermediate.symtab.SymtabEntry.Kind;
import intermediate.type.Typespec;

import static intermediate.symtab.SymtabEntry.Kind.*;
import static intermediate.type.Typespec.Form.*;
import static backend.compiler.Directive.*;
import static backend.compiler.Instruction.*;

public class ProgramGenerator extends CodeGenerator
{
    private SymtabEntry programId;   // symbol table entry of the program name
    private int programLocalsCount;  // count of program local variables

    /**
     * Constructor.
     * @param the parent generator.
     * @param compiler the compiler to use.
     */
    public ProgramGenerator(CodeGenerator parent, Compiler compiler)
    {
        super(parent, compiler);
        
        localStack = new LocalStack();
        programLocalsCount = 5;  // +1 because _elapsed is long
    }
    
    /**
     * Emit code for a program.
     * @param ctx the ProgramContext.
     */
    public void emitProgram(PascalParser.ProgramContext ctx)
    {
        programId = ctx.programHeader().programIdentifier().entry;
        Symtab programSymtab = programId.getRoutineSymtab();
        
        localVariables = new LocalVariables(programLocalsCount);
        
        emitRecords(programSymtab);
        
        emitDirective(CLASS_PUBLIC, programName);
        emitDirective(SUPER, "java/lang/Object");

        emitProgramVariables();
        emitInputScanner();
        emitConstructor();
        emitSubroutines(ctx.block().declarations().routinesPart());
        
        emitMainMethod(ctx);
    }
    
    /**
     * Create a new compiler instance for a record.
     * @param symtab the record type's symbol table.
     */
    public void emitRecords(Symtab symtab)
    {
        for (SymtabEntry id : symtab.sortedEntries())
        {
            if (   (id.getKind() == TYPE)
                && (id.getType().getForm() == RECORD))
            {
                new Compiler(compiler, id);
            }
        }
    }
    
    /**
     * Emit code for a record.
     */
    public void emitRecord(SymtabEntry recordId, String namePath)
    {
        Symtab recordSymtab = recordId.getType().getRecordSymtab();
        
        emitDirective(CLASS_PUBLIC, namePath);
        emitDirective(SUPER, "java/lang/Object");
        emitLine();
        
        // Emit code for any nested records.
        emitRecords(recordSymtab);
        
        // Emit record fields.
        for (SymtabEntry id : recordSymtab.sortedEntries())
        {
            if (id.getKind() == RECORD_FIELD)
            {
                emitDirective(FIELD, id.getName(), typeDescriptor(id));
            }
        }
        
        emitConstructor();
        close();  // the object file
    }
    
    /**
     * Emit field directives for the program variables.
     */
    private void emitProgramVariables()
    {
        // Runtime timer and standard in.

        Symtab symtab = programId.getRoutineSymtab();
        ArrayList<SymtabEntry> ids = symtab.sortedEntries();

        emitLine();
        emitDirective(FIELD_PRIVATE_STATIC, "_sysin", "Ljava/util/Scanner;");

        // Loop over all the program's identifiers and
        // emit a .field directive for each variable.
        for (SymtabEntry id : ids) 
        {
            if (id.getKind() == VARIABLE) 
            {
                emitDirective(FIELD_PRIVATE_STATIC, id.getName(),
                              typeDescriptor(id));
            }
        }
    }
    
    /**
     * Emit code for the runtime input scanner.
     */
    private void emitInputScanner()
    {
        emitLine();
        emitComment("Runtime input scanner");
        emitDirective(METHOD_STATIC, "<clinit>()V");
        emitLine();
        
        emit(NEW, "java/util/Scanner");
        emit(DUP);
        emit(GETSTATIC, "java/lang/System/in Ljava/io/InputStream;");
        emit(INVOKESPECIAL, "java/util/Scanner/<init>(Ljava/io/InputStream;)V");
        emit(PUTSTATIC, programName + "/_sysin Ljava/util/Scanner;");
        emit(RETURN);
        
        emitLine();
        emitDirective(LIMIT_LOCALS, 0);
        emitDirective(LIMIT_STACK,  3);
        emitDirective(END_METHOD);
        
        localStack.reset();
    }

    /**
     * Emit code for the main program constructor.
     */
    private void emitConstructor()
    {
        emitLine();
        emitComment("Main class constructor");
        emitDirective(METHOD_PUBLIC, "<init>()V");        
        emitDirective(VAR, "0 is this L" + programName + ";");
        emitLine();

        emit(ALOAD_0);
        emit(INVOKESPECIAL, "java/lang/Object/<init>()V");
        emit(RETURN);

        emitLine();
        emitDirective(LIMIT_LOCALS, 1);
        emitDirective(LIMIT_STACK,  1);
        emitDirective(END_METHOD);
        
        localStack.reset();
    }

    /**
     * Emit code for any nested procedures and functions.
     */
    private void emitSubroutines(PascalParser.RoutinesPartContext ctx)
    {
        if (ctx != null)
        {
            for (PascalParser.RoutineDefinitionContext defnCtx : 
                                                        ctx.routineDefinition())
            {
                compiler = new Compiler(compiler);
                compiler.visit(defnCtx);
            }
        }     
    }

    /**
     * Emit code for the program body as the main method.
     * @param ctx the ProgramContext.
     */
    private void emitMainMethod(PascalParser.ProgramContext ctx)
    {
        emitLine();
        emitComment("MAIN");
        emitDirective(METHOD_PUBLIC_STATIC, 
                                  "main([Ljava/lang/String;)V");

        emitMainPrologue(programId);

        // Emit code to allocate any arrays, records, and strings.
        StructuredDataGenerator structureCode = 
                                    new StructuredDataGenerator(this, compiler);
        structureCode.emitData(programId);

        // Emit code for the compound statement.
        emitLine();
        compiler.visit(ctx.block().compoundStatement());
        
        emitMainEpilogue();
    }

    /**
     * Emit the main method prologue.
     * @parm programId the symbol table entry for the program name.
     */
    private void emitMainPrologue(SymtabEntry programId)
    {
        emitDirective(VAR, "0 is args [Ljava/lang/String;");
        emitDirective(VAR, "1 is _start Ljava/time/Instant;");
        emitDirective(VAR, "2 is _end Ljava/time/Instant;");
        emitDirective(VAR, "3 is _elapsed J");
        
        // Runtime timer.
        emitLine();
        emit(INVOKESTATIC, "java/time/Instant/now()Ljava/time/Instant;");
        localStack.increase(1);
        emit(ASTORE_1);
    }

    /**
     * Emit the main method epilogue.
     */
    private void emitMainEpilogue()
    {
        // Print the execution time.
        emitLine();
        emit(INVOKESTATIC, "java/time/Instant/now()Ljava/time/Instant;");
        localStack.increase(1);
        emit(ASTORE_2);           
        emit(ALOAD_1);             
        emit(ALOAD_2);             
        emit(INVOKESTATIC, "java/time/Duration/between(Ljava/time/temporal/Temporal;" +
                           "Ljava/time/temporal/Temporal;)Ljava/time/Duration;");
        localStack.decrease(1);
        emit(INVOKEVIRTUAL, "java/time/Duration/toMillis()J");
        localStack.increase(1);
        emit(LSTORE_3);              
        emit(GETSTATIC, "java/lang/System/out Ljava/io/PrintStream;");
        emit(LDC, "\"\\n[%,d milliseconds execution time.]\\n\"");
        emit(ICONST_1);             
        emit(ANEWARRAY, "java/lang/Object");
        emit(DUP);                 
        emit(ICONST_0);         
        emit(LLOAD_3);             
        emit(INVOKESTATIC, "java/lang/Long/valueOf(J)Ljava/lang/Long;");
        emit(AASTORE);        
        emit(INVOKEVIRTUAL, "java/io/PrintStream/printf(Ljava/lang/String;" +
                            "[Ljava/lang/Object;)Ljava/io/PrintStream;");
        localStack.decrease(2);
        emit(POP);          

        emitLine();
        emit(RETURN);
        emitLine();
        
        int stackSize = localStack.capacity();
        double nearestPower = Math.ceil(Math.log(stackSize) / Math.log(2)); //calculates the nearest power of 2 that is greater than the stack size

        emitDirective(LIMIT_LOCALS, localVariables.count());
        emitDirective(LIMIT_STACK,  (int) Math.pow(2, nearestPower));
        emitDirective(END_METHOD);
        
        close();  // the object file
    }

    /**
     * Emit code for a declared procedure or function
     * @param routineId the symbol table entry of the routine's name.
     */
    public void emitRoutine(PascalParser.RoutineDefinitionContext ctx)
    {
        SymtabEntry routineId = ctx.procedureHead() != null 
                                ? ctx.procedureHead().routineIdentifier().entry
                                : ctx.functionHead().routineIdentifier().entry;
        Symtab routineSymtab = routineId.getRoutineSymtab();

        emitRoutineHeader(routineId);
        emitRoutineLocals(routineId);

        // Generate code to allocate any arrays, records, and strings.
        StructuredDataGenerator structuredCode = 
                                    new StructuredDataGenerator(this, compiler);
        structuredCode.emitData(routineId);
                
        localVariables = new LocalVariables(routineSymtab.getMaxSlotNumber());

        // Emit code for the compound statement.
        PascalParser.CompoundStatementContext stmtCtx = 
            (PascalParser.CompoundStatementContext) routineId.getExecutable();
        compiler.visit(stmtCtx);
        
        emitRoutineReturn(routineId);
        emitRoutineEpilogue();
    }

    /**
     * Emit the routine header.
     * @param routineId the symbol table entry of the routine's name.
     */
    private void emitRoutineHeader(SymtabEntry routineId)
    {
        String routineName = routineId.getName();
        ArrayList<SymtabEntry> parmIds = routineId.getRoutineParameters();
        StringBuilder buffer = new StringBuilder();

        // Procedure or function name.
        buffer.append(routineName);
        buffer.append("(");

        // Parameter and return type descriptors.
        if (parmIds != null) 
        {
            for (SymtabEntry parmId : parmIds)
            {
                buffer.append(typeDescriptor(parmId));
            }
        }
        buffer.append(")");
        buffer.append(typeDescriptor(routineId));

        emitLine();
        if (routineId.getKind() == PROCEDURE) 
        {
            emitComment("PROCEDURE " + routineName);
        }
        else
        {
            emitComment("FUNCTION " + routineName);
        }
              
        emitDirective(METHOD_PRIVATE_STATIC, buffer.toString());
    }

    /**
     * Emit directives for the local variables.
     * @param routineId the symbol table entry of the routine's name.
     */
    private void emitRoutineLocals(SymtabEntry routineId)
    {
        Symtab symtab = routineId.getRoutineSymtab();
        ArrayList<SymtabEntry> ids = symtab.sortedEntries();

        emitLine();

        // Loop over all the routine's identifiers and
        // emit a .var directive for each variable and formal parameter.
        for (SymtabEntry id : ids) 
        {
            Kind kind = id.getKind();

            if ((kind == VARIABLE) || (kind == VALUE_PARAMETER)
                                   || (kind == REFERENCE_PARAMETER)) 
            {
                int slot = id.getSlotNumber();
                emitDirective(VAR, slot + " is " + id.getName(),
                              typeDescriptor(id));
            }
        }
    }

    /**
     * Emit the routine's return code.
     * @param routineId the symbol table entry of the routine's name.
     */
    private void emitRoutineReturn(SymtabEntry routineId)
    {
        emitLine();

        // Function: Return the value in the implied function variable.
        if (routineId.getKind() == FUNCTION) 
        {
            Typespec type = routineId.getType();

            // Get the slot number of the function variable.
            String varName = routineId.getName();
            SymtabEntry varId = routineId.getRoutineSymtab().lookup(varName);
            emitLoadLocal(type, varId.getSlotNumber());
            emitReturnValue(type);
        }

        // Procedure: Just return.
        else emit(RETURN);
    }

    /**
     * Emit the routine's epilogue.
     */
    private void emitRoutineEpilogue()
    {
        int stackSize = localStack.capacity();
        double nearestPower = Math.ceil(Math.log(stackSize) / Math.log(2)); //calculates the nearest power of 2 that is greater than the stack size
        
        emitLine();
        emitDirective(LIMIT_LOCALS, localVariables.count());
        emitDirective(LIMIT_STACK,  (int) Math.pow(2, nearestPower)); //sets the stack limit to the next highest power of 2
        emitDirective(END_METHOD);
    }
}
