package sut.momtsaber.clikecompiler.codegen.exceptions;

public class WrongContinuePositionException extends SemanticError
{
    public WrongContinuePositionException(int lineNumber)
    {
        super(lineNumber, "No while found for continue.");
    }
}
