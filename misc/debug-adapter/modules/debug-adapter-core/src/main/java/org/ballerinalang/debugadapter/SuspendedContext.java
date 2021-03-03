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
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionCompiler;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.ballerinalang.debugadapter.DebugSourceType.PACKAGE;
import static org.ballerinalang.debugadapter.DebugSourceType.SINGLE_FILE;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getFileNameFrom;

/**
 * Context holder for debug suspended state related information.
 */
public class SuspendedContext {

    private final VirtualMachineProxyImpl attachedVm;
    private final ThreadReferenceProxyImpl owningThread;
    private final StackFrameProxyImpl frame;
    private final Project project;
    private final String projectRoot;
    private final DebugSourceType sourceType;

    private Path breakPointSourcePath;
    private String fileName;
    private int lineNumber;
    private Document document;
    private ClassLoaderReference classLoader;
    private DebugExpressionCompiler debugCompiler;

    SuspendedContext(Project project, VirtualMachineProxyImpl vm, ThreadReferenceProxyImpl threadRef,
                     StackFrameProxyImpl frame) {
        this.attachedVm = vm;
        this.owningThread = threadRef;
        this.frame = frame;
        this.project = project;
        this.projectRoot = project.sourceRoot().toAbsolutePath().toString();
        this.sourceType = (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) ? SINGLE_FILE : PACKAGE;
        this.lineNumber = -1;
        this.fileName = null;
        this.breakPointSourcePath = null;
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

    public Optional<Path> getBreakPointSourcePath() {
        if (breakPointSourcePath == null) {
            Optional<Path> sourcePath = getSourcePath(frame);
            sourcePath.ifPresent(path -> breakPointSourcePath = path);
        }
        return Optional.ofNullable(breakPointSourcePath);
    }

    private Optional<Path> getSourcePath(StackFrameProxyImpl frame) {
        try {
            return Optional.ofNullable(PackageUtils.getRectifiedSourcePath(frame.location(), project, projectRoot));
        } catch (AbsentInformationException | InvalidStackFrameException | JdiProxyException e) {
            // Todo - How to handle InvalidStackFrameException?
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public DebugExpressionCompiler getDebugCompiler() {
        if (debugCompiler == null) {
            debugCompiler = new DebugExpressionCompiler(this);
        }
        return debugCompiler;
    }

    public Optional<String> getFileName() {
        if (fileName == null) {
            Optional<Path> breakPointPath = getBreakPointSourcePath();
            if (breakPointPath.isEmpty()) {
                return Optional.empty();
            }
            fileName = getFileNameFrom(breakPointPath.get());
        }
        return Optional.ofNullable(fileName);
    }

    public Optional<String> getFileNameWithExt() {
        return getFileName().isEmpty() ? getFileName() : Optional.of(getFileName().get() + BAL_FILE_EXT);
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

    public Document getDocument() {
        if (document == null) {
            loadDocument();
        }
        return document;
    }

    private void loadDocument() {
        Optional<Path> breakPointSourcePath = getBreakPointSourcePath();
        if (breakPointSourcePath.isEmpty()) {
            return;
        }
        DocumentId documentId = project.documentId(breakPointSourcePath.get());
        Module module = project.currentPackage().module(documentId.moduleId());
        document = module.document(documentId);
    }
}
