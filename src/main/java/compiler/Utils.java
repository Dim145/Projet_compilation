package compiler;

public final class Utils
{
    public static boolean isNumber(String s)
    {
        if(s == null)
            return false;

        try
        {
            Double.parseDouble(s.replaceAll(",", "."));
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public static boolean isAllLetter(String s)
    {
        for (char c : s.toCharArray())
        {
            if(!Character.isLetter(c))
                return false;
        }

        return true;
    }
}
