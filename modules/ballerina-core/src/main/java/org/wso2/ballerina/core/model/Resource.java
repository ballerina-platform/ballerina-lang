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
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.ConnectorValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.Executable;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.carbon.messaging.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

    // TODO Refactor
    private Map<String, Annotation> annotationMap = new HashMap<>();
    private List<Worker> workerList = new ArrayList<>();
    private Worker defaultWorker;
    private String name;
    private int stackFrameSize;

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
     * Get all Connections declared within the default Worker scope of the Resource
     *
     * @return list of all the Connections belongs to the default Worker of the Resource
     */
    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
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


    /**
     *  Get resource body
     * @return returns the block statement
     */
    public BlockStmt getResourceBody() {
        return resourceBody;
    }


    /**
     * Get variable declarations
     * @return returns the variable declarations
     */
    public VariableDcl[] getVariableDcls() {
        return variableDcls;
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
        BValueRef[] valueParams = new BValueRef[this.stackFrameSize];
        // Populate MessageValue with CarbonMessages' headers.
        MessageValue messageValue = new MessageValue(context.getCarbonMessage());
        List<Header> headerList = context.getCarbonMessage().getHeaders().getAll();
        messageValue.setHeaderList(headerList);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Populate headers from CarbonMessage.");
            Consumer<Header> headerPrint = (Header header) -> LOG
                    .debug("Header: " + header.getName() + " -> " + header.getValue());
            headerList.forEach(headerPrint);
        }
        valueParams[0] = new BValueRef(messageValue);
        
        int i = 1;
        // Create default values for all declared local variables
        VariableDcl[] variableDcls = this.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            valueParams[i] = BValueRef.getDefaultValue(variableDcl.getTypeC());
            i++;
        }

        for (ConnectorDcl connectorDcl : connectorDcls) {
            Symbol symbol = GlobalScopeHolder.getInstance().getScope().lookup(connectorDcl.getConnectorName());
            if (symbol == null) {
                LOG.error("Connector : " + connectorDcl.getConnectorName() + " not found");
            }
            Connector connector = symbol.getConnector();
            if (connector instanceof AbstractNativeConnector) {
                connector = ((AbstractNativeConnector) connector).getInstance();
            }
            ConnectorValue connectorValue = new ConnectorValue(connector, connectorDcl.getArgExprs());
            valueParams[i] = new BValueRef(connectorValue);
        }
        
        BValueRef[] ret = new BValueRef[1];

        StackFrame stackFrame = new StackFrame(valueParams, ret);
        // ToDo : StackFrame should be added at the upstream components.
        controlStack.pushFrame(stackFrame);
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Parameter[] getParameters() {
        return parameters;
    }
}
