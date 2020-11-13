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
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.ballerinalang.debugadapter.utils.PackageUtils.getFileNameFrom;

/**
 * Suspended debug context related information.
 */
public class SuspendedContext {

    private final VirtualMachineProxyImpl attachedVm;
    private final ThreadReferenceProxyImpl owningThread;
    private final StackFrameProxyImpl frame;
    private final Project project;
    private final String projectRoot;
    private final DebugSourceType sourceType;
    private final String breakPointSourcePath;
    private final String fileName;
    private int lineNumber;
    private ClassLoaderReference classLoader;

    SuspendedContext(Project project, String projectRoot, VirtualMachineProxyImpl vm,
                     ThreadReferenceProxyImpl threadRef, StackFrameProxyImpl frame) {

        this.attachedVm = vm;
        this.owningThread = threadRef;
        this.frame = frame;
        this.project = project;
        this.projectRoot = projectRoot;
        this.breakPointSourcePath = getSourcePath(frame);
        this.sourceType = (project instanceof SingleFileProject) ? DebugSourceType.SINGLE_FILE :
                DebugSourceType.PACKAGE;
        this.fileName = getFileNameFrom(this.breakPointSourcePath);
        this.lineNumber = -1;
    }

    public Project getProject() {
        return project;
    }

    public VirtualMachineProxyImpl getAttachedVm() {
        return attachedVm;
    }

    public ClassLoaderReference getDebuggeeClassLoader() {
        if (classLoader == null) {
            try {
                this.classLoader = frame.location().declaringType().classLoader();
            } catch (JdiProxyException e) {
                this.classLoader = null;
            }
        }
        return classLoader;
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

    public Optional<String> getPackageOrg() {
        return Optional.ofNullable(project.currentPackage().packageOrg().toString());
    }

    public Optional<String> getPackageName() {
        return Optional.ofNullable(project.currentPackage().packageName().toString());
    }

    public Optional<String> getPackageVersion() {
        return Optional.ofNullable(project.currentPackage().packageVersion().toString());
    }

    public Optional<String> getModuleName() {
        return Optional.ofNullable(project.currentPackage().getDefaultModule().moduleName().toString());
    }

    public Path getBreakPointSourcePath() {
        return Paths.get(breakPointSourcePath);
    }

    private String getSourcePath(StackFrameProxyImpl frame) {
        try {
            return PackageUtils.getRectifiedSourcePath(frame.location(), project, projectRoot).toString();
        } catch (AbsentInformationException | InvalidStackFrameException | JdiProxyException e) {
            // Todo - How to handle InvalidStackFrameException?
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        if (lineNumber < 0) {
            try {
                lineNumber = frame.location().lineNumber();
            } catch (JdiProxyException e) {
                lineNumber = -1;
            }
        }
        return lineNumber;
    }
}
