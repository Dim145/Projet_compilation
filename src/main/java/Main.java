import compiler.Compiler;
import processing.core.PApplet;
import processing.core.PFont;
import processing.event.KeyEvent;
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

    private Thread autoRun;
    private int timeSleep = 1000;

    @Override
    public void settings()
    {
        size(1000, 800, P2D);
    }

    @Override
    public void setup()
    {
        createCompiler();

        PFont font = loadFont("MitraMono-16.vlw");
        textFont(font, 16);
    }

    @Override
    public void draw()
    {
        background(0);

        float origY = 15 - line * (textAscent() + textDescent());

        try
        {
            origY += drawText("Debut de la compilation\n \n ", origY, Color.WHITE);

            maxLine = 0;

            origY += drawText("------------------------------------------------------------------", origY);

            String tmp = compiler.getStackString();

            float baseOrigY = origY;
            origY += drawText(tmp, origY);

            baseOrigY += (origY - baseOrigY)/2;

            baseOrigY += drawText("input: " + compiler.getInput(), baseOrigY, textWidth(tmp) + ORIG_X);
            drawText("output: " + compiler.getOutPut(), baseOrigY, textWidth(tmp) + ORIG_X);

            origY += drawText("------------------------------------------------------------------", origY);

            if(autoRun != null && autoRun.isAlive())
                origY += drawText("Compilation automatique en cours...", origY + + (textAscent() + textDescent()), Color.CYAN);

            if(compiler.done())
                drawText("Compilation finish", origY + (textAscent() + textDescent()), Color.GREEN);
        }
        catch (Exception e)
        {
            drawText("error: " + e.getMessage() + "\n" + Arrays.stream(e.getStackTrace()).map(
                    StackTraceElement::toString).collect(Collectors.joining("\n")), origY + (textAscent() + textDescent())*2, Color.RED);
        }
    }

    @Override
    public void keyPressed()
    {
        switch (key)
        {
            case ' ':
                if(this.autoRun == null)
                    nextCompilerState();
            break;
            case 'a':
            case 'A':
                if(this.autoRun != null)
                    break;

                this.autoRun = new Thread(() ->
                {
                    while(!compiler.done()) try
                    {
                        nextCompilerState();
                        Thread.sleep(this.timeSleep);
                    }
                    catch (InterruptedException ignored)
                    {

                    }
                });

                this.autoRun.start();
            break;

            case 'r':
            case 'R':
                if(autoRun == null || !this.autoRun.isAlive())
                    createCompiler();
            break;
        }

        if(keyCode == DOWN && this.autoRun != null)
            this.timeSleep += 250;

        if(keyCode == UP && this.autoRun != null)
            this.timeSleep = Math.max(250, this.timeSleep - 250);
    }

    private void nextCompilerState()
    {
        if(!compiler.done()) try
        {
            compiler.compileNextToken();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void createCompiler()
    {
        compiler = new Compiler(this, "test.txt", "keywords.txt", "rules.txt", "dictionary.csv");
        try
        {
            compiler.parseToken();

            this.autoRun = null;
        }
        catch (Exception ignored)
        {

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
    private float drawText(String text, float y, Color c, float decalageX)
    {
        if(c != null)
            fill(c.getRGB());

        text(text, ORIG_X + decalageX, y);

        int textLines = text.split("\n").length;
        maxLine += textLines;

        return (textAscent() + textDescent() * 2.5f) * textLines;
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     * @param text le text sur une ou plusieurs lignes
     * @param y la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, Color c)
    {
        return drawText(text, y, c, 0);
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

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     * @param text le text sur une ou plusieurs lignes
     * @param y la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, float decallageX)
    {
        return drawText(text, y, null, decallageX);
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
