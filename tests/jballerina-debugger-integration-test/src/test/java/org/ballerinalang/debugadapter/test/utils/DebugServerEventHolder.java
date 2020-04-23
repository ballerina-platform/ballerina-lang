package org.ballerinalang.debugadapter.test.utils;

import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Holds all the notifications/responses coming from the debug adapter server.
 */
public class DebugServerEventHolder {

    private ConcurrentLinkedQueue<StoppedEventArguments> stoppedEvents = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<TerminatedEventArguments> terminatedEvents = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<ExitedEventArguments> exitedEvents = new ConcurrentLinkedQueue<>();

    public ConcurrentLinkedQueue<StoppedEventArguments> getStoppedEvents() {
        return stoppedEvents;
    }

    public void setStoppedEvents(
            ConcurrentLinkedQueue<StoppedEventArguments> stoppedEvents) {
        this.stoppedEvents = stoppedEvents;
    }

    public ConcurrentLinkedQueue<TerminatedEventArguments> getTerminatedEvents() {
        return terminatedEvents;
    }

    public void setTerminatedEvents(
            ConcurrentLinkedQueue<TerminatedEventArguments> terminatedEvents) {
        this.terminatedEvents = terminatedEvents;
    }

    public ConcurrentLinkedQueue<ExitedEventArguments> getExitedEventArguments() {
        return exitedEvents;
    }

    public void setExitedEventArguments(
            ConcurrentLinkedQueue<ExitedEventArguments> exitedEventArguments) {
        this.exitedEvents = exitedEventArguments;
    }
}
