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
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import org.ballerinalang.toml.model.Manifest;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.Variable;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.PackageUtils.findProjectRoot;

/**
 * Listens and publishes events from JVM.
 */
public class EventBus {
    private final Context context;
    private Breakpoint[] breakpointsList = new Breakpoint[0];
    private Map<Long, ThreadReference> threadsMap = new HashMap<>();

    private HashMap<Long, org.eclipse.lsp4j.debug.StackFrame[]> stackframesMap = new HashMap<>();
    AtomicInteger nextStackFrameId = new AtomicInteger();

    private Map<Long, Variable[]> variablesMap = new HashMap<>();
    private Path projectRoot;
    private String orgName = "";

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
            } else {
                Manifest manifest = TomlParserUtils.getManifest(projectRoot);
                orgName = manifest.getProject().getOrgName();
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

    public Map<Long, Variable[]> getVariablesMap() {
        return variablesMap;
    }

    public HashMap<Long, org.eclipse.lsp4j.debug.StackFrame[]> getStackframesMap() {
        return stackframesMap;
    }

    private void populateMaps() {
        nextStackFrameId.set(1);
        threadsMap = new HashMap<>();
        stackframesMap = new HashMap<>();
        variablesMap = new HashMap<>();
        List<ThreadReference> threadReferences = context.getDebuggee().allThreads();
        threadReferences.stream().forEach(threadReference -> {
            threadsMap.put(threadReference.uniqueID(), threadReference);
            try {
                List<StackFrame> frames = threadReference.frames();
                org.eclipse.lsp4j.debug.StackFrame[] stackFrames =
                        new org.eclipse.lsp4j.debug.StackFrame[frames.size()];
                frames.stream().map(stackFrame -> {
                    org.eclipse.lsp4j.debug.StackFrame dapStackFrame = new org.eclipse.lsp4j.debug.StackFrame();
                    int frameId = nextStackFrameId.getAndIncrement();
                    Source source = new Source();
                    try {
                        String sourcePath = stackFrame.location().sourcePath();
                        if (orgName.length() > 0 && sourcePath.startsWith(orgName)) {
                            sourcePath = sourcePath.replaceFirst(orgName, "src");
                        }

                        source.setPath(projectRoot + File.separator + sourcePath);
                        source.setName(stackFrame.location().sourceName());
                    } catch (AbsentInformationException e) {
                    }
                    dapStackFrame.setId((long) frameId);

                    dapStackFrame.setSource(source);
                    dapStackFrame.setLine((long) stackFrame.location().lineNumber());
                    dapStackFrame.setName(stackFrame.location().method().name());
                    try {
                        List<LocalVariable> localVariables = stackFrame.visibleVariables();
                        Variable[] dapVariables = new Variable[localVariables.size()];
                        stackFrame.getValues(stackFrame.visibleVariables()).
                                entrySet().stream().map(localVariableValueEntry -> {
                            LocalVariable localVariable = localVariableValueEntry.getKey();
                            return getDapVariable(localVariable, localVariableValueEntry.getValue());
                        }).collect(Collectors.toList()).toArray(dapVariables);
                        variablesMap.put((long) frameId, dapVariables);
                    } catch (AbsentInformationException e) {
                    }
                    return dapStackFrame;
                }).collect(Collectors.toList())
                        .toArray(stackFrames);
                stackframesMap.put(threadReference.uniqueID(), stackFrames);
            } catch (IncompatibleThreadStateException e) {
            }
        });

    }

    private Variable getDapVariable(LocalVariable variable, Value value) {
        String balType;
        switch (variable.signature()) {
            case "J":
                balType = "Int";
                break;
            case "I":
                balType = "Byte";
                break;
            case "D":
                balType = "Float";
                break;
            case "Z":
                balType = "Boolean";
                break;
            case "Ljava/lang/String;":
                balType = "String";
                break;
            case "Lorg/ballerinalang/jvm/values/DecimalValue;":
                balType = "Decimal";
                break;
            case "Lorg/ballerinalang/jvm/values/MapValue;":
                balType = "Map";
                break;
            case "Lorg/ballerinalang/jvm/values/TableValue;":
                balType = "Table";
                break;
            case "Lorg/ballerinalang/jvm/values/StreamValue;":
                balType = "Stream";
                break;
            case "Lorg/ballerinalang/jvm/values/ArrayValue;":
                balType = "Array";
                break;
            case "Ljava/lang/Object;":
                balType = "Object";
                break;
            case "Lorg/ballerinalang/jvm/values/ErrorValue;":
                balType = "Error";
                break;
            case "Lorg/ballerinalang/jvm/values/FutureValue;":
                balType = "Future";
                break;
            case "Lorg/ballerinalang/jvm/values/FPValue;":
                balType = "Invokable";
                break;
            case "Lorg/ballerinalang/jvm/values/TypedescValue;":
                balType = "Desc";
                break;
            default:
                balType = "Object";
                break;
        }

        Variable dapVariable = new Variable();
        dapVariable.setName(variable.name());
        dapVariable.setType(balType);
        String stringValue = value == null ? "" : value.toString();
        dapVariable.setValue(stringValue);
        return dapVariable;
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
                                try {
                                    List<String> paths = evt.referenceType().sourcePaths("");
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
                                        Location location = evt.referenceType().locationsOfLine(
                                                breakpoint.getLine().intValue()).get(0);
                                        BreakpointRequest bpReq = context.getDebuggee().eventRequestManager()
                                                .createBreakpointRequest(location);
                                        bpReq.enable();
                                    }

                                } catch (AbsentInformationException e) {

                                }
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
}
