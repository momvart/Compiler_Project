package sut.momtsaber.clikecompiler.codegen.exceptions;

import sut.momtsaber.clikecompiler.errors.CompileError;

public class SemanticError extends CompileError
{
    public SemanticError(int lineNumber, String message)
    {
        super(lineNumber, message);
    }
}
