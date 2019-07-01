package sut.momtsaber.clikecompiler.codegen.exceptions;

public class ArgumentNumberMismatchException extends SemanticError
{
    public ArgumentNumberMismatchException(int lineNumber, String functionName, int expected)
    {
        super(lineNumber, String.format("Mismatch in number of arguments of '%s'. Expected: %d",
                functionName,
                expected));
    }
}
