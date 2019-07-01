package sut.momtsaber.clikecompiler.codegen.exceptions;

public class UndefinedFunctionException extends SemanticError
{
    public UndefinedFunctionException(int lineNumber, String id)
    {
        super(lineNumber, String.format("Function '%s' is not defined.", id));
    }
}
