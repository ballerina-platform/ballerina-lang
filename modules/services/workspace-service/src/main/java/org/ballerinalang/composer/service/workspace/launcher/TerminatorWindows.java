package org.ballerinalang.composer.service.workspace.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher Terminator Implementation for Windows. ( Xp professional SP2++)
 */
public class TerminatorWindows implements Terminator {


    private Command command;
    private static final Logger logger = LoggerFactory.getLogger(TerminatorUnix.class);

    TerminatorWindows(Command command) {
        this.command = command;
    }

    private String getFindProcessCommand() {
        // escape forward slashes
        String script = this.command.getScript().replace("\\", "\\\\");
        return "cmd /c wmic.exe Process where \"Commandline like '%" + script + "%'\" CALL TERMINATE";
    }

    public void terminate() {
        String script = command.getScript();
        String findProcessCommand = getFindProcessCommand();
        try {
            Process findProcess = Runtime.getRuntime().exec(findProcessCommand);
            findProcess.waitFor();
        } catch (Throwable e) {
            logger.error("Launcher was unable to find the process ID for " + script + ".");
        }
    }
}
