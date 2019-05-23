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

    @Test
    public void test1()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("E -> T R");
        parser.parseAndAddProduction("R -> + T R | EPS");
        parser.parseAndAddProduction("T -> F Y");
        parser.parseAndAddProduction("Y -> * F Y | EPS");
        parser.parseAndAddProduction("F -> ( E ) | -");
        CFG grammar = parser.closeAndProduce();
        ParseContext context = new ParseContext(grammar);


        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.SYMBOL, "-"));
        tokens.add(new Token(TokenType.SYMBOL, "+"));
        tokens.add(new Token(TokenType.SYMBOL, "-"));
        tokens.add(new Token(TokenType.EOF, null));


        ParseTree tree = context.parse(tokens);
        System.out.println(tree.toHumanReadableString());
    }

    @Test
    public void selfReferencingRuleTest()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("A -> * A | EPS");
        CFG grammar = parser.closeAndProduce();
        ParseContext context = new ParseContext(grammar);


        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.SYMBOL, "*"));
        tokens.add(new Token(TokenType.SYMBOL, "*"));
        tokens.add(new Token(TokenType.EOF, null));
        ParseTree tree = context.parse(tokens);
        System.out.println(tree.toHumanReadableString());
    }

}
