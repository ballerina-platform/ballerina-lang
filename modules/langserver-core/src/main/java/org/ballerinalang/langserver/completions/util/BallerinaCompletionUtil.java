/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.completions.BallerinaCustomErrorStrategy;
import org.ballerinalang.langserver.completions.InMemoryPackageRepository;
import org.ballerinalang.langserver.completions.SuggestionsFilterDataModel;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.consts.CompletionItemResolver;
import org.ballerinalang.langserver.completions.models.ModelPackage;
import org.ballerinalang.langserver.completions.resolvers.DefaultResolver;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;

/**
 * Compilation unit builder is for building ballerina compilation units.
 */
public class BallerinaCompletionUtil {
    private static final String BAL_EXTENSION = ".bal";
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaCompletionUtil.class);
    private Set<Map.Entry<String, ModelPackage>> packages;

    /**
     * Get the Ballerina Compiler instance.
     * @param positionParams - text document position params
     * @return {@link Compiler} Compiler instance
     */
    public static List<CompletionItem> getCompletions(TextDocumentPositionParams positionParams) {
        CompilerContext compilerContext = new CompilerContext();
        SuggestionsFilterDataModel filterDataModel = new SuggestionsFilterDataModel();
        String textContent = getDocumentText(positionParams.getTextDocument().getUri());
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        String compilationUnitId = getRandomCompilationUnitId();
        List<Name> names = new ArrayList<>();
        names.add(new Name("."));
        PackageID tempPackageID = new PackageID(names, new Name("0.0.0"));
        InMemoryPackageRepository inMemoryPackageRepository = new InMemoryPackageRepository(tempPackageID,
                "", compilationUnitId, textContent.getBytes(StandardCharsets.UTF_8));
        options.put(COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());
        compilerContext.put(PackageRepository.class, inMemoryPackageRepository);
        BallerinaCustomErrorStrategy errStrategy = new BallerinaCustomErrorStrategy(compilerContext,
                positionParams, filterDataModel);
        compilerContext.put(DefaultErrorStrategy.class, errStrategy);
        Compiler compiler = Compiler.getInstance(compilerContext);

        compiler.compile(compilationUnitId);
        BLangPackage bLangPackage = (BLangPackage) compiler.getAST();

        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(compilationUnitId, compilerContext, positionParams, filterDataModel);
        bLangPackage.accept(treeVisitor);

        BLangNode symbolEnvNode = filterDataModel.getSymbolEnvNode();
        if (symbolEnvNode == null) {
            return CompletionItemResolver.getResolverByClass(DefaultResolver.class).resolveItems(filterDataModel);
        } else {
            return CompletionItemResolver.getResolverByClass(symbolEnvNode.getClass()).resolveItems(filterDataModel);
        }
    }

    private static String getRandomCompilationUnitId() {
        return UUID.randomUUID().toString().replace("-", "") + BAL_EXTENSION;
    }

    /**
     * Get packages.
     *
     * @return a map contains package details
     */
    public Set<Map.Entry<String, ModelPackage>> getPackages() {
        return this.packages;
    }

    private static String getDocumentText(String uri) {
        String documentText = "";
        try {
            documentText = new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return documentText;
    }
}
