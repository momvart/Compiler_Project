package sut.momtsaber.clikecompiler.codegen;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import sut.momtsaber.clikecompiler.cfg.CFGAction;
import sut.momtsaber.clikecompiler.codegen.exceptions.WrongBreakPositionException;
import sut.momtsaber.clikecompiler.codegen.exceptions.WrongContinuePositionException;
import sut.momtsaber.clikecompiler.codegen.exceptions.DeclarationOfDeclaredException;
import sut.momtsaber.clikecompiler.codegen.exceptions.IllegalTypeOfVoidException;
import sut.momtsaber.clikecompiler.codegen.exceptions.TypeMismatchException;
import sut.momtsaber.clikecompiler.codegen.il.ILOperand;
import sut.momtsaber.clikecompiler.codegen.il.ILStatement;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;

public class CodeGenerationContext
{
    private static final int ADDRESS_START = 100;
    private static final int TEMP_ADDRESS_START = 1000;
    private static final int STACK_START_ADDRESS = 10000;

    private static final int STACK_POINTER_ADDRESS = 0;
    private static final int MAIN_FUNC_POINTER_ADDRESS = 1;

    private static final int VARIABLE_SIZE = 4;

    private ArrayList<ILStatement> statementPipeline;

    private Deque<Scope> scopes = new LinkedList<>();

    private LinkedList<Value> valuesStack = new LinkedList<>();
    private LinkedList<Token> tokenStack = new LinkedList<>();

    private int lastVarAddress = ADDRESS_START;
    private int lastTempAddress = TEMP_ADDRESS_START;
    private final int stackPointerAddress = STACK_POINTER_ADDRESS;
    private final int mainFuncPointerAddress = MAIN_FUNC_POINTER_ADDRESS;

    private int getLineNumber() { return statementPipeline.size(); }

    private int getNextFreeAddress(int size)
    {
        int ret = lastTempAddress;
        lastVarAddress += size;
        return ret;
    }

    private int getNextFreeTempAddress()
    {
        int ret = lastTempAddress;
        lastTempAddress += VARIABLE_SIZE;
        return ret;
    }

    public void handleAction(CFGAction action, Token lastToken) throws InterruptedException
    {
        switch (action.getName())
        {
            case CFGAction.Names.PUSH_TOKEN:
                tokenStack.push(lastToken);
                break;
            case CFGAction.Names.START_PROGRAM:
                startProgram();
                break;
            case CFGAction.Names.END_PROGRAM:
                endProgram();
                break;
            case CFGAction.Names.DECLARE_VAR:
                declareVariable();
                break;
            case CFGAction.Names.DECLARE_ARRAY:
                declareArray();
                break;
            case CFGAction.Names.ADD_SUBTRACT:
                addOrSub();
                break;
            case CFGAction.Names.MULTIPLY:
                multiply();
                break;
            case CFGAction.Names.APPLY_SIGN:
                applySign();
                break;
            case CFGAction.Names.LABEL:
                label();
                break;
            case CFGAction.Names.SAVE:
                save();
                break;
            case CFGAction.Names.WHILE:
                while_();
                break;
            case CFGAction.Names.JPF_SAVE:
                jpf_save();
                break;
            case CFGAction.Names.JP:
                jp();
                break;
            case CFGAction.Names.PUT_1:
                put_1();
                break;
            case CFGAction.Names.JPF_CMP_SAVE:
                jpf_cmp_save();
                break;
            case CFGAction.Names.JPF:
                jpf();
                break;
            case CFGAction.Names.CONTINUE:
                continue_();
                break;
            case CFGAction.Names.BREAK:
                break_();
                break;
        }
    }


    private void startProgram() throws InterruptedException
    {
        Scope global = new Scope(null, getLineNumber());
        global.addDefinition(new VarDefinition("0sp", stackPointerAddress));
        assign(stackPointerAddress, new Value(Value.Type.CONST, STACK_START_ADDRESS));
        global.addDefinition(new VarDefinition("1mainp", mainFuncPointerAddress));
    }

