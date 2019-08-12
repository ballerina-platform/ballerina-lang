/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.PackageUtils.findProjectRoot;

/**
 * Listens and publishes events from JVM.
 */
public class EventBus {
    private final Context context;
    private Breakpoint[] breakpointsList = new Breakpoint[0];
    private Map<Long, ThreadReference> threadsMap = new HashMap<>();
    AtomicInteger nextVariableReference = new AtomicInteger();

    private Path projectRoot;

    public EventBus(Context context) {
        this.context = context;
    }

    public void setBreakpointsList(Breakpoint[] breakpointsList) {
        this.breakpointsList = breakpointsList.clone();
        if (this.breakpointsList.length > 0) {
            Breakpoint breakpoint = this.breakpointsList[0];
            projectRoot = findProjectRoot(Paths.get(breakpoint.getSource().getPath()));
            if (projectRoot == null) {
                // calculate projectRoot for single file
                File file = new File(breakpoint.getSource().getPath());
                File parentDir = file.getParentFile();
                projectRoot = parentDir.toPath();
            }
        }
    }

    public Map<Long, ThreadReference> getThreadsMap() {
        List<ThreadReference> threadReferences = context.getDebuggee().allThreads();
        threadReferences.stream().forEach(threadReference -> {
            threadsMap.put(threadReference.uniqueID(), threadReference);
        });
        return threadsMap;
    }

    private void populateMaps() {
        nextVariableReference.set(1);
        threadsMap = new HashMap<>();
        List<ThreadReference> threadReferences = context.getDebuggee().allThreads();
        threadReferences.stream().forEach(threadReference -> {
            threadsMap.put(threadReference.uniqueID(), threadReference);
        });
    }

    public void startListening() {
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    EventSet eventSet = context.getDebuggee().eventQueue().remove();
                    EventIterator eventIterator = eventSet.eventIterator();

                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.next();
                        /*
                         * If this is ClassPrepareEvent, then set breakpoint
                         */
                        if (event instanceof ClassPrepareEvent) {
                            ClassPrepareEvent evt = (ClassPrepareEvent) event;

                            Arrays.stream(this.breakpointsList).forEach(breakpoint -> {
                                addBreakpoint(evt.referenceType(), breakpoint);
                            });
                        }

                        /*
                         * If this is BreakpointEvent, then read & print variables.
                         */
                        if (event instanceof BreakpointEvent) {
                            StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();
                            populateMaps();
                            stoppedEventArguments.setReason(StoppedEventArgumentsReason.BREAKPOINT);
                            stoppedEventArguments.setThreadId(((BreakpointEvent) event).thread().uniqueID());
                            stoppedEventArguments.setAllThreadsStopped(true);
                            context.getClient().stopped(stoppedEventArguments);
                        } else if (event instanceof StepEvent) {
                            populateMaps();
                            StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();
                            stoppedEventArguments.setReason(StoppedEventArgumentsReason.STEP);
                            stoppedEventArguments.setThreadId(((StepEvent) event).thread().uniqueID());
                            stoppedEventArguments.setAllThreadsStopped(true);
                            context.getClient().stopped(stoppedEventArguments);
                        } else if (event instanceof VMDisconnectEvent
                                || event instanceof VMDeathEvent
                                || event instanceof VMDisconnectedException) {
                            ExitedEventArguments exitedEventArguments = new ExitedEventArguments();
                            exitedEventArguments.setExitCode((long) 0);
                            context.getClient().exited(exitedEventArguments);
                        } else {
                            eventSet.resume();
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        );
    }

    public void addBreakpoint(ReferenceType referenceType, Breakpoint breakpoint) {
        try {
            List<String> paths = referenceType.sourcePaths("");
            String balName = paths.size() > 0 ? paths.get(0) : "";

            Path path = Paths.get(breakpoint.getSource().getPath());
            Path projectRoot = findProjectRoot(path);
            String moduleName;
            if (projectRoot == null) {
                moduleName = breakpoint.getSource().getName();
            } else {
                moduleName = PackageUtils.getRelativeFilePath(path.toString());
            }
            if (moduleName.equals(balName)) {
                List<Location> locations = referenceType.locationsOfLine(
                        breakpoint.getLine().intValue());
                if (locations.size() > 0) {
                    Location location = locations.get(0);
                    BreakpointRequest bpReq = context.getDebuggee().eventRequestManager()
                            .createBreakpointRequest(location);
                    bpReq.enable();
                }
            }

        } catch (AbsentInformationException e) {

        }
    }
}
