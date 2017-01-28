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
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.statements.BlockStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code CallableUnitBuilder} is a builder class responsible for building Functions, Actions and Resources
 * <p/>
 * A CallableUnit represents a Function, an Action or a Resource.
 *
 * @since 0.8.0
 */
class CallableUnitBuilder {

    private SymbolName name;
    private Position position;
    private List<Annotation> annotationList = new ArrayList<>();
    private boolean publicFunc;
    private List<Parameter> parameterList = new ArrayList<>();
    private List<Parameter> returnParamList = new ArrayList<>();
    private List<Worker> workerList = new ArrayList<>();
    private BlockStmt body;

    CallableUnitBuilder() {
    }

    void setName(SymbolName name) {
        this.name = name;
    }

    void addAnnotation(Annotation annotation) {
        this.annotationList.add(annotation);
    }

    void setPublic(boolean isPublic) {
        this.publicFunc = isPublic;
    }

    void addParameter(Parameter param) {
        this.parameterList.add(param);
    }

    void addReturnParameter(Parameter param) {
        this.returnParamList.add(param);
    }

    void addWorker(Worker worker) {
        this.workerList.add(worker);
    }

    void setBody(BlockStmt body) {
        this.body = body;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    BallerinaFunction buildFunction() {
        return new BallerinaFunction(name, position, publicFunc,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                returnParamList.toArray(new Parameter[returnParamList.size()]),
                workerList.toArray(new Worker[workerList.size()]), body);
    }

    Resource buildResource() {
        return new Resource(name, position, annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                workerList.toArray(new Worker[workerList.size()]), body);
    }

    BallerinaAction buildAction() {
        return new BallerinaAction(name, position, annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                returnParamList.toArray(new Parameter[returnParamList.size()]),
                workerList.toArray(new Worker[workerList.size()]), body);
    }

    BTypeConvertor buildTypeConverter() {
        return new BTypeConvertor(name, position, publicFunc,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                returnParamList.toArray(new Parameter[returnParamList.size()]), body);
    }
}
