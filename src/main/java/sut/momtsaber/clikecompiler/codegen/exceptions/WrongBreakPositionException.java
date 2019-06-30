package sut.momtsaber.clikecompiler.codegen.exceptions;

public class WrongBreakPositionException extends SemanticError
{

    public WrongBreakPositionException(int lineNumber)
    {
        super(lineNumber, "No while or switch found for break.");
    }
}
