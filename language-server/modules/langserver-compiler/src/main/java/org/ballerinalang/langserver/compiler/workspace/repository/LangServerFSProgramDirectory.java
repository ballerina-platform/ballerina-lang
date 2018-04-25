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

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.FileSystemProgramDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;

/**
 * FS program directory handler for the language server.
 */
public class LangServerFSProgramDirectory extends FileSystemProgramDirectory {
    private static final CompilerContext.Key<LangServerFSProgramDirectory> LS_PROGRAM_DIRECTORY =
            new CompilerContext.Key<>();

    private Path programDirPath;
    private WorkspaceDocumentManager documentManager;
    private LSContext lsContext;
    private static Converter<Path> converter = null;

    public static LangServerFSProgramDirectory getInstance(CompilerContext context, Path projectDirPath,
                                                           WorkspaceDocumentManager documentManager,
                                                           LSContext lsContext) {
        LangServerFSProgramDirectory lsFSProgramDirectory = context.get(LS_PROGRAM_DIRECTORY);
        if (lsFSProgramDirectory == null) {
            synchronized (LangServerFSProgramDirectory.class) {
                lsFSProgramDirectory = context.get(LS_PROGRAM_DIRECTORY);
                if (lsFSProgramDirectory == null) {
                    lsFSProgramDirectory = new LangServerFSProgramDirectory(context, projectDirPath, documentManager,
                            lsContext);
                }
            }
        }
        lsFSProgramDirectory.documentManager = documentManager;
        lsFSProgramDirectory.lsContext = lsContext;
        if (converter != null) {
            ((LSPathConverter) converter).resetLSContext(lsContext);
        }
        return lsFSProgramDirectory;
    }

    public LangServerFSProgramDirectory(CompilerContext context, Path programDirPath,
                                        WorkspaceDocumentManager documentManager, LSContext lsContext) {
        super(programDirPath);
        context.put(LS_PROGRAM_DIRECTORY, this);
        this.programDirPath = programDirPath;
        this.documentManager = documentManager;
        this.lsContext = lsContext;
    }

    @Override
    public Converter<Path> getConverter() {
        // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
        converter = new LSPathConverter(programDirPath, documentManager, lsContext);
        return converter;
    }
}
