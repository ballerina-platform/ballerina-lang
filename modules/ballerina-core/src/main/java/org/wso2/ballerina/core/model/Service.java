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
public class Service implements CompilationUnit, SymbolScope {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    // TODO Refactor
    private SymbolName symbolName;
    private NodeLocation location;
    private SymbolName name;
    private Annotation[] annotations;
    private ConnectorDcl[] connectorDcls;
    private VariableDef[] variableDefs;
    private Resource[] resources;

    public Service(NodeLocation location, SymbolName serviceName, Annotation[] annotations,
                   ConnectorDcl[] connectorDcls, VariableDef[] variableDefs, Resource[] resources) {
        this.location = location;
        this.name = serviceName;
        this.annotations = annotations;
        this.connectorDcls = connectorDcls;
        this.variableDefs = variableDefs;
        this.resources = resources;
    }

    /**
     * @param symbolName Service Identifier
     */
    public Service(SymbolName symbolName) {
        this.name = symbolName;
    }

    /**
     * Get the {@code Identifier} of the Service.
     *
     * @return Service Identifier
     */
    public SymbolName getSymbolName() {
        return name;
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

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    // Methods in the SymbolScope interface

    @Override
    public String getScopeName() {
        return null;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return null;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {

    }

    @Override
    public Symbol resolve(SymbolName name) {
        return null;
    }

}
