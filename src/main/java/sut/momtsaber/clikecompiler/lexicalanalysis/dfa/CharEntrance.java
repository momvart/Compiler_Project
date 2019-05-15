package sut.momtsaber.clikecompiler.lexicalanalysis.dfa;

import sut.momtsaber.clikecompiler.dfa.Entrance;

public interface CharEntrance extends Entrance<Character>
{
    static CharEntrance of(char c)
    {
        return input -> input == c;
    }

    static CharEntrance anyOf(String characters)
    {
        return input -> characters.chars().anyMatch(c -> c == input);
    }

    CharEntrance LETTER = Character::isLetter;
    CharEntrance DIGIT = Character::isDigit;
    CharEntrance LETTER_OR_DIGIT = Character::isLetterOrDigit;
    CharEntrance ANY = c -> true;
    CharEntrance WHITESPACE = Character::isWhitespace;
    CharEntrance STAR = input -> input == '*';
    CharEntrance SLASH = input -> input == '/';
}
