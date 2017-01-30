/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.symbols.SymbolScope;

import java.util.Map;

/**
 * A {@code Service} is an HTTP web service described by a Swagger.
 * A Service is the discrete unit of functionality that can be remotely accessed.
 * <p/>
 * <p/>
 * A Service is defined as follows:
 * <p/>
 * [ServiceAnnotations]
 * service ServiceName {
 * ConnectorDeclaration;*
 * VariableDeclaration;*
 * ResourceDefinition;+
 * }
 *
 * @since 0.8.0
 */
@SuppressWarnings("unused")
public class Service implements CompilationUnit, SymbolScope, BLangSymbol {
    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected SymbolName symbolName;

    private Annotation[] annotations;
    private ConnectorDcl[] connectorDcls;
    private VariableDef[] variableDefs;
    private Resource[] resources;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    public Service(NodeLocation location,
                   String name,
                   String pkgPath,
                   SymbolName symbolName,
                   Annotation[] annotations,
                   ConnectorDcl[] connectorDcls,
                   VariableDef[] variableDefs,
                   Resource[] resources,
                   SymbolScope enclosingScope,
                   Map<SymbolName, BLangSymbol> symbolMap) {

        this.location = location;
        this.name = name;
        this.pkgPath = pkgPath;
        this.symbolName = symbolName;
        this.annotations = annotations;
        this.connectorDcls = connectorDcls;
        this.variableDefs = variableDefs;
        this.resources = resources;

        this.enclosingScope = enclosingScope;
        this.symbolMap = symbolMap;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
    }

    public VariableDef[] getVariableDefs() {
        return variableDefs;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    public void setConnectorDcls(ConnectorDcl[] connectorDcls) {
        this.connectorDcls = connectorDcls;
    }

    public void setVariableDefs(VariableDef[] variableDefs) {
        this.variableDefs = variableDefs;
    }

    /**
     * Get all the Resources associated to a Service.
     *
     * @return array of Resources belongs to a Service
     */
    public Resource[] getResources() {
        return resources;
    }

    /**
     * Assign Resources to the Service.
     *
     * @param resources List of Resources
     */
    public void setResources(Resource[] resources) {
        this.resources = resources;
    }


    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }


    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return this;
    }


    // Methods in the SymbolScope interface

    @Override
    public String getScopeName() {
        return null;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public Symbol resolve(SymbolName name) {
        return null;
    }

}
