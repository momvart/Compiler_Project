package sut.momtsaber.clikecompiler.codegen;

import java.util.Deque;
import java.util.LinkedList;

public class CodeGenerationContext
{
    private Deque<Scope> scopes = new LinkedList<>();

    private void beginNewScope()
    {
        scopes.push(new Scope(scopes.element()));
    }

    private void endCurrentScope()
    {
        scopes.pop();
    }
}
