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
import com.sun.jdi.ReferenceType;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.internal.IdentifierUtils.decodeIdentifier;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_ORG;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_PACKAGE_PREFIX;
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

    private Path breakPointSourcePath;
    private String fileName;
    private int lineNumber;
    private Document document;
    private ClassLoaderReference classLoader;
    private final Map<String, String> loadedLangLibVersions;

    SuspendedContext(Project project, String projectRoot, VirtualMachineProxyImpl vm,
                     ThreadReferenceProxyImpl threadRef, StackFrameProxyImpl frame) {
        this.attachedVm = vm;
        this.owningThread = threadRef;
        this.frame = frame;
        this.project = project;
        this.projectRoot = projectRoot;
        this.sourceType = (project instanceof SingleFileProject) ? DebugSourceType.SINGLE_FILE :
                DebugSourceType.PACKAGE;
        this.lineNumber = -1;
        this.fileName = null;
        this.breakPointSourcePath = null;
        this.loadedLangLibVersions = new HashMap<>();
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

    public Map<String, String> getLoadedLangLibVersions() {
        if (loadedLangLibVersions.isEmpty()) {
            populateLangLibVersions();
        }
        return loadedLangLibVersions;
    }

    private void populateLangLibVersions() {
        String langLibPrefix = LANG_LIB_ORG + "." + encodeModuleName(LANG_LIB_PACKAGE_PREFIX);
        for (ReferenceType referenceType : attachedVm.allClasses()) {
            if (!referenceType.name().startsWith(langLibPrefix)) {
                continue;
            }
            String[] split = referenceType.name().split("\\.");
            String langLibVersion = split[2];
            String langLibName = decodeIdentifier(split[1]).split("\\.")[1];
            if (!loadedLangLibVersions.containsKey(langLibName)) {
                loadedLangLibVersions.put(langLibName, langLibVersion);
            }
        }
    }

    private void loadDocument() {
        if (project instanceof BuildProject) {
            BuildProject prj = (BuildProject) project;
            Optional<Path> breakPointSourcePath = getBreakPointSourcePath();
            if (breakPointSourcePath.isEmpty()) {
                return;
            }
            Optional<DocumentId> docId = ProjectLoader.getDocumentId(breakPointSourcePath.get(), prj);
            Module module = prj.currentPackage().module(docId.get().moduleId());
            document = module.document(docId.get());
        } else {
            SingleFileProject prj = (SingleFileProject) project;
            DocumentId docId = prj.currentPackage().getDefaultModule().documentIds().iterator().next();
            document = prj.currentPackage().getDefaultModule().document(docId);
        }
    }
}
