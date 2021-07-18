/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.semantictokens;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.eclipse.lsp4j.SemanticTokens;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Semantic tokens provider class.
 */
public class SemanticTokensHandler {

    private final SemanticTokensContext semanticTokensContext;
    private SemanticModel semanticModel = null;
    private Document document = null;
    private Path filePath = null;

    public SemanticTokensHandler(SemanticTokensContext semanticTokensContext) {

        this.semanticTokensContext = semanticTokensContext;
    }

    public SemanticTokens getSemanticTokens() {

        List<Integer> data = new ArrayList<>();
        String fileUri = this.semanticTokensContext.fileUri();
        Optional<Path> filePathOptional = CommonUtil.getPathFromURI(fileUri);
        if (filePathOptional.isEmpty()) {
            return new SemanticTokens(data);
        }
        this.filePath = filePathOptional.get();
        Optional<Document> docOptional = this.semanticTokensContext.workspace().document(filePath);
        if (docOptional.isEmpty()) {
            return new SemanticTokens(data);
        }
        this.document = docOptional.get();
        SyntaxTree syntaxTree = this.document.syntaxTree();
        if (syntaxTree == null) {
            return new SemanticTokens(data);
        }
        SemanticTokensVisitor semanticTokensVisitor = new SemanticTokensVisitor(data, this);
        semanticTokensVisitor.visitSemanticTokens(syntaxTree.rootNode());
        return new SemanticTokens(data);
    }

    private void setSemanticModel() {

        try {
            if (this.semanticModel == null && this.filePath != null) {
                Optional<Module> module = this.semanticTokensContext.workspace().module(this.filePath);
                module.ifPresent(value -> this.semanticModel = value.getCompilation().getSemanticModel());
            }
        } catch (Throwable e) {
            this.semanticModel = null;
        }
    }

    /**
     * for that file.
     *
     * @return
     */
    public List<Location> getSemanticModelReferences(LinePosition linePosition) {

        this.setSemanticModel();
        if (this.semanticModel == null || this.document == null || this.filePath == null) {
            return new ArrayList<>();
        }
        Path path = this.filePath.getFileName();
        if (path == null) {
            return new ArrayList<>();
        }
        return this.semanticModel.references(this.document, linePosition).stream().filter(location ->
                location.lineRange().filePath().equals(path.toString())).collect(Collectors.toList());
    }

    public Optional<Symbol> getSemanticModelSymbol(Node node) {

        this.setSemanticModel();
        if (this.semanticModel == null) {
            return Optional.empty();
        }
        return this.semanticModel.symbol(node);
    }
}
