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
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.DocumentServiceOperationContext;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.CodeLens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Provides code lenses related common functionalities.
 *
 * @since 0.984.0
 */
public class CodeLensUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeLensUtil.class);

    /**
     * Compile and get code lenses.
     *
     * @param fileUri         file uri
     * @param documentManager Document Manager
     * @return a list of code lenses
     * @throws CompilationFailedException thrown when compilation error occurs
     */
    public static List<CodeLens> compileAndGetCodeLenses(String fileUri, WorkspaceDocumentManager documentManager)
            throws CompilationFailedException {
        List<CodeLens> lenses = new ArrayList<>();
        LSContext codeLensContext = new DocumentServiceOperationContext
                .ServiceOperationContextBuilder(LSContextOperation.TXT_CODE_LENS)
                .withCommonParams(null, fileUri, documentManager)
                .build();
        BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(codeLensContext, documentManager,
                                                               LSCustomErrorStrategy.class, false, false);
        // Source compilation has no errors, continue
        Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                .filter(cUnit -> (fileUri.endsWith(cUnit.getName())))
                .findFirst();

        CompilerContext compilerContext = codeLensContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        final List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = new ArrayList<>();
        if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            CollectDiagnosticListener listener =
                    (CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class);
            diagnostics.addAll(listener.getDiagnostics());
            listener.clearAll();
        }

        codeLensContext.put(CodeLensesProviderKeys.BLANG_PACKAGE_KEY, bLangPackage);
        codeLensContext.put(CodeLensesProviderKeys.FILE_URI_KEY, fileUri);
        codeLensContext.put(CodeLensesProviderKeys.DIAGNOSTIC_KEY, diagnostics);

        documentCUnit.ifPresent(cUnit -> {
            codeLensContext.put(CodeLensesProviderKeys.COMPILATION_UNIT_KEY, cUnit);

            List<LSCodeLensesProvider> providers = LSCodeLensesProviderHolder.getInstance().getProviders();
            for (LSCodeLensesProvider provider : providers) {
                try {
                    lenses.addAll(provider.getLenses(codeLensContext));
                } catch (LSCodeLensesProviderException e) {
                    LOGGER.error("Error while retrieving lenses from: " + provider.getName());
                }
            }
        });
        return lenses;
    }


    /**
     * Calculate and returns topmost position of the annotations.
     *
     * @param annotationAttachments a list of {@link BLangAnnotationAttachment}
     * @param initialValue          initial position
     * @return calculated topmost position for the node
     */
    public static int getTopMostLocOfAnnotations(List<BLangAnnotationAttachment> annotationAttachments,
                                                 int initialValue) {
        int topMost = initialValue;
        if (annotationAttachments != null) {
            for (BLangAnnotationAttachment attachment : annotationAttachments) {
                topMost = Math.min(attachment.pos.sLine - 1, topMost);
            }
        }
        return topMost;
    }

    /**
     * Calculate and returns topmost position of the documentations.
     *
     * @param docs         {@link BLangMarkdownDocumentation} markdown docs
     * @param initialValue initial position
     * @return calculated topmost position for the node
     */
    public static int getTopMostLocOfDocs(BLangMarkdownDocumentation docs, int initialValue) {
        int topMost = initialValue;
        if (docs != null) {
            for (BLangMarkdownDocumentationLine line : docs.documentationLines) {
                topMost = Math.min(line.pos.sLine - 1, topMost);
            }
        }
        return topMost;
    }
}
