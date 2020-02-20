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
import org.wso2.ballerinalang.compiler.FileSystemProgramDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * FS program directory handler for the language server.
 */
public class LangServerFSProgramDirectory extends FileSystemProgramDirectory {
    private static final Map<Path, LangServerFSProgramDirectory> projectDirs = new HashMap<>();

    private Path programDirPath;
    private WorkspaceDocumentManager documentManager;

    /**
     * Returns a LangServerFSProgramDirectory instance for a given project root path.
     *
     * Note:
     * It is important to note that we need to keep the same reference of the FileSystemProgramDirectory since the
     * Compiler caches the initial reference of the FileSystemProgramDirectory (i.e.CompilerContext-Singleton).
     *
     * @param projectRootPath project root path
     * @param documentManager document manager need to be assigned
     * @return {@link LangServerFSProgramDirectory}
     */
    public static LangServerFSProgramDirectory getInstance(Path projectRootPath,
                                                           WorkspaceDocumentManager documentManager) {
        LangServerFSProgramDirectory programDirectory = projectDirs.get(projectRootPath);
        if (programDirectory == null) {
            synchronized (LangServerFSProgramDirectory.class) {
                programDirectory = projectDirs.get(projectRootPath);
                if (programDirectory == null) {
                    programDirectory = new LangServerFSProgramDirectory(projectRootPath, documentManager);
                }
            }
        }
        programDirectory.documentManager = documentManager;
        return programDirectory;
    }

    private LangServerFSProgramDirectory(Path programDirPath, WorkspaceDocumentManager documentManager) {
        super(programDirPath);
        LangServerFSProgramDirectory.projectDirs.put(programDirPath, this);
        this.programDirPath = programDirPath;
        this.documentManager = documentManager;
    }

    @Override
    public Converter<Path> getConverter() {
        return new LSPathConverter(programDirPath, documentManager);
    }
}
