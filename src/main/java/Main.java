import compiler.Compiler;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main extends PApplet
{
    private static final long LINE_INC = 5;
    private static final int ORIG_X = 15;

    private static final int STACK_WIDTH = 50;
    private static final int STACK_HEIGHT = 30;

    private static final int MAX_VITESSE = 20;
    private static final int PAS_VITESSE = 100;

    private Compiler compiler = null;
    private long line = 0;
    private long maxLine = 0;

    private Thread autoRun;
    private int timeSleep = 1000;
    private Exception exception = null;

    private long lastChange = 0;
    private String codePath;

    private List<File> testFiles = null;

    @Override
    public void settings()
    {
        size(1000, 800, P2D);
    }

    @Override
    public void setup()
    {
        testFiles = new ArrayList<>();

        createCompiler();
    }

    @Override
    public void draw()
    {
        background(0);

        float origY = 15 - line * (textAscent() + textDescent());

        String vitesse = "Vitesse: " + (MAX_VITESSE - this.timeSleep / PAS_VITESSE + 1) + "/" + MAX_VITESSE;
        text(vitesse, width / 2f - textWidth(vitesse) / 2, origY);

        origY += drawText(String.format("%-21s", "'a' = autorun") + "'arrow up' = accelere\n" + String.format("%-21s",
                        "'r' = reset") + "'arrow down' = decelere\n" + String.format("%-21s", "'espace' = next step") + "\n \n",
                origY, Color.WHITE);

        AtomicInteger i = new AtomicInteger();
        String testFilesStr = testFiles
                .stream()
                .map((f) -> i.getAndIncrement() + " = " + f.getName())
                .collect(Collectors.joining(" , "));

        origY += drawText(testFilesStr, origY, Color.WHITE);

        String currentTestStr = "current testFile: " + this.codePath;
        origY += drawText(currentTestStr, origY, width/2f - textWidth(currentTestStr)/2f - ORIG_X);

        maxLine = 0;

        if(compiler != null)
        {
            stroke(Color.WHITE.getRGB());
            line(0, origY - 2, width, origY - 2);
            origY += (textAscent() + textDescent());

            List<String> tmp = compiler.getStack();

            float baseOrigY = origY;

            for (String st : tmp)
            {
                fill(Color.WHITE.getRGB());
                rect(ORIG_X, origY, STACK_WIDTH, STACK_HEIGHT);
                fill(Color.BLACK.getRGB());
                text(st, ORIG_X + STACK_WIDTH / 2f - textWidth(st) / 2,
                        origY + STACK_HEIGHT / 2f + (textAscent() + textDescent()) / 2);

                origY += STACK_HEIGHT + 5;
            }

            baseOrigY += (origY - baseOrigY) / 2;

            baseOrigY += drawText("input: " + compiler.getInput(), baseOrigY, Color.WHITE, STACK_WIDTH + ORIG_X);
            origY += drawText("output: " + compiler.getOutPut(), baseOrigY, STACK_WIDTH + ORIG_X);

            line(0, origY - 2, width, origY - 2);
            origY += (textAscent() + textDescent());

            if (autoRun != null && autoRun.isAlive())
                origY += drawText("Compilation automatique en cours...", origY + (textAscent() + textDescent()),
                        Color.CYAN);

            if (compiler.done()) origY += drawText("Compilation finish", origY + (textAscent() + textDescent()), Color.GREEN);
        }

        try
        {
            if(lastChange < dataFile(codePath).lastModified())
                origY += drawText("Le fichier a changer, veuillez reset la compilation.", origY + (textAscent() + textDescent()), Color.ORANGE);
        }
        catch (Exception ignored)
        {
            // dans le cas ou le fichier n'existe pas
        }

        if (exception != null)
        {
            origY += drawText("Compilation error\n \n", origY + (textAscent() + textDescent()), Color.RED);

            drawText("error: " + exception.getMessage() + "\n" + Arrays.stream(exception.getStackTrace()).map(
                            StackTraceElement::toString).collect(Collectors.joining("\n")),
                    origY + (textAscent() + textDescent()) * 2, Color.RED);
        }
    }

    @Override
    public void keyPressed()
    {
        switch (key)
        {
            case ' ':
                if (this.autoRun == null && this.compiler != null) nextCompilerState();
                break;
            case 'a':
            case 'A':
                if (this.autoRun != null || this.compiler == null) break;

                this.autoRun = new Thread(() ->
                {
                    while (!compiler.done())
                    {
                        try
                        {
                            if (exception != null) break;

                            nextCompilerState();
                            Thread.sleep(this.timeSleep);
                        }
                        catch (InterruptedException ignored)
                        {

                        }
                    }
                });

                this.autoRun.start();
                break;

            case 'r':
            case 'R':
                if (autoRun == null || !this.autoRun.isAlive()) createCompiler();
                break;
        }

        if(Character.isDigit(key))
        {
            int index = key - '0';

            if(index >= 0 && index < testFiles.size())
            {
                this.codePath = testFiles.get(index).getName();

                if(this.autoRun != null && this.autoRun.isAlive())
                    this.autoRun.stop();

                this.autoRun = null;

                this.createCompiler();
            }
        }

        if (keyCode == DOWN && this.autoRun != null) this.timeSleep = Math.min(PAS_VITESSE * MAX_VITESSE, this.timeSleep + PAS_VITESSE);
        if (keyCode == UP   && this.autoRun != null) this.timeSleep = Math.max(PAS_VITESSE, this.timeSleep - PAS_VITESSE);
    }

    private void nextCompilerState()
    {
        try
        {
            compiler.compileNextToken();
        }
        catch (Exception e)
        {
            if (!compiler.done()) this.exception = e;
        }
    }

    private void createCompiler()
    {
        testFiles.clear();
        File dataRep = new File(dataPath(""));
        for (File f : dataRep.listFiles() )
            if(f.getName().startsWith("test") && f.getName().endsWith(".txt"))
                testFiles.add(f);

        this.codePath = testFiles.get(0).getName();

        try
        {
            compiler = new Compiler(this, codePath, "keywords.txt", "rules.txt", "dictionary.csv");

            lastChange = dataFile(codePath).lastModified();
            compiler.parseToken();

            this.autoRun = null;
            this.exception = null;
        }
        catch (Exception e)
        {
            this.exception = e;
            this.compiler = null;
        }
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     *
     * @param text le text sur une ou plusieurs lignes
     * @param y    la valeur y où commencer à écrire
     * @param c    la couleur du text
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, Color c, float decalageX)
    {
        if (c != null) fill(c.getRGB());

        String[] tab = text.split("\n");

        int textLines = 0;
        for (int i = 0; i < tab.length; i++)
        {
            if(ORIG_X + decalageX + textWidth(tab[i]) >= width)
            {
                int indexDepass = tab[i].length() - 1;

                while(tab[i].charAt(indexDepass) != ' ' || ORIG_X + decalageX + textWidth(tab[i].substring(0, indexDepass)) >= width)
                    indexDepass--;

                tab[i] = tab[i].substring(0, indexDepass) + "\n" + tab[i].substring(indexDepass);
            }

            String[] tmp = tab[i].split("\n");

            for (String s : tmp)
            {
                text(s, ORIG_X + decalageX, y + (textAscent() + textDescent()) * textLines++);
            }
        }

        //text(text, ORIG_X + decalageX, y);

        maxLine += textLines;

        return (textAscent() + textDescent()) * textLines;
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     *
     * @param text le text sur une ou plusieurs lignes
     * @param y    la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, Color c)
    {
        return drawText(text, y, c, 0);
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     *
     * @param text le text sur une ou plusieurs lignes
     * @param y    la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y)
    {
        return drawText(text, y, null);
    }

    /**
     * Dessine le text de la couleur demander, incrémente maxLine du nb de ligne de la string et
     * renvoi la hauteur des lignes écrite.
     *
     * @param text le text sur une ou plusieurs lignes
     * @param y    la valeur y où commencer à écrire
     * @return la hauteur du text écrit (hauteur d'une ligne * nb lignes)
     */
    private float drawText(String text, float y, float decallageX)
    {
        return drawText(text, y, null, decallageX);
    }

    @Override
    public void mouseWheel(MouseEvent event)
    {
        line = Math.min(maxLine, Math.max(0, line + event.getCount() * LINE_INC));
    }

    public static void main(String[] args)
    {
        PApplet.main(Main.class);
    }
}