    private void endProgram() {}

    //region Scope
    private void beginNewScope()
    {
        scopes.push(new Scope(getCurrentScope(), getLineNumber()));
    }

    private void endCurrentScope()
    {
        scopes.pop();
    }

    private Scope getCurrentScope() { return scopes.element(); }
    //endregion

    //region Declaration

    private void declareVariable() throws IllegalTypeOfVoidException, DeclarationOfDeclaredException
    {
        Token name = tokenStack.pop();
        Token type = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack.");
        if (type.getType() != TokenType.KEYWORD)
            throw new IllegalStateException("Variable type not found in the stack.");
        if (!type.getValue().equals("int"))
            throw new IllegalTypeOfVoidException(getLineNumber(), name.getValue());
        declareVariable(name.getValue());
    }

    private void declareVariable(String name) throws DeclarationOfDeclaredException
    {
        if (!getCurrentScope()
                .addDefinition(new VarDefinition(name, getNextFreeAddress(VARIABLE_SIZE))))
            throw new DeclarationOfDeclaredException(getLineNumber(), name);
    }

    private void declareArray() throws IllegalTypeOfVoidException, DeclarationOfDeclaredException, InterruptedException
    {
        Token size = tokenStack.pop();
        Token name = tokenStack.pop();
        Token type = tokenStack.pop();
        if (size.getType() != TokenType.NUMBER)
            throw new IllegalStateException("Array size not found in the stack.");
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Array name not found in the stack.");
        if (type.getType() != TokenType.KEYWORD)
            throw new IllegalStateException("Array type not found in the stack.");
        if (!type.getValue().equals("int"))
            throw new IllegalTypeOfVoidException(getLineNumber(), name.getValue());
        declareArray(name.getValue(), Integer.parseInt(size.getValue()));
    }

    private void declareArray(String name, int size) throws DeclarationOfDeclaredException, InterruptedException
    {
        VarDefinition def = new VarDefinition(name, getNextFreeAddress(VARIABLE_SIZE), true);
        if (!getCurrentScope().addDefinition(def))
            throw new DeclarationOfDeclaredException(getLineNumber(), name);
        assign(def.getAddress(), new Value(Value.Type.CONST, getNextFreeAddress(VARIABLE_SIZE * size)));
    }

    //endregion

    //region Arithmetic

    private void checkArithmeticOperands(Value first, Value second)
    {
        if (first.getType() == Value.Type.REFERENCE || second.getType() == Value.Type.REFERENCE ||
                first.getType() == Value.Type.VOID || second.getType() == Value.Type.VOID)
            throw new TypeMismatchException(getLineNumber(), first.getType(), second.getType());
    }

    private Value makeTempResult()
    {
        Value result = new Value(Value.Type.VAR, getNextFreeTempAddress());
        valuesStack.push(result);
        return result;
    }

    private void multiply() throws InterruptedException
    {
        multiply(valuesStack.pop(), valuesStack.pop());
    }

    private void multiply(Value first, Value second) throws InterruptedException
    {
        checkArithmeticOperands(first, second);
        statementPipeline.add(ILStatement.multiply(first.toOperand(), second.toOperand(),
                makeTempResult().toOperand()));
    }

    private void addOrSub() throws InterruptedException
    {
        Token addop = tokenStack.pop();
        if (addop.getType() != TokenType.SYMBOL ||
                !addop.getValue().equals("+") || !addop.getValue().equals("-"))
            throw new IllegalStateException("Add operation not found in the stack");

        if (addop.getValue().equals("+"))
            add(valuesStack.pop(), valuesStack.pop());
        else
            sub(valuesStack.pop(), valuesStack.pop());
    }

    private void add(Value first, Value second) throws InterruptedException
    {
        checkArithmeticOperands(first, second);
        statementPipeline.add(ILStatement.add(first.toOperand(), second.toOperand(),
                makeTempResult().toOperand()));
    }

