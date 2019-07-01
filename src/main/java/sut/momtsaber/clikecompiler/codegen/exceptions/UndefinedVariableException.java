package sut.momtsaber.clikecompiler.codegen.exceptions;

public class UndefinedVariableException extends SemanticError
{
    public UndefinedVariableException(int lineNumber, String id)
    {
        super(lineNumber, String.format("Variable '%s' is not defined.", id));
    }
}
