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

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.types.Type;

import java.util.List;

/**
 * A {@code BallerinaFunction} is an operation that is executed by a {@code Worker}.
 * <p>
 * The structure of a BallerinaFunction is as follows:
 * <p>
 * [FunctionAnnotations]
 * [public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
 * ((TypeName[(, TypeName)*])?) [throws exception] {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }
 *
 * @since 1.0.0
 */
public class BallerinaFunction implements Function {

    private Identifier functionName;

    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Connection> connections;
    private List<VariableDcl> variableDcls;
    private List<Worker> workers;

    private List<Type> returnTypes;
    private BlockStmt functionBody;

    private boolean publicFunc;

    public BallerinaFunction(Identifier name,
                             Boolean isPublic,
                             List<Annotation> annotations,
                             List<Parameter> parameters,
                             List<Type> returnTypes,
                             List<Connection> connections,
                             List<VariableDcl> variableDcls,
                             List<Worker> workers,
                             BlockStmt functionBody) {

        this.functionName = name;
        this.publicFunc = isPublic;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnTypes = returnTypes;
        this.connections = connections;
        this.variableDcls = variableDcls;
        this.workers = workers;
        this.functionBody = functionBody;
    }

    public String getName() {
        return functionName.getName();
    }

    /**
     * Get the function Identifier
     *
     * @return function identifier
     */
    public Identifier getFunctionName() {
        return functionName;
    }

    /**
     * Get all the Annotations associated with a BallerinaFunction
     *
     * @return list of Annotations
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Get list of Arguments associated with the function definition
     *
     * @return list of Arguments
     */
    public List<Parameter> getParameters() {
        return parameters;
    }


    /**
     * Get all Connections declared within the BallerinaFunction scope
     *
     * @return list of all the Connections belongs to a BallerinaFunction
     */
    public List<Connection> getConnections() {
        return connections;
    }


    /**
     * Get all the variableDcls declared in the scope of BallerinaFunction
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    public List<VariableDcl> getVariableDcls() {
        return variableDcls;
    }

    /**
     * Get all the Workers associated with a BallerinaFunction
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    public List<Type> getReturnTypes() {
        return returnTypes;
    }

    /**
     * Check whether function is public, which means function is visible outside the package
     *
     * @return whether function is public
     */
    public boolean isPublic() {
        return publicFunc;
    }

    /**
     * Mark function as public
     */
    public void makePublic() {
        publicFunc = true;
    }

    /**
     * TODO This is the basic implementation of the function interpreter
     *
     * @param ctx
     */
    public void interpret(Context ctx) {
        functionBody.interpret(ctx);
    }
}
