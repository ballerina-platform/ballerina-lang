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
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.InvalidStackFrameException;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.ballerinalang.debugadapter.utils.PackageUtils.findProjectRoot;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getFileNameFrom;

/**
 * Suspended debug context related information.
 */
public class SuspendedContext {

    private final VirtualMachineProxyImpl attachedVm;
    private final ThreadReferenceProxyImpl owningThread;
    private final StackFrameProxyImpl frame;
    private final Path projectRoot;
    private final DebugSourceType sourceType;
    private final String breakPointSourcePath;
    private final String fileName;
    private ClassLoaderReference classLoader;
    private Optional<String> orgName;
    private Optional<String> moduleName;
    private Optional<String> version;

    public SuspendedContext(Path projectRoot, VirtualMachineProxyImpl vm, ThreadReferenceProxyImpl threadRef,
                            StackFrameProxyImpl frame) {
        this.attachedVm = vm;
        this.owningThread = threadRef;
        this.frame = frame;
        this.projectRoot = projectRoot;
        this.breakPointSourcePath = getSourcePath(frame, projectRoot);
        this.sourceType = findProjectRoot(Paths.get(breakPointSourcePath)) != null ? DebugSourceType.MODULE :
                DebugSourceType.SINGLE_FILE;
        this.fileName = getFileNameFrom(this.breakPointSourcePath);
    }

    public VirtualMachineProxyImpl getAttachedVm() {
        return attachedVm;
    }

    public ThreadReferenceProxyImpl getOwningThread() {
        return owningThread;
    }

    public StackFrameProxyImpl getFrame() {
        return frame;
    }

    public DebugSourceType getSourceType() {
        return sourceType;
    }

    public ClassLoaderReference getClassLoader() {
        if (classLoader == null) {
            try {
                this.classLoader = frame.location().declaringType().classLoader();
            } catch (JdiProxyException e) {
                this.classLoader = null;
            }
        }
        return classLoader;
    }

    public Optional<String> getOrgName() {
        if (orgName == null || !orgName.isPresent()) {
            orgName = this.sourceType == DebugSourceType.MODULE ? Optional.of(PackageUtils.getOrgName(projectRoot)) :
                    Optional.empty();
        }
        return orgName;
    }

    public Optional<String> getModuleName() {
        if (moduleName == null || !moduleName.isPresent()) {
            moduleName = breakPointSourcePath.isEmpty() ? Optional.empty() : Optional.of(PackageUtils.getModuleName(
                    breakPointSourcePath));
        }
        return moduleName;
    }

    public Optional<String> getVersion() {
        if (version == null || !version.isPresent()) {
            version = sourceType == DebugSourceType.MODULE ? Optional.of(PackageUtils.getModuleVersion(projectRoot)) :
                    Optional.empty();
        }
        return version;
    }

    public String getBreakPointSourcePath() {
        return breakPointSourcePath;
    }

    private String getSourcePath(StackFrameProxyImpl frame, Path projectRoot) {
        try {
            return PackageUtils.getRectifiedSourcePath(frame.location(), projectRoot);
        } catch (AbsentInformationException | InvalidStackFrameException | JdiProxyException e) {
            // Todo - How to handle InvalidStackFrameException?
            return "";
        }
    }

    public String getFileName() {
        return fileName;
    }
}
