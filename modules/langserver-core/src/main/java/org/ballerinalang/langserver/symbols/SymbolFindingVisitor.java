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

package org.ballerinalang.langserver.symbols;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor for creating a flat list of symbols found in a compilation unit.
 */
public class SymbolFindingVisitor extends BLangNodeVisitor {
    private List<SymbolInformation> symbols = new ArrayList<SymbolInformation>();
    private String uri = "";

    public SymbolFindingVisitor(TextDocumentServiceContext documentServiceContext) {
        this.symbols = documentServiceContext.get(DocumentServiceKeys.SYMBOL_LIST_KEY);
        this.uri = documentServiceContext.get(DocumentServiceKeys.FILE_URI_KEY);
    }

    public void visit(BLangCompilationUnit compUnit) {
        compUnit.getTopLevelNodes().forEach(node -> {
            ((BLangNode) node).accept(this);
        });
    }

    public void visit(BLangFunction funcNode) {
        this.addSymbol(funcNode, funcNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangStruct structNode) {
        this.addSymbol(structNode, structNode.symbol, SymbolKind.Class);
    }

    public void visit(BLangService serviceNode) {
        this.addSymbol(serviceNode, serviceNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangConnector connectorNode) {
        this.addSymbol(connectorNode, connectorNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangEnum enumNode) {
        this.addSymbol(enumNode, enumNode.symbol, SymbolKind.Enum);
    }

    public void visit(BLangTransformer transformerNode) {
        this.addSymbol(transformerNode, transformerNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangVariable variableNode) {
        this.addSymbol(variableNode, variableNode.symbol, SymbolKind.Variable);
    }

    public void visit(BLangImportPackage importNode) {
        // Don't add package imports
    }

    public void visit(BLangPackageDeclaration packageDecNode) {
        // Don't add package declarations
    }

    private void addSymbol(BLangNode node, BSymbol balSymbol, SymbolKind kind) {
        SymbolInformation lspSymbol = new SymbolInformation();
        lspSymbol.setName(balSymbol.getName().getValue());
        lspSymbol.setKind(kind);
        lspSymbol.setLocation(new Location(this.uri, getRange(node)));
        this.symbols.add(lspSymbol);
    }

    private Range getRange(BLangNode node) {
        Range r = new Range();

        int startLine = node.getPosition().getStartLine() - 1; // LSP range is 0 based
        int startChar = node.getPosition().getStartColumn() - 1;
        int endLine = node.getPosition().getEndLine() - 1;
        int endChar = node.getPosition().getEndColumn() - 1;

        if (endLine <= 0) {
            endLine = startLine;
        }

        if (endChar <= 0) {
            endChar = startChar + 1;
        }

        r.setStart(new Position(startLine, startChar));
        r.setEnd(new Position(endLine, endChar));

        return r;
    }
}
