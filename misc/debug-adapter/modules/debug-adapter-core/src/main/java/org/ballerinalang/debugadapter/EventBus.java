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
import com.sun.jdi.IncompatibleThreadStateException;
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
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.toml.model.Manifest;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.utils.PackageUtils.findProjectRoot;

/**
 * Listens and publishes events through JDI.
 */
public class EventBus {

    private Path projectRoot;
    private Map<Long, ThreadReference> threadsMap = new HashMap<>();
    private final DebugContext context;
    private final Map<String, Breakpoint[]> breakpointsList = new HashMap<>();
    private final AtomicInteger nextVariableReference = new AtomicInteger();
    private final List<EventRequest> stepEventRequests = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);

    public EventBus(DebugContext context) {
        this.context = context;
        this.projectRoot = null;
    }

    public void setBreakpointsList(String path, Breakpoint[] breakpointsList) {
        Breakpoint[] breakpoints = breakpointsList.clone();
        this.breakpointsList.put(path, breakpoints);

        if (this.context.getDebuggee() != null) {
            // Setting breakpoints to a already running debug session.
            context.getDebuggee().eventRequestManager().deleteAllBreakpoints();
            Arrays.stream(breakpointsList).forEach(breakpoint -> this.context.getDebuggee().allClasses()
                    .forEach(referenceType -> this.addBreakpoint(referenceType, breakpoint)));
        }

        projectRoot = findProjectRoot(Paths.get(path));
        if (projectRoot == null) {
            // calculate projectRoot for single file
            File file = new File(path);
            File parentDir = file.getParentFile();
            projectRoot = parentDir.toPath();
        }
    }

    public void resetBreakpoints() {
        if (this.context.getDebuggee() == null) {
            return;
        }
        context.getDebuggee().eventRequestManager().deleteAllBreakpoints();
        breakpointsList.forEach((filePath, breakpoints) -> {
            Arrays.stream(breakpoints).forEach(breakpoint -> {
                this.context.getDebuggee().allClasses().forEach(referenceType -> {
                    this.addBreakpoint(referenceType, breakpoint);
                });
            });
        });
    }

    public Map<Long, ThreadReference> getThreadsMap() {
        if (context.getDebuggee() == null) {
            return null;
        }
        List<ThreadReference> threadReferences = context.getDebuggee().allThreads();
        threadReferences.forEach(threadReference -> threadsMap.put(threadReference.uniqueID(), threadReference));
        return threadsMap;
    }

    private void populateMaps() {
        nextVariableReference.set(1);
        threadsMap = new HashMap<>();
        List<ThreadReference> threadReferences = context.getDebuggee().allThreads();
        threadReferences.forEach(threadReference -> threadsMap.put(threadReference.uniqueID(), threadReference));
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
                            this.breakpointsList.forEach((path, breakpoints) -> Arrays.stream(breakpoints)
                                    .forEach(breakpoint -> addBreakpoint(evt.referenceType(), breakpoint)));
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
                            List<EventRequest> stepEventRequests = new ArrayList<>();
                            context.getDebuggee().eventRequestManager().deleteEventRequests(stepEventRequests);
                        } else if (event instanceof StepEvent) {
                            populateMaps();
                            if (((StepEvent) event).location().lineNumber() > 0) {
                                context.getDebuggee().eventRequestManager().deleteEventRequests(stepEventRequests);
                                StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();
                                stoppedEventArguments.setReason(StoppedEventArgumentsReason.STEP);
                                stoppedEventArguments.setThreadId(((StepEvent) event).thread().uniqueID());
                                stoppedEventArguments.setAllThreadsStopped(true);
                                context.getClient().stopped(stoppedEventArguments);
                            } else {
                                long threadId = ((StepEvent) event).thread().uniqueID();
                                this.createStepRequest(threadId, StepRequest.STEP_OVER);
                            }
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
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
    }

    public void addBreakpoint(ReferenceType referenceType, Breakpoint breakpoint) {
        try {
            String sourceReference = getRelativeSourcePath(referenceType, breakpoint);
            String breakpointSource = getSourcePath(breakpoint);
            if (sourceReference.isEmpty() || breakpointSource.isEmpty() || !sourceReference.equals(breakpointSource)) {
                return;
            }

            List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine().intValue());
            if (!locations.isEmpty()) {
                Location location = locations.get(0);
                BreakpointRequest bpReq = context.getDebuggee().eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        } catch (AbsentInformationException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void createStepRequest(long threadId, int stepType) {
        // Make sure there are no existing step events
        context.getDebuggee().eventRequestManager().deleteEventRequests(stepEventRequests);

        ThreadReference threadReference = getThreadsMap().get(threadId);
        StepRequest request = context.getDebuggee().eventRequestManager().createStepRequest(threadReference,
                StepRequest.STEP_LINE, stepType);
        request.setSuspendPolicy(StepRequest.SUSPEND_ALL);

        // TODO change this to a class inclusion filter
        request.addClassExclusionFilter("io.*");
        request.addClassExclusionFilter("com.*");
        request.addClassExclusionFilter("org.*");
        request.addClassExclusionFilter("ballerina.*");
        request.addClassExclusionFilter("java.*");
        request.addClassExclusionFilter("$lambda$main$");

        stepEventRequests.add(request);
        request.addCountFilter(1); // next step only
        request.enable();
        context.getDebuggee().resume();

        // We are resuming all threads, we need to notify debug client about this.
        ContinuedEventArguments continuedEventArguments = new ContinuedEventArguments();
        continuedEventArguments.setAllThreadsContinued(true);
        context.getClient().continued(continuedEventArguments);
    }

    public void createStepOverRequest(long threadId) {
        ThreadReference threadReference = getThreadsMap().get(threadId);
        try {
            Location currentLocation = threadReference.frames().get(0).location();
            ReferenceType referenceType = currentLocation.declaringType();

            List<Location> allLineLocations = currentLocation.method().allLineLocations();
            Optional<Location> lastLocation =
                    allLineLocations.stream().max(Comparator.comparingInt(Location::lineNumber));
            Optional<Location> firstLocation =
                    allLineLocations.stream().min(Comparator.comparingInt(Location::lineNumber));
            // We are going to add breakpoints for each and every line and continue the debugger.

            if (!firstLocation.isPresent()) {
                return;
            }
            int nextStepPoint = firstLocation.get().lineNumber();

            while (true) {
                List<Location> locations = referenceType.locationsOfLine(nextStepPoint);
                if (locations.size() > 0) {
                    Location nextStepLocation = locations.get(0);
                    // Make sure we are step over in the same method.
                    if (nextStepLocation.lineNumber() > currentLocation.lineNumber()) {
                        BreakpointRequest bpReq = context.getDebuggee().eventRequestManager()
                                .createBreakpointRequest(nextStepLocation);
                        bpReq.enable();
                    }
                }
                nextStepPoint++;
                if (nextStepPoint > lastLocation.get().lineNumber()) {
                    break;
                }
            }

            context.getDebuggee().resume();

            // We are resuming all threads, we need to notify debug client about this.
            ContinuedEventArguments continuedEventArguments = new ContinuedEventArguments();
            continuedEventArguments.setAllThreadsContinued(true);
            context.getClient().continued(continuedEventArguments);
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            LOGGER.error(e.getMessage());
            createStepRequest(threadId, StepRequest.STEP_OVER);
        }
    }

    /**
     * Extracts relative path of the source file location using JDI class-reference mappings.
     */
    private static String getRelativeSourcePath(ReferenceType refType, Breakpoint bp)
            throws AbsentInformationException {

        List<String> sourcePaths = refType.sourcePaths("");
        List<String> sourceNames = refType.sourceNames("");
        if (sourcePaths.isEmpty() || sourceNames.isEmpty()) {
            return "";
        }
        String sourcePath = sourcePaths.get(0);
        String sourceName = sourceNames.get(0);

        // Some additional processing is required here to rectify the source path, as the source name will be the
        // relative path instead of the file name, for the ballerina module sources.
        //
        // Note: Directly using file separator as a regex will fail on windows.
        String[] srcNames = sourceName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
        String fileName = srcNames[srcNames.length - 1];
        String relativePath = sourcePath.replace(sourceName, fileName);

        // Replaces org name with the ballerina src directory name, as the JDI path is prepended with the org name
        // for the bal files inside ballerina modules.
        Path projectRoot = findProjectRoot(Paths.get(bp.getSource().getPath()));
        if (projectRoot != null) {
            Manifest manifest = TomlParserUtils.getManifest(projectRoot);
            String orgName = manifest.getProject().getOrgName();
            if (!orgName.isEmpty() && relativePath.startsWith(orgName)) {
                relativePath = relativePath.replaceFirst(orgName, "src");
            }
        }
        return relativePath;
    }

    /**
     * Extracts relative path of the source file related to a given breakpoint.
     */
    private static String getSourcePath(Breakpoint breakpoint) {
        Path path = Paths.get(breakpoint.getSource().getPath());
        Path projectRoot = findProjectRoot(path);
        if (projectRoot == null) {
            return breakpoint.getSource().getName();
        } else {
            Path relativePath = projectRoot.relativize(path);
            return relativePath.toString();
        }
    }
}