    private void sub(Value first, Value second) throws InterruptedException
    {
        checkArithmeticOperands(first, second);
        statementPipeline.add(ILStatement.subtract(first.toOperand(), second.toOperand(),
                makeTempResult().toOperand()));
    }

    private void applySign() throws InterruptedException
    {
        Token sign = tokenStack.pop();
        if (sign.getType() != TokenType.SYMBOL ||
                !sign.getValue().equals("+") || !sign.getValue().equals("-"))
            throw new IllegalStateException("Sign not found in the stack");

        if (sign.getValue().equals("+"))
            return;

        statementPipeline.add(ILStatement.subtract(ILOperand.immediate(0), valuesStack.pop().toOperand(),
                makeTempResult().toOperand()));
    }

    //endregion

    private void assign() throws InterruptedException
    {
        Value right = valuesStack.pop();
        Value left = valuesStack.pop();
        if (right.getType() != left.getType())
            throw new TypeMismatchException(getLineNumber(), left.getType(), right.getType());
        assign(left.getValue(), right);
    }

    private void assign(int address, Value value) throws InterruptedException
    {
        statementPipeline.add(ILStatement.assign(ILOperand.direct(address), value.toOperand()));
    }


    private void label()
    {
        valuesStack.push(new Value(Value.Type.CONST, getLineNumber()));
    }

    private void save()
    {
        valuesStack.push(new Value(Value.Type.CONST, getLineNumber()));
        statementPipeline.add(null);
    }

    // while statement
    private void while_()
    {
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        Value label = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber() + 1).toOperand()));
        statementPipeline.add(ILStatement.jump(label.toOperand()));
    }
    // end while

    // if statement
    private void jpf_save()
    {
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber() + 1).toOperand()));
        save();
    }

    private void jp()
    {
        Value savedCodeLine = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jump(new Value(Value.Type.CONST, getLineNumber()).toOperand()));
    }
    // end if

    //case statement
    private void jpf_cmp_save()
    {
        Value case_value = valuesStack.pop();
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        Value expression = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber()).toOperand()));
        valuesStack.push(expression);
        Value temp = makeTempResult();
        statementPipeline.add(ILStatement.equals_(expression.toOperand(), case_value.toOperand(), temp.toOperand()));
        save();
    }

    private void jpf()
    {
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        // popping expression
        valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber()).toOperand()));
    }

    private void put_1()
    {
        valuesStack.push(new Value(Value.Type.CONST, 1));
    }

    //end case

    // break and continue
    private void continue_()
    {
        if (!(getCurrentScope() instanceof WhileScope))
            throw new WrongContinuePositionException(getLineNumber());
        statementPipeline.add(ILStatement.jump(new Value(Value.Type.CONST, getCurrentScope().getStartLine()).toOperand()));
    }

    private void break_()
    {
        if (!(getCurrentScope() instanceof WhileScope || getCurrentScope() instanceof SwitchScope))
            throw new WrongBreakPositionException(getLineNumber());
        int jumpPosition = getNextFreeTempAddress();
        if (getCurrentScope() instanceof WhileScope)
            ((WhileScope)getCurrentScope()).setBreakAddress(jumpPosition);
        else if (getCurrentScope() instanceof SwitchScope)
            ((SwitchScope)getCurrentScope()).setBreakAddress(jumpPosition);
        statementPipeline.add(ILStatement.jump(new Value(Value.Type.REFERENCE, jumpPosition).toOperand()));
        // we should set the value for jumpPosition after finishing the scope
    }

    // end break and continue
    public static class Value
    {
        public enum Type
        {
            CONST,
            VAR,
            REFERENCE,
            VOID
        }

        private final Type type;
        private final int value;

        Value(Type type, int value)
        {
            this.type = type;
            this.value = value;
        }

        public Type getType()
        {
            return type;
        }

        public int getValue()
        {
            return value;
        }

        ILOperand toOperand()
        {
            switch (type)
            {
                case CONST:
                    return ILOperand.immediate(value);
                case VAR:
                case REFERENCE:
                    return ILOperand.direct(value);
                default:
                    return null;
            }
        }
    }
}

