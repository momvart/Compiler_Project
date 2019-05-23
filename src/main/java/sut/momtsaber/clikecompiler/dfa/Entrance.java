package sut.momtsaber.clikecompiler.dfa;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;

public interface Entrance<T>
{
    boolean canEnter(T input);

    static <T> Entrance<T> or(Entrance<? super T>... entrances)
    {
        return input -> Arrays.stream(entrances)
                .anyMatch(entrance -> entrance.canEnter(input));
    }

    static <T> Entrance<T> negative(Entrance<T> entrance)
    {
        return input -> !entrance.canEnter(input);
    }

    Entrance<Object> ANY = input -> true;
}
