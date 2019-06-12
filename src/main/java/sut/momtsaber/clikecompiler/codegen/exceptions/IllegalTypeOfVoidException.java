package sut.momtsaber.clikecompiler.codegen.exceptions;

public class IllegalTypeOfVoidException extends SemanticError
{
    public IllegalTypeOfVoidException(int lineNumber, String varName)
    {
        super(lineNumber, "Illegal type of void for: " + varName);
    }
}
