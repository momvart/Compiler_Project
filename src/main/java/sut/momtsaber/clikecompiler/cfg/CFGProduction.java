package sut.momtsaber.clikecompiler.cfg;

import java.util.LinkedList;
import java.util.List;

public class CFGProduction
{
    private CFGNonTerminal leftHand;
    private LinkedList<LinkedList<CFGSymbol>> rightHands;

    public CFGProduction(CFGNonTerminal leftHand, LinkedList<LinkedList<CFGSymbol>> rightHands)
    {
        this.leftHand = leftHand;
        this.rightHands = rightHands;
    }

    public CFGNonTerminal getLeftHand()
    {
        return leftHand;
    }

    public LinkedList<LinkedList<CFGSymbol>> getRightHands()
    {
        return rightHands;
    }

    @Override
    public String toString()
    {
        return "CFGProduction{" +
                "leftHand=" + leftHand +
                ", rightHands=" + rightHands +
                '}';
    }
}
