package sut.momtsaber.clikecompiler.codegen.exceptions;

public class DeclarationOfDeclaredException extends SemanticError
{
    public DeclarationOfDeclaredException(int lineNumber, String id)
    {
        super(lineNumber, "Function or variable is already declared: " + id);
    }
}
