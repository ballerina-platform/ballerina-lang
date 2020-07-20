/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.ballerinalang.debugadapter.utils.PackageUtils.getFileNameFrom;

/**
 * Suspended debug context related information.
 */
public class SuspendedContext {

    private final VirtualMachine attachedVm;
    private final ThreadReference owningThread;
    private final StackFrame frame;
    private final DebugSourceType sourceType;
    private final Optional<String> orgName;
    private final String breakPointSourcePath;
    private final String fileName;

    public SuspendedContext(Path projectRoot, VirtualMachine vm, ThreadReference threadRef, StackFrame frame) {
        this.attachedVm = vm;
        this.frame = frame;
        this.owningThread = threadRef;
        this.sourceType = projectRoot != null ? DebugSourceType.MODULE : DebugSourceType.SINGLE_FILE;
        this.orgName = projectRoot != null ? Optional.of(PackageUtils.getOrgName(projectRoot)) : Optional.empty();
        this.breakPointSourcePath = getSourcePath(frame, projectRoot);
        this.fileName = getFileNameFrom(this.breakPointSourcePath);
    }

    public VirtualMachine getAttachedVm() {
        return attachedVm;
    }

    public ThreadReference getOwningThread() {
        return owningThread;
    }

    public StackFrame getFrame() {
        return frame;
    }

    public DebugSourceType getSourceType() {
        return sourceType;
    }

    public Optional<String> getOrgName() {
        return orgName;
    }

    public String getBreakPointSourcePath() {
        return breakPointSourcePath;
    }

    private String getSourcePath(StackFrame frame, Path projectRoot) {
        try {
            return PackageUtils.getRectifiedSourcePath(frame.location(), projectRoot);
        } catch (AbsentInformationException e) {
            return "";
        }
    }

    public String getFileName() {
        return fileName;
    }
}
