package org.ballerinalang.composer.service.workspace.launcher;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Launcher Terminator Implementation for Unix.
 */
public class TerminatorUnix implements Terminator {
    private Command command;
    private static final Logger logger = LoggerFactory.getLogger(TerminatorUnix.class);

    TerminatorUnix(Command command) {
        this.command = command;
    }

    private String[] getFindProcessCommand(String script) {
        String[] cmd = {
                "/bin/sh",
                "-c",
                "ps -ef | grep " + script + " | grep run | grep -v 'grep' | awk '{print $2}'"
        };
        return cmd;
    }

    public void terminate() {
        String script = command.getScript();
        int processID = -1;
        String[] findProcessCommand = getFindProcessCommand(script);
        BufferedReader reader = null;
        try {
            Process findProcess = Runtime.getRuntime().exec(findProcessCommand);
            findProcess.waitFor();
            reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream(), Charset.defaultCharset()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                try {
                    processID = Integer.parseInt(line);
                    kill(processID);
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
            logger.error("Launcher was unable to find the process ID for " + script + ".");
        } finally {
            if (reader != null) {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    public void kill(int pid) {
        //todo need to put aditional validation
        if (pid < 0) {
            return;
        }
        String killCommand = String.format("kill -9 %d", pid);
        BufferedReader reader = null;
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
            reader = new BufferedReader(new InputStreamReader(kill.getInputStream(), Charset.defaultCharset()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
            }
        } catch (Throwable e) {
            logger.error("Launcher was unable to terminate process:" + pid + ".");
        } finally {
            if (reader != null) {
                IOUtils.closeQuietly(reader);
            }
        }
    }

}
