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
import org.wso2.ballerina.core.model.statements.BlockStmt;

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
public class Resource implements Node {

    private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

    // TODO Refactor
    private Map<String, Annotation> annotationMap = new HashMap<>();
    private List<Worker> workerList = new ArrayList<>();
    private String name;
    private int stackFrameSize;

    private Annotation[] annotations;
    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private Worker[] workers;
    private BlockStmt resourceBody;
    private SymbolName resourceName;
    private Position position;

    private Application application;

    public Resource() {
    }

    public Resource(String name) {
        this.name = name;
    }

    public Resource(SymbolName name,
                    Position position,
                    Annotation[] annotations,
                    Parameter[] parameters,
                    ConnectorDcl[] connectorDcls,
                    VariableDcl[] variableDcls,
                    Worker[] workers,
                    BlockStmt functionBody) {

        this.resourceName = name;
        this.position = position;
        this.annotations = annotations;
        this.parameters = parameters;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;

        /* To Do : Do we pass multiple workers from the model? */
        this.workers = workers;
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
     * Get all the Annotations associated with a Resource
     *
     * @return list of Annotations
     */
    public Annotation[] getResourceAnnotations() {
        return annotations;
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
     * Get resource body
     *
     * @return returns the block statement
     */
    public BlockStmt getResourceBody() {
        return resourceBody;
    }

    /**
     * Get variable declarations
     *
     * @return returns the variable declarations
     */
    public VariableDcl[] getVariableDcls() {
        return variableDcls;
    }

    public String getName() {
        return name;
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Parameter[] getParameters() {
        return parameters;
    }
    
    public SymbolName getSymbolName() {
        return this.resourceName;
    }

    /**
     * Get the location of this resource in the ballerina source file.
     * 
     * @return  Location of this resource in the ballerina source file.
     */
    public Position getResourceLocation() {
        return position;
    }
}
