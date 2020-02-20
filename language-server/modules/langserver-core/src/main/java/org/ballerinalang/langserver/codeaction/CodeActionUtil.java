/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.DocumentServiceOperationContext;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerCache;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Code Action related Utils.
 * 
 * @since 1.0.1
 */
public class CodeActionUtil {
    private static final Logger logger = LoggerFactory.getLogger(CodeActionUtil.class);
    private CodeActionUtil() {
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param identifier Document Identifier
     * @param cursorLine Cursor line
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType topLevelNodeInLine(TextDocumentIdentifier identifier, int cursorLine,
                                            WorkspaceDocumentManager docManager) {
        Optional<Path> filePath = CommonUtil.getPathFromURI(identifier.getUri());
        LSCompilerCache.clearAll();
        if (!filePath.isPresent()) {
            return null;
        }

        try {
            LSContext context = new DocumentServiceOperationContext
                    .ServiceOperationContextBuilder(LSContextOperation.TXT_CODE_ACTION)
                    .build();
            context.put(DocumentServiceKeys.IS_CACHE_SUPPORTED, true);
            context.put(DocumentServiceKeys.FILE_URI_KEY, identifier.getUri());
            context.put(DocumentServiceKeys.DOC_MANAGER_KEY, docManager);
            BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager,
                    LSCustomErrorStrategy.class, false, false);
            String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangPackage evalPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);
            
            Optional<BLangCompilationUnit> filteredCUnit = evalPkg.compUnits.stream()
                    .filter(cUnit ->
                            cUnit.getPosition().getSource().cUnitName.replace("/", CommonUtil.FILE_SEPARATOR)
                                    .equals(relativeSourcePath))
                    .findAny();
            
            if (!filteredCUnit.isPresent()) {
                return null;
            }

            for (TopLevelNode topLevelNode : filteredCUnit.get().getTopLevelNodes()) {
                DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(((BLangNode) topLevelNode).pos);
                if (topLevelNode instanceof BLangService) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.SERVICE;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the service
                        for (BLangFunction resourceFunction : ((BLangService) topLevelNode).resourceFunctions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.RESOURCE;
                            }
                        }
                    }
                }

                if (topLevelNode instanceof BLangImportPackage && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.IMPORTS;
                }

                if (topLevelNode instanceof BLangFunction && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.FUNCTION;
                }
                
                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangRecordTypeNode
                        && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.RECORD;
                }
                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.OBJECT;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the object
                        for (BLangFunction resourceFunction
                                : ((BLangObjectTypeNode) ((BLangTypeDefinition) topLevelNode).typeNode).functions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.OBJECT_FUNCTION;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (CompilationFailedException e) {
            logger.error("Error while compiling the source");
            return null;
        }
    }
}
