/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.wsdltoballerina;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.wsdltoballerina.recordgenerator.RecordGenerator;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.Field;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLOperation;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLService;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.wsdltoballerina.GeneratorConstants.BALLERINA;
import static io.ballerina.wsdltoballerina.GeneratorConstants.DATA;
import static io.ballerina.wsdltoballerina.GeneratorConstants.XML_DATA;

public class TypeGenerator {
    private final WSDLService wsdlService;

    TypeGenerator(WSDLService wsdlService) {
        this.wsdlService = wsdlService;
    }

    String generate(List<String> operations) throws FormatterException {
        RecordGenerator recordGenerator = new RecordGenerator();
        Map<String, NonTerminalNode> allTypeToTypeDescNodes = new LinkedHashMap<>();
        List<WSDLOperation> wsdlOperations = wsdlService.getWSDLOperations();
        for (WSDLOperation wsdlOperation : wsdlOperations) {
            if (operations.isEmpty() || operations.contains(wsdlOperation.getOperationName())) {
                OperationProcessor operationProcessor = new OperationProcessor(wsdlOperation);
                List<Field> fields = operationProcessor.generateFields();
                Map<String, NonTerminalNode> typeToTypeDescNodes = recordGenerator.generateBallerinaTypes(fields);
                allTypeToTypeDescNodes.putAll(typeToTypeDescNodes);
            }
        }

        boolean usesXmlData = recordGenerator.hasXmlDataUsage();
        NodeList<ImportDeclarationNode> imports;
        if (usesXmlData) {
            imports = AbstractNodeFactory.createNodeList(getImportDeclarations());
        } else {
            imports = AbstractNodeFactory.createEmptyNodeList();
        }

        List<Map.Entry<String, NonTerminalNode>> typeToTypeDescEntries =
                new ArrayList<>(allTypeToTypeDescNodes.entrySet());
        List<TypeDefinitionNode> typeDefNodes = typeToTypeDescEntries.stream()
                .map(entry -> {
                    String recordName = entry.getKey();
                    Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                    IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(recordName);
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(null, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).toList();

        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(new ArrayList<>(typeDefNodes));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        FormattingOptions options = FormattingOptions.builder().build();
        return Formatter.format(modulePartNode.syntaxTree(), options).toSourceCode();
    }

    private List<ImportDeclarationNode> getImportDeclarations() {
        ImportDeclarationNode importForXmlData =
                GeneratorUtils.getImportDeclarationNode(BALLERINA, DATA + "." + XML_DATA);
        return new ArrayList<>(List.of(importForXmlData));
    }
}
