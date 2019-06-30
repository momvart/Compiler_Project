package sut.momtsaber.clikecompiler.codegen.il;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ILStatement
{
    public static int lineNumber = 0;

    enum CommandType
    {
        ADD("ADD"),
        SUBTRACT("SUB"),
        MULTIPLY("MULT"),
        AND("AND"),
        NOT("NOT"),
        ASSIGN("ASSIGN"),
        EQUALS("EQ"),
        LESS_THAN("LT"),
        JUMP_FALSE("JPF"),
        JUMP("JP"),
        PRINT("PRINT");

        private String str;

        CommandType(String str)
        {
            this.str = str;
        }
    }

    private final int lineNum;
    private final CommandType command;
    private final ILOperand[] operands = new ILOperand[3];

    public ILStatement(CommandType command, ILOperand first, ILOperand second, ILOperand third)
    {
        this.lineNum = lineNumber++;
        this.command = command;
        this.operands[0] = first;
        this.operands[1] = second;
        this.operands[2] = third;
    }

    public static ILStatement add(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.ADD, first, second, result);
    }

    public static ILStatement subtract(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.SUBTRACT, first, second, result);
    }

    public static ILStatement multiply(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.MULTIPLY, first, second, result);
    }

    public static ILStatement and(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.AND, first, second, result);
    }

    public static ILStatement not(ILOperand source, ILOperand dest)
    {
        return new ILStatement(CommandType.NOT, source, dest, null);
    }

    public static ILStatement assign(ILOperand source, ILOperand dest)
    {
        return new ILStatement(CommandType.ASSIGN, source, dest, null);
    }

    public static ILStatement equals_(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.EQUALS, first, second, result);
    }

    public static ILStatement lessThan(ILOperand first, ILOperand second, ILOperand result)
    {
        return new ILStatement(CommandType.LESS_THAN, first, second, result);
    }

    public static ILStatement jumpFalse(ILOperand condition, ILOperand line)
    {
        return new ILStatement(CommandType.JUMP_FALSE, condition, line, null);
    }

    public static ILStatement jump(ILOperand line)
    {
        return new ILStatement(CommandType.JUMP, line, null, null);
    }

    public static ILStatement print(ILOperand value)
    {
        return new ILStatement(CommandType.PRINT, value, null, null);
    }

    @Override
    public String toString()
    {
        return String.format("%d\t(%s)", this.lineNum,
                Stream.concat(Stream.of(command.str), Stream.of(operands).map(ILOperand::convertToString))
                        .collect(Collectors.joining(", ")));
    }
}
