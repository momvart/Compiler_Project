package sut.momtsaber.clikecompiler.parser.tree;

import java.util.ArrayList;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;

public class ParseTree
{
    private TreeNode root;

    public ParseTree(CFGNonTerminal head)
    {
        this.root = new TreeNode(head, new ArrayList<>());
    }

    public void addTerminal(CFGTerminal terminal, Token value)
    {
        root.children.add(new LeafNode(terminal, value));
    }

    public void addNonTerminal(CFGNonTerminal nonTerminal, ParseTree tree)
    {
        root.children.add(new TreeNode(nonTerminal, tree.root.children));
    }

    public ArrayList<Node> getChildren()
    {
        return root.children;
    }

    public String toInorderString()
    {
        StringBuilder sb = new StringBuilder();
        this.root.toInorderString(sb);
        return sb.toString();
    }

    public String toHumanReadableString()
    {
        return this.toHumanReadableString(null);
    }

    public String toHumanReadableString(CFG namingConsultant)
    {
        return root.toHumanReadableString(0, namingConsultant);
    }

    private static abstract class Node
    {
        protected CFGSymbol head;

        private Node(CFGSymbol head)
        {
            this.head = head;
        }
    }

    private static class TreeNode extends Node
    {
        private ArrayList<Node> children;

        private TreeNode(CFGSymbol head, ArrayList<Node> children)
        {
            super(head);
            this.children = children;
        }

        private String toHumanReadableString(int level, CFG namingConsultant)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; i++)
                sb.append("  ");
            String shifted = sb.toString();
            sb.append("├―");
            sb.append(namingConsultant == null ? head : namingConsultant.getNonTerminalName((CFGNonTerminal)head));
            for (Node child : children)
            {
                sb.append(System.lineSeparator());
                if (child instanceof TreeNode)
                    sb.append(((TreeNode)child).toHumanReadableString(level + 1, namingConsultant));
                else if (child instanceof LeafNode)
                    sb.append(shifted).append("  ").append("├―").append(((LeafNode)child).value);
            }
            return sb.toString();
        }

        private StringBuilder toInorderString(StringBuilder sb)
        {
            for (Node child : children)
            {
                if (child instanceof TreeNode)
                    ((TreeNode)child).toInorderString(sb);
                else if (child instanceof LeafNode)
                    sb.append(((LeafNode)child).value.getValue()).append(" ");
            }
            return sb;
        }
    }

    private static class LeafNode extends Node
    {
        private Token value;

        private LeafNode(CFGTerminal head, Token value)
        {
            super(head);
            this.value = value;
        }
    }
}
