package sut.momtsaber.clikecompiler.codegen.il;

public class ILOperand
{
    enum OperandType
    {
        DIRECT(""),
        INDIRECT("@"),
        IMMEDIATE("#");

        private String prefix;

        OperandType(String prefix)
        {
            this.prefix = prefix;
        }
    }

    private final OperandType type;
    private final int value;

    public ILOperand(OperandType type, int value)
    {
        this.type = type;
        this.value = value;
    }

    public static ILOperand direct(int address)
    {
        return new ILOperand(OperandType.DIRECT, address);
    }

    public static ILOperand indirect(int address)
    {
        return new ILOperand(OperandType.INDIRECT, address);
    }

    public static ILOperand immediate(int value)
    {
        return new ILOperand(OperandType.IMMEDIATE, value);
    }

    public static String convertToString(ILOperand operand)
    {
        return operand == null ? "" : operand.toString();
    }

    @Override
    public String toString()
    {
        return type.prefix + value;
    }
}
