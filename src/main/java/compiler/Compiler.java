package compiler;

import processing.core.PApplet;

import java.util.*;
import java.util.stream.Collectors;

public class Compiler
{
    private final String[] languageKeywords;
    private final List<Rule> grammar;
    private final HashMap<String, HashMap<String, String>> dictionary;
    private final Stack<String> stack;
    private int index;
    private final StringBuilder output;
    private String code;
    private PApplet main;
    private List<String> grammarTokens;
    private boolean isCompilFinich;

    public Compiler(PApplet main, String codePath, String keyWordsPath, String rulesPath, String dictionnaryPath)
    {
        this.main = main;

        this.code = String.join("\n", main.loadStrings(codePath))
                          .replaceAll("\r", "")
                          .replaceAll("\n", " ")
                          .replaceAll("\t", " ")
                          .trim();
        this.languageKeywords = String.join("\n", main.loadStrings(keyWordsPath)).split(" ");

        this.grammar = Arrays.stream(main.loadStrings(rulesPath))
                             .map(line -> line.split("\t"))
                             .map(chars -> new Rule(
                                     Integer.parseInt(chars[0]),
                                     chars[1],
                                     chars[2].split(" ")))
                .collect(Collectors.toList());

        String[]   tmpDic    = main.loadStrings(dictionnaryPath);
        String[][] scopedDic = Arrays.stream(tmpDic).map(s -> s.split("\t")).collect(Collectors.toList()).toArray(new String[0][0]);

        HashMap<String, HashMap<String, String>> dictionary = new HashMap<>();
        for (int i = 0; i < scopedDic[0].length; i++)
        {
            String currentToken = scopedDic[0][i];
            HashMap<String, String> dic = new HashMap<>();
            for (int j = 1; j < scopedDic.length; j++)
            {
                dic.put(scopedDic[j][0], scopedDic[j][i]);
            }

            dictionary.put(currentToken, dic);
        }

        this.dictionary = dictionary;

        this.stack = new Stack<>();
        this.index = 0;
        this.output = new StringBuilder();
    }

    public void parseToken() throws Exception
    {
        List<String> tokens = new ArrayList<>();

        for (String token : Arrays.stream(code.split(" ")).filter(t -> t != null && !t.isEmpty()).collect(Collectors.toList()))
        {
            if(Utils.isNumber(token))
            {
                tokens.add("nb");
                continue;
            }

            boolean isReserved = Arrays.asList(languageKeywords).contains(token);
            
            if(isReserved)
            {
                tokens.add(token);
            }
            else if (Utils.isAllLetter(token))
            {
                tokens.add("id");
            }
            else
            {
                throw new Exception("token exception: " + token);
            }
        }

        this.grammarTokens = tokens;

        stack.push("$");
        stack.push("P");
    }

    public boolean done()
    {
        return index == grammarTokens.size();
    }

    public void compileNextToken() throws Exception
    {
        if(this.isCompilFinich && index < grammarTokens.size())
        {
            throw new Exception("Unreachable code: " + grammarTokens.stream().skip(index).collect(Collectors.toList()));
        }

        if(grammarTokens.size() == 0)
        {
            throw new Exception("call \"parseToken()\" first !");
        }

        if(index == grammarTokens.size())
            throw new Exception("compilation is finish");

        String token = grammarTokens.get(index);
        String popValue = stack.pop();

        HashMap<String, String> popDic = dictionary.get(token);

        if(popDic == null)
            throw new Exception("unreconized token in dictionnary");

        String element = popDic.get(popValue);

        if(element == null)
            throw new Exception("Couldn't manage to associate the pop within the current token context");

        output.append(element).append(" ");

        if(Utils.isNumber(element))
        {
            int ruleNum = Integer.parseInt(element);

            Rule rule = grammar.stream().filter(r -> r.number == ruleNum).findFirst().orElse(null);

            if(rule == null)
                throw new Exception("unknown rule n°" + ruleNum + "(" + popValue + " " + token + ")");

            for (int i = rule.grammar.length; i > 0; i--)
            {
                String subToken = rule.grammar[i - 1];

                if(!"ε".equals(subToken))
                    stack.push(subToken);
            }
        }
        else switch (element)
        {
            case "pop": index++;break;
            case "ACC": index++; isCompilFinich = true;break;
        }

        if(done())
            isCompilFinich = true;
    }

    public String getOutPut()
    {
        return output.toString();
    }

    public String getInput()
    {
        return grammarTokens.stream().skip(index).collect(Collectors.joining(" "));
    }
}