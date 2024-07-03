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
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLService;
import io.ballerina.wsdltoballerina.wsdlmodel.SOAPVersion;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.wsdltoballerina.GeneratorConstants.BALLERINA;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SOAP_SOAP11;
import static io.ballerina.wsdltoballerina.GeneratorConstants.SOAP_SOAP12;

public class SOAPClientModulePartGenerator {

    private final WSDLService wsdlService;

    protected SOAPClientModulePartGenerator(WSDLService wsdlService) {
        this.wsdlService = wsdlService;
    }

    protected ModulePartNode getSOAPClientModulePartNode() {
        NodeList<ImportDeclarationNode> importDeclarationNodes =
                AbstractNodeFactory.createNodeList(getImportDeclarations());
        ClassDefinitionNode classDefinitionNode = new SOAPClientGenerator(wsdlService).getClassDefinitionNode();
        List<ModuleMemberDeclarationNode> moduleMemberDeclarations = new ArrayList<>();
        moduleMemberDeclarations.add(classDefinitionNode);
        NodeList<ModuleMemberDeclarationNode> moduleMemberDeclarationNodes =
                AbstractNodeFactory.createNodeList(moduleMemberDeclarations);
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");

        return NodeFactory.createModulePartNode(importDeclarationNodes, moduleMemberDeclarationNodes, eofToken);
    }

    private List<ImportDeclarationNode> getImportDeclarations() {
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(BALLERINA,
                wsdlService.getSoapVersion() == SOAPVersion.SOAP11 ? SOAP_SOAP11 : SOAP_SOAP12);
        return new ArrayList<>(List.of(importForHttp));
    }
}
