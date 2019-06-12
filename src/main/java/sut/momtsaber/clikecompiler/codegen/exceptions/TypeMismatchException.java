package sut.momtsaber.clikecompiler.codegen.exceptions;

import sut.momtsaber.clikecompiler.codegen.CodeGenerationContext;

public class TypeMismatchException extends SemanticError
{
    public TypeMismatchException(int lineNumber, CodeGenerationContext.Value.Type first, CodeGenerationContext.Value.Type second)
    {
        super(lineNumber, String.format("Type mismatch: %s and %s", first.toString(), second.toString()));
    }
}
