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
    private final StringBuilder output;
    private final String code;

    private int index;
    private List<String> grammarTokens;
    private boolean isCompilFinish;

    public Compiler(PApplet main, String codePath, String keyWordsPath, String rulesPath, String dictionnaryPath)
    {

        String tmp = String.join("\n", main.loadStrings(main.dataPath(codePath)))
                          .replaceAll("\r", "")
                          .replaceAll("\n", " ")
                          .replaceAll("\t", " ")
                          .trim();
        if(!tmp.endsWith(" ε"))
            tmp += " ε";
        code = tmp;
        this.languageKeywords = String.join("\n", main.loadStrings(main.dataPath(keyWordsPath))).split(" ");

        this.grammar = Arrays.stream(main.loadStrings(main.dataPath(rulesPath)))
                             .map(line -> line.split("\t"))
                             .map(line -> new Rule(
                                     Integer.parseInt(line[0]),
                                     line[1],
                                     line[2].split(" ")))
                .collect(Collectors.toList());

        String[]   tmpDic    = main.loadStrings(main.dataPath(dictionnaryPath));
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
            String[] tmpTokens = new String[2];

            tmpTokens[0] = token;

            if(token.endsWith(";") && token.length() > 1)
            {
                tmpTokens[0] = token.substring(0, token.length() - 1);
                tmpTokens[1] = ";";
            }

            for (String tmpToken : tmpTokens)
            {
                if(tmpToken == null)
                    continue;

                if(Utils.isNumber(tmpToken))
                {
                    tokens.add("nb");
                    continue;
                }

                boolean isReserved = Arrays.asList(languageKeywords).contains(tmpToken);

                if(isReserved)
                {
                    tokens.add(tmpToken);
                }
                else if (Utils.isAllLetter(tmpToken))
                {
                    tokens.add("id");
                }
                else
                {
                    throw new Exception("token exception: " + tmpToken);
                }
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
        if(this.isCompilFinish && index < grammarTokens.size())
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
            case "ACC": index++;isCompilFinish = true;break;
        }

        if(done())
            isCompilFinish = true;
    }

    public String getOutPut()
    {
        return output.toString();
    }

    public String getInput()
    {
        return grammarTokens.stream().skip(index).collect(Collectors.joining(" "));
    }

    public List<String> getStack()
    {
        List<String> list = new ArrayList<>(stack);
        Collections.reverse(list);

        while(list.size() < 7)
            list.add(0, " ");

        return list;
    }
}
