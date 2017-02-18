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

package org.ballerinalang.model;

import org.ballerinalang.model.builder.CallableUnitGroupBuilder;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.Collections;
import java.util.HashMap;
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
public class Service implements CompilationUnit, SymbolScope, BLangSymbol {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected SymbolName symbolName;

    private Annotation[] annotations;
    private Resource[] resources;
    private VariableDefStmt[] variableDefStmts;

    private BallerinaFunction initFunction;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    // Here we need to link a service with it's program. We execute the matching resource
    // when a request is made. At that point, we need to access runtime environment to execute the resource.
    private BLangProgram bLangProgram;

    private Service(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    /**
     * Get all the Resources associated to a Service.
     *
     * @return arrays of Resources belongs to a Service
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

    public VariableDefStmt[] getVariableDefStmts() {
        return variableDefStmts;
    }

    public BallerinaFunction getInitFunction() {
        return initFunction;
    }

    public void setInitFunction(BallerinaFunction initFunction) {
        this.initFunction = initFunction;
    }

    public BLangProgram getBLangProgram() {
        return bLangProgram;
    }

    public void setBLangProgram(BLangProgram bLangProgram) {
        this.bLangProgram = bLangProgram;
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
    public ScopeName getScopeName() {
        return ScopeName.SERVICE;
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
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    /**
     * {@code ServiceBuilder} is responsible for building a {@cdoe Service} node.
     *
     * @since 0.8.0
     */
    public static class ServiceBuilder extends CallableUnitGroupBuilder {
        private Service service;

        public ServiceBuilder(SymbolScope enclosingScope) {
            service = new Service(enclosingScope);
            currentScope = service;
        }

        public Service buildService() {
            this.service.location = this.location;
            this.service.name = this.name;
            this.service.pkgPath = this.pkgPath;
            this.service.symbolName = new SymbolName(name, pkgPath);

            this.service.annotations = this.annotationList.toArray(new Annotation[this.annotationList.size()]);
            this.service.resources = this.resourceList.toArray(new Resource[this.resourceList.size()]);
            this.service.variableDefStmts = this.variableDefStmtList.toArray(
                    new VariableDefStmt[variableDefStmtList.size()]);
            return service;
        }
    }
}
