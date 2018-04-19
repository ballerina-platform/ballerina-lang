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

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;

/**
 * Lang Server File System Project Directory.
 */
public class LangServerFSProjectDirectory extends FileSystemProjectDirectory {
    private static final CompilerContext.Key<LangServerFSProjectDirectory> LS_PROJECT_DIRECTORY =
            new CompilerContext.Key<>();

    private Path projectDirPath;
    private WorkspaceDocumentManager documentManager;

    public static LangServerFSProjectDirectory getInstance(CompilerContext context, Path projectDirPath,
                                                           WorkspaceDocumentManager documentManager) {
        LangServerFSProjectDirectory lsFSProjectDirectory = context.get(LS_PROJECT_DIRECTORY);
        if (lsFSProjectDirectory == null) {
            synchronized (LangServerFSProjectDirectory.class) {
                lsFSProjectDirectory = context.get(LS_PROJECT_DIRECTORY);
                if (lsFSProjectDirectory == null) {
                    lsFSProjectDirectory = new LangServerFSProjectDirectory(context, projectDirPath, documentManager);
                }
            }
        }
        lsFSProjectDirectory.documentManager = documentManager;
        return lsFSProjectDirectory;
    }

    private LangServerFSProjectDirectory(CompilerContext context, Path projectDirPath,
                                         WorkspaceDocumentManager documentManager) {
        super(projectDirPath);
        context.put(LS_PROJECT_DIRECTORY, this);
        this.projectDirPath = projectDirPath;
        this.documentManager = documentManager;
    }

    @Override
    public Converter<Path> getConverter() {
        return new LSPathConverter(projectDirPath, documentManager);
    }
}
