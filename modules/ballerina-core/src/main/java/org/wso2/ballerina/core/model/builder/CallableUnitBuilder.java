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
import org.wso2.ballerina.core.model.BTypeMapper;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.statements.BlockStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code CallableUnitBuilder} is responsible for building Functions, Actions and Resources from parser events.
 * <p/>
 * A CallableUnit represents a Function, an Action or a Resource.
 *
 * @since 0.8.0
 */
public class CallableUnitBuilder {
    protected NodeLocation location;
    protected SymbolScope currentScope;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;
    protected boolean isNative;

    protected List<Annotation> annotationList = new ArrayList<>();
    protected List<ParameterDef> parameterDefList = new ArrayList<>();
    protected List<ParameterDef> returnParamList = new ArrayList<>();
    protected List<Worker> workerList = new ArrayList<>();
    protected BlockStmt body;

    SymbolScope getCurrentScope() {
        return currentScope;
    }

    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }

    public void setName(String name) {
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

    public void addAnnotation(Annotation annotation) {
        this.annotationList.add(annotation);
    }

    public void addParameter(ParameterDef param) {
        this.parameterDefList.add(param);
    }

    public void addReturnParameter(ParameterDef param) {
        this.returnParamList.add(param);
    }

    public void addWorker(Worker worker) {
        this.workerList.add(worker);
    }

    public void setBody(BlockStmt body) {
        this.body = body;
    }

    public BallerinaFunction buildFunction() {
        return null;
    }

    public Resource buildResource() {
        return null;
    }

    public BallerinaAction buildAction() {
        return null;
    }

    public BTypeMapper buildTypeMapper() {
        return null;
    }

    public Worker buildWorker() {
        return null;
    }
    
    public void setNative(boolean isNative) {
        this.isNative = isNative;
    }
}
