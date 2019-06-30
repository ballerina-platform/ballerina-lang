package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;


/**
 * Command tests supper class.
 *
 * @since 0.992.0
 */
public abstract class CommandTest {
    protected Path tmpDir;
    protected ByteArrayOutputStream console;
    protected PrintStream printStream;

    protected String readOutput() throws IOException {
        return readOutput(false);
    }

    protected String readOutput(boolean slient) throws IOException {
        String output = "";
        output = console.toString();
        console.close();
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
        if (!slient) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }

    @BeforeClass
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("b7a-cmd-test");
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });
        console.close();
        printStream.close();
    }
}
