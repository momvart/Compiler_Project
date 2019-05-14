package sut.momtsaber.clikecompiler.utils;

import java.util.ArrayList;

public class FuncProvider
{
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {
        ArrayList<T> newList = new ArrayList<>();
        for (T element : list)
            if (!newList.contains(element))
                newList.add(element);
        return newList;
    }
}
