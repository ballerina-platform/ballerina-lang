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

package org.wso2.ballerina.model;

import org.wso2.ballerina.interpreter.Context;
import org.wso2.ballerina.interpreter.Interpreter;
import org.wso2.ballerina.model.statements.Statement;
import org.wso2.ballerina.model.types.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code Function} is an operation that is executed by a {@code Worker}.
 * <p>
 * The structure of a Function is as follows:
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
@SuppressWarnings("unused")
public class Function implements Interpreter {

    private Identifier functionName;

    private List<Annotation> annotations;
    private List<Parameter> parameters;
    private List<Connection> connections;
    private List<VariableDcl> variableDcls;
    private List<Worker> workers;

    private List<Type> returnTypes;
    private Statement functionBody;

    private boolean publicFunc;

    public Function(Identifier name,
                    Boolean isPublic,
                    List<Annotation> annotations,
                    List<Parameter> parameters,
                    List<Type> returnTypes,
                    List<Connection> connections,
                    List<VariableDcl> variableDcls,
                    List<Worker> workers,
                    Statement functionBody) {

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

    /**
     * Get all the Annotations associated with a Function
     *
     * @return list of Annotations
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set list of all the Annotations
     *
     * @param annotations list of Annotations
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an {@code Annotation} to the Function
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<Annotation>();
        }
        annotations.add(annotation);
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
     * Set Arguments list to the function
     *
     * @param parameters list of Arguments
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Add an {@code Argument} to the function
     *
     * @param argument Argument to be added to the function definition
     */
    public void addArgument(Parameter argument) {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        parameters.add(argument);
    }

    /**
     * Get all Connections declared within the Function scope
     *
     * @return list of all the Connections belongs to a Function
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Function
     *
     * @param connections list of connections to be assigned to a Function
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a {@code Connection} to the Function
     *
     * @param connection Connection to be added to the Function
     */
    public void addConnection(Connection connection) {
        if (connections == null) {
            connections = new ArrayList<Connection>();
        }
        connections.add(connection);
    }

    /**
     * Get all the variableDcls declared in the scope of Function
     *
     * @return list of all Function scoped variableDcls
     */
    public List<VariableDcl> getVariableDcls() {
        return variableDcls;
    }

    /**
     * Assign variableDcls to the Function
     *
     * @param variableDcls list of Function
     */
    public void setVariableDcls(List<VariableDcl> variableDcls) {
        this.variableDcls = variableDcls;
    }

    /**
     * Add a {@code Variable} to the Function
     *
     * @param variable variable to be added to the Function
     */
    public void addVariable(VariableDcl variable) {
        if (variableDcls == null) {
            variableDcls = new ArrayList<VariableDcl>();
        }
        variableDcls.add(variable);
    }

    /**
     * Get all the Workers associated with a Function
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    /**
     * Assign Workers to the Function
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    /**
     * Add a {@code Worker} to the Function
     *
     * @param worker Worker to be added to the Resource
     */
    public void addWorker(Worker worker) {
        if (workers == null) {
            workers = new ArrayList<Worker>();
        }
        workers.add(worker);
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

    public String getName() {
        return functionName.getName();
    }
}
