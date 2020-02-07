/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.compiler.workspace.repository;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Lang Server File System Project Directory.
 */
public class LangServerFSProjectDirectory extends FileSystemProjectDirectory {
    private static final Map<Path, LangServerFSProjectDirectory> projectDirs = new HashMap<>();
    private Path projectDirPath;
    private WorkspaceDocumentManager documentManager;

    /**
     * Returns a LangServerFSProjectDirectory instance for a given project root path.
     *
     * Note:
     * It is important to note that we need to keep the same reference of the FileSystemProjectDirectory since the
     * Compiler caches the initial reference of the FileSystemProjectDirectory (i.e. CompilerContext-Singleton).
     *
     * @param projectRootPath project root path
     * @param documentManager document manager need to be assigned
     * @return {@link LangServerFSProjectDirectory}
     */
    public static LangServerFSProjectDirectory getInstance(Path projectRootPath,
                                                           WorkspaceDocumentManager documentManager) {
        LangServerFSProjectDirectory projectDirectory = projectDirs.get(projectRootPath);
        if (projectDirectory == null) {
            synchronized (LangServerFSProjectDirectory.class) {
                projectDirectory = projectDirs.get(projectRootPath);
                if (projectDirectory == null) {
                    projectDirectory = new LangServerFSProjectDirectory(projectRootPath, documentManager);
                }
            }
        }
        projectDirectory.documentManager = documentManager;
        return projectDirectory;
    }

    private LangServerFSProjectDirectory(Path projectDirPath, WorkspaceDocumentManager documentManager) {
        super(projectDirPath);
        LangServerFSProjectDirectory.projectDirs.put(projectDirPath, this);
        this.projectDirPath = projectDirPath;
        this.documentManager = documentManager;
    }

    @Override
    public Converter<Path> getConverter() {
        return new LSPathConverter(projectDirPath, documentManager);
    }

    /**
     * Trigger re-scan of the project root.
     */
    public void rescanProjectRoot() {
        this.scanned = false;
        getSourcePackageNames();
    }
}
