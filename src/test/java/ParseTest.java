import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.parser.ParseContext;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;
import sut.momtsaber.clikecompiler.utils.GrammarParser;

public class ParseTest
{
    private ParseContext context;

    private CFG grammar;
    @Before
    public void init()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("E -> T R");
        parser.parseAndAddProduction("R -> + T R | EPS");
        parser.parseAndAddProduction("T -> F Y");
        parser.parseAndAddProduction("Y -> * F Y | EPS");
        parser.parseAndAddProduction("F -> ( E ) | -");
        grammar = parser.closeAndProduce();
        context = new ParseContext(grammar);
    }

    @Test
    public void parse()
    {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.SYMBOL, "-"));
        tokens.add(new Token(TokenType.SYMBOL, "+"));
        tokens.add(new Token(TokenType.SYMBOL, "-"));
        tokens.add(new Token(TokenType.EOF,null));


        ParseTree tree = context.parse(tokens);
        System.out.println(tree.toHumanReadableString());
    }

}
