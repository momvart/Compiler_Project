package sut.momtsaber.clikecompiler.codegen;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import sut.momtsaber.clikecompiler.cfg.CFGAction;
import sut.momtsaber.clikecompiler.codegen.exceptions.ArgumentNumberMismatchException;
import sut.momtsaber.clikecompiler.codegen.exceptions.SemanticError;
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

    private int getSourceCodeLineNumber()
    {
        return -1;
    }

    private int getNextFreeAddress(int size)
    {
        int temp = lastVarAddress;
        lastVarAddress += size;
        return temp;
    }

    private int getNextFreeTempAddress()
    {
        int temp = lastTempAddress;
        lastTempAddress += VARIABLE_SIZE;
        return temp;
    }

    private void addNewStatement(ILStatement statement)
    {
        statementPipeline.add(statement);
    }

    private void setStatementAt(int index, ILStatement statement) { statementPipeline.set(index, statement); }

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
                declareVariable(false);
                break;
            case CFGAction.Names.DECLARE_ARRAY:
                declareArray();
                break;
            case CFGAction.Names.DECLARE_FUNC:
                declareFunction();
                break;
            case CFGAction.Names.DECLARE_PARAM_VAR:
                declareParamVariable();
                break;
            case CFGAction.Names.FUNC_INIT:
                initFunction();
                break;
            case CFGAction.Names.SET_RETURN_VAL:
                setReturnValue();
                break;
            case CFGAction.Names.RETURN:
                returnFromFunction();
                break;
            case CFGAction.Names.DECLARE_PARAM_ARRAY:
                declareParamArray();
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
            case CFGAction.Names.PUT_ARRAY_ELEMENT:
                putArrayElement();
                break;
            case CFGAction.Names.PUT_VARIABLE:
                putVariable();
                break;
            case CFGAction.Names.PUT_NUMBER:
                putNumber();
                break;
            case CFGAction.Names.LABEL:
                label();
                break;
            case CFGAction.Names.SAVE:
                save();
                break;
            case CFGAction.Names.WHILE:
                _while();
                break;
            case CFGAction.Names.JPF_SAVE:
                jpfSave();
                break;
            case CFGAction.Names.JP:
                jp();
                break;
            case CFGAction.Names.PUT_1:
                put1();
                break;
            case CFGAction.Names.JPF_CMP_SAVE:
                jpfCmpSave();
                break;
            case CFGAction.Names.JPF:
                jpf();
                break;
            case CFGAction.Names.CONTINUE:
                continueLoop();
                break;
            case CFGAction.Names.BREAK:
                breakLoop();
                break;
        }
    }

    private void startProgram() throws InterruptedException
    {
        Scope global = new Scope(null, getLineNumber());
        global.addDefinition(new VarDefinition("0sp", stackPointerAddress));
        assign(stackPointerAddress, new Value(Value.Type.CONST, STACK_START_ADDRESS));
        global.addDefinition(new VarDefinition("1mainp", mainFuncPointerAddress));

        addNewStatement(null);  //jump to main function
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

    private VarDefinition declareVariable(boolean isArray) throws IllegalTypeOfVoidException, DeclarationOfDeclaredException
    {
        Token name = tokenStack.pop();
        Token type = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack.");
        if (type.getType() != TokenType.KEYWORD)
            throw new IllegalStateException("Variable type not found in the stack.");
        if (!type.getValue().equals("int"))
            throw new IllegalTypeOfVoidException(getSourceCodeLineNumber(), name.getValue());
        return declareVariable(name.getValue(), isArray);
    }

    private VarDefinition declareVariable(String name, boolean isArray) throws DeclarationOfDeclaredException
    {
        VarDefinition retVal = new VarDefinition(name, getNextFreeAddress(VARIABLE_SIZE), isArray);
        if (!getCurrentScope().addDefinition(retVal))
            throw new DeclarationOfDeclaredException(getSourceCodeLineNumber(), name);
        return retVal;
    }

    private void declareArray() throws IllegalTypeOfVoidException, DeclarationOfDeclaredException, InterruptedException
    {
        Token size = tokenStack.pop();
        if (size.getType() != TokenType.NUMBER)
            throw new IllegalStateException("Array size not found in the stack.");
        VarDefinition def = declareVariable(true);
        assign(def.getAddress(), new Value(Value.Type.CONST,
                getNextFreeAddress(VARIABLE_SIZE * Integer.parseInt(size.getValue()))));
    }

    //endregion

    //region Arithmetic

    private void checkArithmeticOperands(Value first, Value second)
    {
        if (first.getType() == Value.Type.REFERENCE || second.getType() == Value.Type.REFERENCE ||
                first.getType() == Value.Type.VOID || second.getType() == Value.Type.VOID)
            throw new TypeMismatchException(getSourceCodeLineNumber(), first.getType(), second.getType());
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

    //region Function

    private FuncDefinition currentFuncDef;

    private void declareFunction()
    {
        Token name = tokenStack.pop();
        Token type = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Function name not found in the stack.");
        if (type.getType() != TokenType.KEYWORD)
            throw new IllegalStateException("Function type not found in the stack.");
        declareFunction(name.getValue(), type.getValue().equals("void"));
    }

    private void declareFunction(String name, boolean isVoid)
    {
        currentFuncDef = new FuncDefinition(name, getLineNumber(),
                getNextFreeAddress(VARIABLE_SIZE),
                isVoid ? -1 : getNextFreeAddress(VARIABLE_SIZE));
        getCurrentScope().addDefinition(currentFuncDef);
        beginNewScope();
    }

    private void declareParamVariable()
    {
        currentFuncDef.addArg(declareVariable(false));
    }

    private void declareParamArray()
    {
        currentFuncDef.addArg(declareVariable(true));
    }

    private void initFunction()
    {
        getCurrentScope().addDefinition(currentFuncDef.getReturnAddress());
        getCurrentScope().addDefinition(currentFuncDef.getReturnValue());
        currentFuncDef = null;
    }

    private void setReturnValue() throws InterruptedException
    {
        assign(getCurrentScope().getVarDefinition(FuncDefinition.RETURN_VAL_VAR_NAME).getAddress(),
                valuesStack.pop());
    }

    private void returnFromFunction()
    {
        addNewStatement(ILStatement.jump(
                ILOperand.indirect(
                        getCurrentScope().getVarDefinition(FuncDefinition.RETURN_ADDR_VAR_NAME).getAddress())));
    }

    private void startArgs()
    {
        valuesStack.push(null); //mark in the stack to show the start of arguments
    }

    private void callFunction() throws InterruptedException
    {
        Token name = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Function name not found in the stack.");

        currentFuncDef = getCurrentScope().getFuncDefinition(name.getValue());
        List<VarDefinition> args = currentFuncDef.getArgs();
        for (int i = args.size() - 1; i >= 0; i--)
        {
            Value arg = valuesStack.pop();
            if (arg == null)
                throw new ArgumentNumberMismatchException(getSourceCodeLineNumber(), currentFuncDef.getId(), args.size());
            assign(args.get(i).getAddress(), arg);
        }
        if (valuesStack.pop() != null)
            throw new ArgumentNumberMismatchException(getSourceCodeLineNumber(), currentFuncDef.getId(), args.size());

        assign(currentFuncDef.getReturnAddress().getAddress(), new Value(Value.Type.CONST, getLineNumber() + 1));

        addNewStatement(ILStatement.jump(ILOperand.immediate(currentFuncDef.getLineNum())));

        valuesStack.push(currentFuncDef.getReturnValue() == null ?
                new Value(Value.Type.VOID, 0) :
                new Value(Value.Type.VAR, currentFuncDef.getReturnValue().getAddress()));
    }

    //endregion

    private void putArrayElement() throws InterruptedException
    {
        Token name = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack");
        VarDefinition def = getCurrentScope().getVarDefinition(name.getValue());
        if (!def.isArray())
            throw new SemanticError(getSourceCodeLineNumber(), "Variable is not an array.");
        Value index = valuesStack.pop();
        multiply(index, new Value(Value.Type.CONST, VARIABLE_SIZE));
        index = valuesStack.pop();
        add(new Value(Value.Type.VAR, def.getAddress()), index);
    }

    private void putVariable()
    {
        Token name = tokenStack.pop();
        if (name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack");
        VarDefinition def = getCurrentScope().getVarDefinition(name.getValue());
        valuesStack.push(new Value(def.isArray() ? Value.Type.REFERENCE : Value.Type.VAR, def.getAddress()));
    }

    private void putNumber()
    {
        Token num = tokenStack.pop();
        if (num.getType() != TokenType.NUMBER)
            throw new IllegalStateException("Number found in the stack");
        valuesStack.push(new Value(Value.Type.CONST, Integer.parseInt(num.getValue())));
    }

    private void assign() throws InterruptedException
    {
        Value right = valuesStack.pop();
        Value left = valuesStack.pop();
        if (right.getType() != left.getType())
            throw new TypeMismatchException(getSourceCodeLineNumber(), left.getType(), right.getType());
        assign(left.getValue(), right);
    }

    private void assign(int address, Value value) throws InterruptedException
    {
        statementPipeline.add(ILStatement.assign(ILOperand.direct(address), value.toOperand()));
    }

    private void jumpTo(int line)
    {
        addNewStatement(ILStatement.jump(ILOperand.direct(getLineNumber())));
    }

    private void jumpIndirect(int address)
    {
        addNewStatement(ILStatement.jump(ILOperand.indirect(address)));
    }

    private void label()
    {
        valuesStack.push(new Value(Value.Type.CONST, getLineNumber()));
    }

    private void save()
    {
        valuesStack.push(new Value(Value.Type.CONST, getLineNumber()));
        // todo i += 1
    }

    //region Control Flow

    // while statement
    private void _while()
    {
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        Value label = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber() + 1).toOperand()));
        statementPipeline.add(ILStatement.jump(label.toOperand()));
    }
    // end while

    // if statement
    private void jpfSave()
    {
        Value savedCodeLine = valuesStack.pop();
        Value condition = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jumpFalse(condition.toOperand(), new Value(Value.Type.CONST, getLineNumber() + 1).toOperand()));
        save();
    }

    private void jp()
    {
        Value savedCodeLine = valuesStack.pop();
        statementPipeline.set(savedCodeLine.getValue(), ILStatement.jump(ILOperand.direct(getLineNumber())));
    }
    // end if

    //case statement
    private void jpfCmpSave()
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

    private void put1()
    {
        valuesStack.push(new Value(Value.Type.CONST, 1));
    }

    //end case

    // break and continue
    private void continueLoop()
    {
        if (!(getCurrentScope() instanceof WhileScope))
            throw new WrongContinuePositionException(getLineNumber());
        statementPipeline.add(ILStatement.jump(new Value(Value.Type.CONST, getCurrentScope().getStartLine()).toOperand()));
    }

    private void breakLoop()
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

        public Value(Type type, int value)
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

        public ILOperand toOperand()
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
    //endregion
}

