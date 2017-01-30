/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.builder;

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.symbols.SymbolScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code CallableUnitBuilder} is responsible for building Functions, Actions and Resources from parser events.
 * <p/>
 * A CallableUnit represents a Function, an Action or a Resource.
 *
 * @since 0.8.0
 */
class CallableUnitBuilder implements SymbolScope {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;

    private List<Annotation> annotationList = new ArrayList<>();
    private List<ParameterDef> parameterDefList = new ArrayList<>();
    private List<ParameterDef> returnParamList = new ArrayList<>();
    private List<Worker> workerList = new ArrayList<>();
    private BlockStmt body;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap = new HashMap<>();

    CallableUnitBuilder(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }

    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }

    void setName(String name) {
        this.name = name;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    void addAnnotation(Annotation annotation) {
        this.annotationList.add(annotation);
    }

    void addParameter(ParameterDef param) {
        this.parameterDefList.add(param);
    }

    void addReturnParameter(ParameterDef param) {
        this.returnParamList.add(param);
    }

    void addWorker(Worker worker) {
        this.workerList.add(worker);
    }

    void setBody(BlockStmt body) {
        this.body = body;
    }

    @Override
    public String getScopeName() {
        return null;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return this.enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public Symbol resolve(SymbolName name) {
        return null;
    }

    BallerinaFunction buildFunction() {
        return new BallerinaFunction(
                location,
                name,
                pkgPath,
                isPublic,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterDefList.toArray(new ParameterDef[parameterDefList.size()]),
                returnParamList.toArray(new ParameterDef[returnParamList.size()]),
                workerList.toArray(new Worker[workerList.size()]),
                body,
                enclosingScope,
                symbolMap);
    }

    Resource buildResource() {
        return new Resource(
                location,
                name,
                pkgPath,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterDefList.toArray(new ParameterDef[parameterDefList.size()]),
                workerList.toArray(new Worker[workerList.size()]),
                body,
                enclosingScope,
                symbolMap);
    }

    BallerinaAction buildAction() {
        return new BallerinaAction(
                location,
                name,
                pkgPath,
                isPublic,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterDefList.toArray(new ParameterDef[parameterDefList.size()]),
                returnParamList.toArray(new ParameterDef[returnParamList.size()]),
                workerList.toArray(new Worker[workerList.size()]),
                body,
                enclosingScope,
                symbolMap);
    }

    BTypeConvertor buildTypeConverter() {
        return new BTypeConvertor(
                location,
                name,
                pkgPath,
                isPublic,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterDefList.toArray(new ParameterDef[parameterDefList.size()]),
                returnParamList.toArray(new ParameterDef[returnParamList.size()]),
                body,
                enclosingScope,
                symbolMap);
    }
}
