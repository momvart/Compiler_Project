import org.junit.Test;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;

public class ParseTreePrintTest
{
    @Test
    public void testPrinting()
    {
        ParseTree tree = new ParseTree(new CFGNonTerminal(0));
        tree.addTerminal(CFGTerminal.parse("ID"), new Token(TokenType.ID, "a"));
        ParseTree internal = new ParseTree(new CFGNonTerminal(1));
        internal.addTerminal(CFGTerminal.parse("+"), new Token(TokenType.SYMBOL, "+"));
        internal.addTerminal(CFGTerminal.parse("ID"), new Token(TokenType.ID, "b"));
        tree.addNonTerminal(new CFGNonTerminal(1), internal);

        System.out.println(tree.toHumanReadableString());
    }
}
