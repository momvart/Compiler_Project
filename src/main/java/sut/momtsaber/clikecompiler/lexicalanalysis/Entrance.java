package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;

public interface Entrance
{
    boolean canEnter(char input);

    static Entrance of(char c)
    {
        return input -> input == c;
    }

    static Entrance anyOf(String characters)
    {
        return input -> characters.chars().anyMatch(c -> c == input);
    }

    static Entrance or(Entrance... entrances)
    {
        return input -> Arrays.stream(entrances)
                .anyMatch(entrance -> entrance.canEnter(input));
    }

    static Entrance negative(Entrance entrance)
    {
        return input -> !entrance.canEnter(input);
    }

    Entrance LETTER = Character::isLetter;
    Entrance DIGIT = Character::isDigit;
    Entrance LETTER_OR_DIGIT = Character::isLetterOrDigit;
    Entrance ANY = c -> true;
    Entrance WHITESPACE = Character::isWhitespace;
    Entrance STAR = input -> input == '*';
    Entrance SLASH = input -> input == '/';
}
