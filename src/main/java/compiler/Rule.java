package compiler;

public class Rule
{
    public int number;
    public String name;
    public String[] grammar;

    public Rule(int number, String name, String[] grammar)
    {
        this.name = name;
        this.number = number;
        this.grammar = grammar;
    }
}
