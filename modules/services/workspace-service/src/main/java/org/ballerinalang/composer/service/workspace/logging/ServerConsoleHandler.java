package org.ballerinalang.composer.service.workspace.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;

/**
 *
 */
public class ServerConsoleHandler extends ConsoleHandler {
    @Override
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        super.setFormatter(newFormatter);
    }
}
