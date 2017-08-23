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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.PackageNode;

import java.util.List;

/**
 * @since 0.94
 */
public class BLangPackage extends BLangNode implements PackageNode {
    public List<BLangIdentifier> nameComps;
    public BLangIdentifier version;
    public List<BLangImportPackage> imports;
    public List<BLangXMLNS> xmlnsList;
    public List<BLangVariable> globalVars;
    public List<BLangService> services;
    public List<BLangConnector> connectors;
    public List<BLangFunction> functions;
    public List<BLangStruct> structs;
    public List<BLangAnnotation> annotations;

    public BLangPackage(List<BLangIdentifier> nameComps,
                        BLangIdentifier version,
                        List<BLangImportPackage> imports,
                        List<BLangXMLNS> xmlnsList,
                        List<BLangVariable> globalVars,
                        List<BLangService> services,
                        List<BLangConnector> connectors,
                        List<BLangFunction> functions,
                        List<BLangStruct> structs,
                        List<BLangAnnotation> annotations) {
        this.nameComps = nameComps;
        this.version = version;
        this.imports = imports;
        this.xmlnsList = xmlnsList;
        this.globalVars = globalVars;
        this.services = services;
        this.connectors = connectors;
        this.functions = functions;
        this.structs = structs;
        this.annotations = annotations;
    }

    @Override
    public List<? extends IdentifierNode> getNameComponents() {
        return nameComps;
    }

    @Override
    public IdentifierNode getVersion() {
        return version;
    }

    @Override
    public List<BLangImportPackage> getImports() {
        return imports;
    }

    @Override
    public List<BLangXMLNS> getNamespaceDeclarations() {
        return xmlnsList;
    }

    @Override
    public List<BLangVariable> getGlobalVariables() {
        return globalVars;
    }

    @Override
    public List<BLangService> getServices() {
        return services;
    }

    @Override
    public List<BLangConnector> getConnectors() {
        return connectors;
    }

    @Override
    public List<BLangFunction> getFunctions() {
        return functions;
    }

    @Override
    public List<BLangStruct> getStructs() {
        return structs;
    }

    @Override
    public List<BLangAnnotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
