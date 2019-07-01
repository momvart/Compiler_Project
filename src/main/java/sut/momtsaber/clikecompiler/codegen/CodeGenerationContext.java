package sut.momtsaber.clikecompiler.codegen;

import java.util.ArrayList;
import java.util.Collections;
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
import sut.momtsaber.clikecompiler.codegen.scopes.BreakableScope;
import sut.momtsaber.clikecompiler.codegen.scopes.FuncScope;
import sut.momtsaber.clikecompiler.codegen.scopes.Scope;
import sut.momtsaber.clikecompiler.codegen.scopes.SwitchScope;
import sut.momtsaber.clikecompiler.codegen.scopes.WhileScope;
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

    private ArrayList<ILStatement> statementPipeline = new ArrayList<>();

    private Deque<Scope> scopes = new LinkedList<>();
    private Scope globalScope;

    private LinkedList<Integer> lineStack = new LinkedList<>();
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

    public List<ILStatement> getCodeBlock()
    {
        return Collections.unmodifiableList(statementPipeline);
    }

    public void handleAction(CFGAction action, Token lastToken) throws InterruptedException
    {
        System.err.printf("Got %s with %s%n", action.getName(), lastToken);
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
            case CFGAction.Names.BEGIN_SCOPE:
                beginNewScope();
                break;
            case CFGAction.Names.END_SCOPE:
                endCurrentScope();
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
            case CFGAction.Names.DECLARE_PARAM_ARRAY:
                declareParamArray();
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
            case CFGAction.Names.START_ARGS:
                startArgs();
                break;
            case CFGAction.Names.CALL:
                callFunction();
                break;
            case CFGAction.Names.END_FUNC:
                endFunction();
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
            case CFGAction.Names.COMPARE:
                compare();
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
            case CFGAction.Names.POP_VALUE:
                popValue();
                break;
            case CFGAction.Names.ASSIGN:
                assign();
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
            case CFGAction.Names.BEGIN_WHILE_SCOPE:
                beginWhileScope();
                break;
            case CFGAction.Names.END_WHILE_SCOPE:
                endWhileScope();
                break;
            case CFGAction.Names.BEGIN_SWICH_SCOPE:
                beginSwitchScope();
                break;
            case CFGAction.Names.END_SWITCH_SCOPE:
                endSwitchScope();
                break;
        }
    }

    private void endSwitchScope()
    {
        SwitchScope scope = (SwitchScope)getCurrentScope();
        addNewStatement(ILStatement.assign(ILOperand.direct(getLineNumber()), ILOperand.direct(scope.getBreakAddress())));
        endCurrentScope();
    }

    private void beginSwitchScope()
    {
        scopes.push(new SwitchScope(getCurrentScope(), getLineNumber()));
    }

    private void endWhileScope()
    {
        WhileScope scope = (WhileScope)getCurrentScope();
        addNewStatement(ILStatement.assign(ILOperand.direct(getLineNumber()), ILOperand.direct(scope.getBreakAddress())));
        endCurrentScope();
    }

    private void beginWhileScope()
    {
        scopes.push(new WhileScope(getCurrentScope(), getLineNumber()));
    }

    private void startProgram() throws InterruptedException
    {
        Scope global = new Scope(null, getLineNumber());
        this.globalScope = global;
        scopes.push(globalScope);
//        global.addDefinition(new VarDefinition("0sp", stackPointerAddress));
//        assign(stackPointerAddress, new Value(Value.Type.CONST, STACK_START_ADDRESS));
        global.addDefinition(new VarDefinition("1mainp", mainFuncPointerAddress));
        lineStack.push(getLineNumber());
        addNewStatement(null);  //set return address of main
        lineStack.push(getLineNumber());
        addNewStatement(null);  //jump to main function

        declareFunction("output", true);
        addParam(declareVariable("a", false));
        initFunction();
        addNewStatement(ILStatement.print(ILOperand.direct(getCurrentScope().getVarDefinition("a").getAddress())));
        endFunction();
    }

    private void endProgram() throws InterruptedException
    {
        setStatementAt(lineStack.pop(),
                ILStatement.assign(ILOperand.immediate(getLineNumber()),
                        ILOperand.direct(getCurrentScope().getFuncDefinition("main").getReturnAddress().getAddress())));
    }

    //region Scope
    private void beginNewScope()
    {
        scopes.push(new Scope(getCurrentScope(), getLineNumber()));
    }

    private void endCurrentScope()
    {
        Scope scope = scopes.pop();
        scope.setEndLine(getLineNumber());
    }

    private Scope getCurrentScope() { return scopes.element(); }
    //endregion

    //region Declaration

    private VarDefinition declareVariable(boolean isArray) throws IllegalTypeOfVoidException, DeclarationOfDeclaredException
    {
        Token name = tokenStack.pollFirst();
        Token type = tokenStack.pollFirst();
        if (name == null || name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack.");
        if (type == null || type.getType() != TokenType.KEYWORD)
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
        Token size = tokenStack.pollFirst();
        if (size == null || size.getType() != TokenType.NUMBER)
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
                !addop.getValue().equals("+") && !addop.getValue().equals("-"))
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

    private void compare()
    {
        Token relop = tokenStack.pop();
        if (relop.getType() != TokenType.SYMBOL ||
                !relop.getValue().equals("<") && !relop.getValue().equals("=="))
            throw new IllegalStateException("Compare operation not found in the stack");

        if (relop.getValue().equals("<"))
            lessThan(valuesStack.pop(), valuesStack.pop());
        else
            equals(valuesStack.pop(), valuesStack.pop());
    }

    private void equals(Value first, Value second)
    {
        checkArithmeticOperands(first, second);
        addNewStatement(ILStatement.equals(first.toOperand(), second.toOperand(),
                makeTempResult().toOperand()));
    }

    private void lessThan(Value first, Value second)
    {
        checkArithmeticOperands(first, second);
        addNewStatement(ILStatement.lessThan(first.toOperand(), second.toOperand(),
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
        if (getCurrentScope() == globalScope && name.equals("main"))
            setStatementAt(lineStack.pop(), ILStatement.jump(ILOperand.immediate(currentFuncDef.getLineNum())));
        scopes.push(new FuncScope(getCurrentScope(), getLineNumber(), currentFuncDef));
    }

    private void addParam(VarDefinition def)
    {
        currentFuncDef.addParam(def);
    }

    private void declareParamVariable()
    {
        addParam(declareVariable(false));
    }

    private void declareParamArray()
    {
        addParam(declareVariable(true));
    }

    private void initFunction()
    {
        getCurrentScope().addDefinition(currentFuncDef.getReturnAddress());
        if (currentFuncDef.getReturnValue() != null)
            getCurrentScope().addDefinition(currentFuncDef.getReturnValue());
        currentFuncDef = null;
    }

    private void setReturnValue() throws InterruptedException
    {
        setReturnValue(valuesStack.pop());
    }

    private void setReturnValue(Value retVal) throws InterruptedException
    {
        assign(getCurrentScope().getVarDefinition(FuncDefinition.RETURN_VAL_VAR_NAME).getAddress(),
                retVal);
    }

    private void returnFromFunction()
    {
        jumpIndirect(getCurrentScope().getVarDefinition(FuncDefinition.RETURN_ADDR_VAR_NAME).getAddress());
    }

    private void endFunction() throws InterruptedException
    {
        FuncScope funcScope = (FuncScope)getCurrentScope();
        if (!funcScope.getFuncDefinition().isVoid())
            setReturnValue(new Value(Value.Type.CONST, 0));
        returnFromFunction();
        endCurrentScope();
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
        List<VarDefinition> args = currentFuncDef.getParams();
        for (int i = args.size() - 1; i >= 0; i--)
        {
            Value arg = valuesStack.pop();
            if (arg == null)
                throw new ArgumentNumberMismatchException(getSourceCodeLineNumber(), currentFuncDef.getId(), args.size());
            assign(args.get(i).getAddress(), arg);
        }
        if (valuesStack.pop() != null)
            throw new ArgumentNumberMismatchException(getSourceCodeLineNumber(), currentFuncDef.getId(), args.size());

        assign(currentFuncDef.getReturnAddress().getAddress(), new Value(Value.Type.CONST, getLineNumber() + 2));

        jumpTo(currentFuncDef.getLineNum());

        valuesStack.push(currentFuncDef.getReturnValue() == null ?
                new Value(Value.Type.VOID, 0) :
                new Value(Value.Type.VAR, currentFuncDef.getReturnValue().getAddress()));
    }

    //endregion

    private void putArrayElement() throws InterruptedException
    {
        Token name = tokenStack.pollFirst();
        if (name == null || name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack");
        VarDefinition def = getCurrentScope().getVarDefinition(name.getValue());
        if (!def.isArray())
            throw new SemanticError(getSourceCodeLineNumber(), "Variable is not an array.");
        Value index = valuesStack.pop();
        multiply(index, new Value(Value.Type.CONST, VARIABLE_SIZE));
        index = valuesStack.pop();
        add(new Value(Value.Type.VAR, def.getAddress()), index);
        valuesStack.push(new Value(Value.Type.INDIRECT, valuesStack.pop().getValue()));
    }

    private void putVariable()
    {
        Token name = tokenStack.pollFirst();
        if (name == null || name.getType() != TokenType.ID)
            throw new IllegalStateException("Variable name not found in the stack");
        VarDefinition def = getCurrentScope().getVarDefinition(name.getValue());
        valuesStack.push(new Value(def.isArray() ? Value.Type.REFERENCE : Value.Type.VAR, def.getAddress()));
    }

    private void putNumber()
    {
        Token num = tokenStack.pollFirst();
        if (num == null || num.getType() != TokenType.NUMBER)
            throw new IllegalStateException("Number found in the stack");
        valuesStack.push(new Value(Value.Type.CONST, Integer.parseInt(num.getValue())));
    }

    private void popValue()
    {
        valuesStack.pop();
    }

    private void assign() throws InterruptedException
    {
        Value right = valuesStack.pop();
        Value left = valuesStack.pop();
        if (right.getType() == Value.Type.REFERENCE && left.getType() != Value.Type.REFERENCE ||
                right.getType() == Value.Type.VOID || left.getType() == Value.Type.VOID)
            throw new TypeMismatchException(getSourceCodeLineNumber(), left.getType(), right.getType());
        addNewStatement(ILStatement.assign(right.toOperand(), left.toOperand()));
        valuesStack.push(right);
    }

    private ILStatement assign(int address, Value value) throws InterruptedException
    {
        ILStatement retVal = ILStatement.assign(value.toOperand(), ILOperand.direct(address));
        statementPipeline.add(retVal);
        return retVal;
    }

    private void jumpTo(int line)
    {
        addNewStatement(ILStatement.jump(ILOperand.direct(line)));
    }

    private void jumpIndirect(int address)
    {
        addNewStatement(ILStatement.jump(ILOperand.indirect(address)));
    }

    private void label()
    {
        lineStack.push(getLineNumber());
    }

    private void save()
    {
        label();
        addNewStatement(null);
    }

    //region Control Flow

    // while statement
    private void _while()
    {
        Integer savedCodeLine = lineStack.pop();
        Value condition = valuesStack.pop();
        Integer label = lineStack.pop();
        setStatementAt(savedCodeLine, ILStatement.jumpFalse(condition.toOperand(), ILOperand.direct(getLineNumber() + 1)));
        jumpTo(label);
    }
    // end while

    // if statement
    private void jpfSave()
    {
        Integer savedCodeLine = lineStack.pop();
        Value condition = valuesStack.pop();
        setStatementAt(savedCodeLine, ILStatement.jumpFalse(condition.toOperand(), ILOperand.direct(getLineNumber() + 1)));
        save();
    }

    private void jp()
    {
        Integer savedCodeLine = lineStack.pop();
        setStatementAt(savedCodeLine, ILStatement.jump(ILOperand.direct(getLineNumber())));
    }
    // end if

    //case statement
    private void jpfCmpSave()
    {
        Integer savedCodeLine = lineStack.pop();
        Value case_value = valuesStack.pop();
        Value condition = valuesStack.pop();
        Value expression = valuesStack.pop();
        setStatementAt(savedCodeLine, ILStatement.jumpFalse(condition.toOperand(), ILOperand.direct(getLineNumber())));
        valuesStack.push(expression);
        Value temp = makeTempResult();
        addNewStatement(ILStatement.equals(expression.toOperand(), case_value.toOperand(), temp.toOperand()));
        save();
    }

    private void jpf()
    {
        Integer savedCodeLine = lineStack.pop();
        Value condition = valuesStack.pop();
        // popping expression
        valuesStack.pop();
        setStatementAt(savedCodeLine, ILStatement.jumpFalse(condition.toOperand(), ILOperand.direct(getLineNumber())));
    }

    private void put1()
    {
        valuesStack.push(new Value(Value.Type.CONST, 1));
    }

    //end case

    // break and continue
    private void continueLoop()
    {
        WhileScope whileScope = getWhileScope();
        if (whileScope == null)
            throw new WrongContinuePositionException(getLineNumber());
        jumpTo(whileScope.getStartLine());
    }

    private WhileScope getWhileScope()
    {
        for (Scope scope : scopes)
        {
            if (scope instanceof WhileScope)
            {
                return (WhileScope)scope;
            }
        }
        return null;
    }

    private void breakLoop()
    {
        BreakableScope breakable = null;
        for (Scope scope : scopes)
            if (scope instanceof BreakableScope)
            {
                breakable = (BreakableScope)scope;
                break;
            }
        if (breakable == null)
            throw new WrongBreakPositionException(getLineNumber());
        int jumpPosition = getNextFreeTempAddress();
        breakable.setBreakAddress(jumpPosition);
        jumpIndirect(jumpPosition);
        // we should set the value for jumpPosition after finishing the scope
    }

    // end break and continue
    //endregion

    public static class Value
    {
        public enum Type
        {
            CONST,
            VAR,
            REFERENCE,
            VOID,
            INDIRECT
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
                case INDIRECT:
                    return ILOperand.indirect(value);
                default:
                    return null;
            }
        }
    }
}

