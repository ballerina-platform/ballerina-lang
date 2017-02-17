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
package org.ballerinalang.model.builder;

import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.statements.BlockStmt;

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
