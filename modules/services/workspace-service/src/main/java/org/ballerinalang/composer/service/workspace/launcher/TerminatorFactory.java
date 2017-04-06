package org.ballerinalang.composer.service.workspace.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher Terminator factory.
 */
public class TerminatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(TerminatorFactory.class);

    public Terminator getTerminator(String os, Command command) {

        if (os.equalsIgnoreCase("unix")) {

            return new TerminatorUnix(command);

        } else if (os.equalsIgnoreCase("windows")) {
            return new TerminatorWindows(command);

        } else {
            logger.error("Unknown Operating System");
        }
        return null;
    }
}
