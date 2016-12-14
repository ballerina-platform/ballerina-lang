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
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.MessageType;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.Executable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code Resource} is a single request handler within a {@code Service}.
 * The resource concept is designed to be access protocol independent.
 * But in the initial release of the language it is intended to work with HTTP.
 * <p>
 * The structure of a ResourceDefinition is as follows:
 * <p>
 * [ResourceAnnotations]
 * resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }*
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Resource implements Executable, Node {

    // TODO Refactor
    private Map<String, Annotation> annotationMap = new HashMap<>();
    private List<Parameter> arguments = new ArrayList<>();
    private List<Worker> workerList = new ArrayList<>();
    private Worker defaultWorker;
    private String name;

    private Annotation[] annotations;
    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private Worker[] workers;
    private BlockStmt resourceBody;
    private SymbolName resourceName;

    public Resource() {
        defaultWorker = new Worker();
    }

    public Resource(String name) {
        defaultWorker = new Worker();
        this.name = name;
    }

    public Resource(SymbolName name,
                    Annotation[] annotations,
                    Parameter[] parameters,
                    ConnectorDcl[] connectorDcls,
                    VariableDcl[] variableDcls,
                    Worker[] workers,
                    BlockStmt functionBody) {

        this.resourceName = name;
        this.annotations = annotations;
        this.parameters = parameters;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;

        /* To Do : Do we pass multiple workers from the model? */
        this.workers = workers;
        defaultWorker = new Worker();
        defaultWorker.addStatement(functionBody);
        this.resourceBody = functionBody;
    }

    /**
     * Get an Annotation from a given name
     *
     * @param name name of the annotation
     * @return Annotation
     */
    public Annotation getAnnotation(String name) {
        /* ToDo : Annotations should be a map. */

        for (Annotation annotation : annotations) {
            if (annotation.getName().equals(name)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Get all the Annotations associated with a Resource
     *
     * @return map of Annotations
     */
    public Map<String, Annotation> getAnnotations() {
        return annotationMap;
    }

    /**
     * Set Annotations
     *
     * @param annotations map of Annotations
     */
    public void setAnnotations(Map<String, Annotation> annotations) {
        this.annotationMap = annotations;
    }

    /**
     * Add an {@code Annotation} to the Resource
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        annotationMap.put(annotation.getName(), annotation);
    }

    /**
     * Get list of Arguments associated with the Resource definition
     *
     * @return list of Arguments
     */
    public List<Parameter> getArguments() {
        return arguments;
    }

    /**
     * Set Arguments list to the Resource
     *
     * @param arguments list of Arguments
     */
    public void setArguments(List<Parameter> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add an {@code Argument} to the Resource
     *
     * @param argument Argument to be added to the Resource definition
     */
    public void addArgument(Parameter argument) {
        arguments.add(argument);
    }


    /**
     * Get all Connections declared within the default Worker scope of the Resource
     *
     * @return list of all the Connections belongs to the default Worker of the Resource
     */
    public List<ConnectorDcl> getConnectorDcls() {
        return defaultWorker.getConnectorDcls();
    }

    /**
     * Assign connections to the default Worker of the Resource
     *
     * @param connectorDcls list of connections to be assigned to the default Worker of the Resource
     */
    public void setConnectorDcls(List<ConnectorDcl> connectorDcls) {
        defaultWorker.setConnectorDcls(connectorDcls);
    }

    /**
     * Add a {@code Connection} to the default Worker of the Resource
     *
     * @param connectorDcl Connection to be added to the default Worker of the Resource
     */
    public void addConnection(ConnectorDcl connectorDcl) {
        defaultWorker.addConnection(connectorDcl);
    }

    /**
     * Get all the variables declared in the default Worker scope of the Resource
     *
     * @return list of all default Worker scoped variables
     */
    public List<VariableDcl> getVariables() {
        return defaultWorker.getVariables();
    }

    /**
     * Assign variables to the default Worker of the Resource
     *
     * @param variables list of variables
     */
    public void setVariables(List<VariableDcl> variables) {
        defaultWorker.setVariables(variables);
    }

    /**
     * Add a {@code Variable} to the default Worker of the Resource
     *
     * @param variable variable to be added default Worker
     */
    public void addVariable(VariableDcl variable) {
        defaultWorker.addVariable(variable);
    }

    /**
     * Get all the Workers associated with a Resource
     *
     * @return list of Workers
     */
    public List<Worker> getWorkers() {
        return workerList;
    }

    /**
     * Assign Workers to the Resource
     *
     * @param workers list of all the Workers
     */
    public void setWorkers(List<Worker> workers) {
        this.workerList = workers;
    }

    /**
     * Add a {@code Worker} to the Resource
     *
     * @param worker Worker to be added to the Resource
     */
    public void addWorker(Worker worker) {
        workerList.add(worker);
    }

    /**
     * Get all the Statements associated with the default Worker
     *
     * @return list of Statements associated with the default Worker
     */
    public List<Statement> getStatements() {
        return defaultWorker.getStatements();
    }

    /**
     * Set Statements to be associated with the default Worker
     *
     * @param statements list of Statements
     */
    public void setStatements(List<Statement> statements) {
        defaultWorker.setStatements(statements);
    }

    /**
     * Add a {@code Statement} to the default Worker in the Resource
     *
     * @param statement a Statement to be added to the default Worker
     */
    public void addStatement(Statement statement) {
        defaultWorker.addStatement(statement);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean execute(Context context, BalCallback callback) {
        populateDefaultMessage(context);
        return defaultWorker.execute(context, callback);
    }

    private void populateDefaultMessage(Context context) {
        // Adding MessageValue to the ControlStack
        ControlStack controlStack = context.getControlStack();
        BValueRef[] valueParams = new BValueRef[1];
        valueParams[0] = new BValueRef(new MessageValue(context.getCarbonMessage()));

        StackFrame stackFrame = new StackFrame(valueParams, null, new BValueRef[0]);
        // ToDo : StackFrame should be added at the upstream components.
        controlStack.pushFrame(stackFrame);

        // ToDo : Use generic identifier for message.
        Parameter paramMessage = new Parameter(new MessageType(), new SymbolName("m"));
        arguments.add(paramMessage);
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
