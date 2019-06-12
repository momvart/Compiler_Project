package sut.momtsaber.clikecompiler.codegen.exceptions;

import sut.momtsaber.clikecompiler.codegen.CodeGenerationContext;

public class OperandTypesException extends SemanticError
{
    public OperandTypesException(int lineNumber, CodeGenerationContext.Value.Type first, CodeGenerationContext.Value.Type second)
    {
        super(lineNumber,
                String.format("Unable to do arithmetic or logical operation on %s and %s", first, second));
    }
}
