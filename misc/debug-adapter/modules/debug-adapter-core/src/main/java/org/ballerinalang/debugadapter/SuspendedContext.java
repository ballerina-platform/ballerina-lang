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

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.InvalidStackFrameException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionCompiler;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.DebugSourceType.DEPENDENCY;
import static org.ballerinalang.debugadapter.DebugSourceType.PACKAGE;
import static org.ballerinalang.debugadapter.DebugSourceType.SINGLE_FILE;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getFileNameFrom;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getStackFrameSourcePath;

/**
 * Context holder for debug suspended state related information.
 */
public class SuspendedContext {

    private final ExecutionContext executionContext;
    private final VirtualMachineProxyImpl attachedVm;
    private final ThreadReferenceProxyImpl owningThread;
    private final StackFrameProxyImpl frame;
    private final Project project;

    private DebugSourceType sourceType;
    private Path breakPointSourcePath;
    private String fileName;
    private int lineNumber;
    private Module module;
    private Document document;
    private ClassLoaderReference classLoader;
    private DebugExpressionCompiler debugCompiler;

    SuspendedContext(ExecutionContext executionContext, ThreadReferenceProxyImpl threadRef,
                     StackFrameProxyImpl frame) {
        this.executionContext = executionContext;
        this.owningThread = threadRef;
        this.frame = frame;
        this.attachedVm = executionContext.getDebuggeeVM();
        this.lineNumber = -1;
        this.project = resolveCurrentProject(executionContext.getSourceProject());
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
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
        if (sourceType == null) {
            Project project = getProject();
            switch (project.kind()) {
                case BUILD_PROJECT:
                    sourceType = PACKAGE;
                    break;
                case SINGLE_FILE_PROJECT:
                    sourceType = SINGLE_FILE;
                    break;
                case BALA_PROJECT:
                default:
                    sourceType = DEPENDENCY;
                    break;
            }
        }

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

    public Optional<String> getPackageMajorVersion() {
        return Optional.of(String.valueOf(project.currentPackage().packageVersion().value().major()));
    }

    public Optional<String> getModuleName() {
        return Optional.ofNullable(project.currentPackage().getDefaultModule().moduleName().toString());
    }

    public Optional<Path> getBreakPointSourcePath(Project project) {
        if (breakPointSourcePath == null) {
            Optional<Path> sourcePath = getSourcePath(project, frame);
            sourcePath.ifPresent(path -> breakPointSourcePath = path.normalize());
        }
        return Optional.ofNullable(breakPointSourcePath);
    }

    private Optional<Path> getSourcePath(Project soureceProject, StackFrameProxyImpl frame) {
        try {
            Optional<Map.Entry<Path, DebugSourceType>> pathAndType = getStackFrameSourcePath(frame.location(),
                    soureceProject);
            if (pathAndType.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(pathAndType.get().getKey());
        } catch (InvalidStackFrameException | JdiProxyException e) {
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

    public SemanticModel getSemanticInfo() {
        return getDebugCompiler().getSemanticInfo();
    }

    public Optional<String> getFileName() {
        if (fileName == null) {
            Optional<Path> breakPointPath = getBreakPointSourcePath(project);
            if (breakPointPath.isEmpty()) {
                return Optional.empty();
            }
            fileName = getFileNameFrom(breakPointPath.get());
        }
        return Optional.ofNullable(fileName);
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

    public Module getModule() {
        if (module == null) {
            loadModule();
        }
        return module;
    }

    public Document getDocument() {
        if (document == null) {
            loadDocument();
        }
        return document;
    }

    private void loadModule() {
        Optional<Path> breakPointSourcePath = getBreakPointSourcePath(project);
        if (breakPointSourcePath.isEmpty()) {
            return;
        }
        DocumentId documentId = project.documentId(breakPointSourcePath.get());
        module = project.currentPackage().module(documentId.moduleId());
    }

    private void loadDocument() {
        Optional<Path> breakPointSourcePath = getBreakPointSourcePath(project);
        if (breakPointSourcePath.isEmpty()) {
            return;
        }
        DocumentId documentId = project.documentId(breakPointSourcePath.get());
        module = project.currentPackage().module(documentId.moduleId());
        document = module.document(documentId);
    }

    /**
     * This util method is used to resolve the project instance related to the current debug hit. (Can be a
     * BuildProject, SingleFileProject or BalaProject).
     *
     * @param sourceProject source project (Either BuildProject or SingleFileProject).
     */
    private Project resolveCurrentProject(Project sourceProject) {
        Optional<Path> breakPointSourcePath = getBreakPointSourcePath(sourceProject);
        if (breakPointSourcePath.isPresent()) {
            return executionContext.getProjectCache().getProject(breakPointSourcePath.get());
        }
        return sourceProject;
    }
}
