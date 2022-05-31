import compiler.Compiler;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main extends PApplet
{
    private static final long LINE_INC = 5;
    private static final int ORIG_X = 15;

    private Compiler compiler = null;
    private long line = 0;
    private long maxLine = 0;

    @Override
    public void settings()
    {
        size(800, 800, P2D);
    }

    @Override
    public void setup()
    {
        frameRate(10);
    }

    @Override
    public void draw()
    {
        background(0);

        float origY = 15 - line * (textAscent() + textDescent());
        try
        {
            compiler = new Compiler(this, "test.txt", "keywords.txt", "rules.txt", "dictionary.csv");

            compiler.parseToken();
            maxLine = 0;

            while(!compiler.done())
            {
                compiler.compileNextToken();

                origY += drawText("input: " + compiler.getInput(), origY, Color.WHITE);
                origY += drawText("output: " + compiler.getOutPut(), origY, Color.WHITE);
                origY += drawText("------------------------------------------------------------------", origY, Color.WHITE);
            }

            drawText("Compilation finish", origY + (textAscent() + textDescent()), Color.GREEN);
        }
        catch (Exception e)
        {
            drawText("error: " + e.getMessage() + "\n" + Arrays.stream(e.getStackTrace()).map(
                    StackTraceElement::toString).collect(Collectors.joining("\n")), origY + (textAscent() + textDescent())*2, Color.RED);
        }
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     * @param text le text sur une ou plusieurs lignes
     * @param y la valeur y où commencer à écrire
     * @param c la couleur du text
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, Color c)
    {
        if(c != null)
            fill(c.getRGB());

        text(text, ORIG_X, y);

        int textLines = text.split("\n").length;
        maxLine += textLines;

        return (textAscent() + textDescent()) * textLines;
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     * @param text le text sur une ou plusieurs lignes
     * @param y la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y)
    {
        return drawText(text, y, null);
    }

    @Override
    public void mouseWheel(MouseEvent event)
    {
        line = Math.min(maxLine, Math.max(0, line + event.getCount()*LINE_INC));
    }

    public static void main(String[] args)
    {
        PApplet.main(Main.class);
    }
}
