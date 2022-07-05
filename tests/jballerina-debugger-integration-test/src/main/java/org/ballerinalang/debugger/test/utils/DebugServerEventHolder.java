package org.ballerinalang.debugger.test.utils;

import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Holds all the notifications/responses coming from the debug adapter server.
 */
public class DebugServerEventHolder {

    private final ConcurrentLinkedQueue<StoppedEventArguments> stoppedEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TerminatedEventArguments> terminatedEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ExitedEventArguments> exitedEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<OutputEventArguments> outputEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<BreakpointEventArguments> breakpointEvents = new ConcurrentLinkedQueue<>();

    public ConcurrentLinkedQueue<StoppedEventArguments> getStoppedEvents() {
        return stoppedEvents;
    }

    public void addStoppedEvent(StoppedEventArguments event) {
        this.stoppedEvents.add(event);
    }

    public ConcurrentLinkedQueue<TerminatedEventArguments> getTerminatedEvents() {
        return terminatedEvents;
    }

    public void addTerminatedEvent(TerminatedEventArguments event) {
        this.terminatedEvents.add(event);
    }

    public ConcurrentLinkedQueue<ExitedEventArguments> getExitedEvents() {
        return exitedEvents;
    }

    public void addExitedEvent(ExitedEventArguments event) {
        this.exitedEvents.add(event);
    }

    public ConcurrentLinkedQueue<OutputEventArguments> getOutputEvents() {
        return outputEvents;
    }

    public void addOutputEvent(OutputEventArguments event) {
        this.outputEvents.add(event);
    }

    public ConcurrentLinkedQueue<BreakpointEventArguments> getBreakpointEvents() {
        return breakpointEvents;
    }

    public void addBreakpointEvent(BreakpointEventArguments event) {
        this.breakpointEvents.add(event);
    }

    void reset() {
        stoppedEvents.clear();
        breakpointEvents.clear();
        outputEvents.clear();
        // Todo: need to clear termination and exited events?
    }
}
