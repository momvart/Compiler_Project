package sut.momtsaber.clikecompiler.dfa;

import java.util.Arrays;

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
