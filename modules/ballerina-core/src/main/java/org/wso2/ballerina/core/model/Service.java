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
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.Executable;
import org.wso2.ballerina.core.runtime.core.dispatching.ResourceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code Service} is an HTTP web service described by a Swagger.
 * A Service is the discrete unit of functionality that can be remotely accessed.
 * <p>
 * <p>
 * A Service is defined as follows:
 * <p>
 * [ServiceAnnotations]
 * service ServiceName {
 * ConnectorDeclaration;*
 * VariableDeclaration;*
 * ResourceDefinition;+
 * }
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Service implements Executable, Node {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    // TODO Refactor
    private SymbolName symbolName;
    private Map<String, Annotation> annotationMap = new HashMap<>();
    private List<Connector> connectors = new ArrayList<>();
    private List<VariableDcl> variables = new ArrayList<>();
    private List<Resource> resourceList = new ArrayList<>();

    private SymbolName name;
    private Annotation[] annotations;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private Resource[] resources;

    public Service(SymbolName serviceName,
                   Annotation[] annotations,
                   ConnectorDcl[] connectorDcls,
                   VariableDcl[] variableDcls,
                   Resource[] resources) {
        this.name = serviceName;
        this.annotations = annotations;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;
        this.resources = resources;
    }

    /**
     * @param symbolName Service Identifier
     */
    public Service(SymbolName symbolName) {
        this.name = symbolName;
    }

    /**
     * Get the {@code Identifier} of the Service
     *
     * @return Service Identifier
     */
    public SymbolName getSymbolName() {
        return name;
    }

    /**
     * Get an Annotation associated with a Service for a given name
     *
     * @param name name of the Annotation
     * @return Annotation
     */
    public Annotation getAnnotation(String name) {
        return annotationMap.get(name);
    }

    /**
     * Get all the Annotations associated with a Service
     *
     * @return Map of Annotations
     */
    public Map<String, Annotation> getAnnotations() {
        return annotationMap;
    }

    /**
     * Set list of all the Annotations
     *
     * @param annotations list of Annotations
     */
    public void setAnnotations(Map<String, Annotation> annotations) {
        this.annotationMap = annotations;
    }

    /**
     * Add an {@code Annotation} to the Service
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        annotationMap.put(annotation.getName(), annotation);
    }

    /**
     * Get all Connectors declared within the Service scope
     *
     * @return list of all the Connectors belongs to a Service
     */
    public List<Connector> getConnectors() {
        return connectors;
    }

    /**
     * Assign connectors to the Service
     *
     * @param connectors list of connectors to be assigned to a Service
     */
    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    /**
     * Add a {@code Connector} to the Service
     *
     * @param connector Connector to be added to the Service
     */
    public void addConnector(Connector connector) {
        connectors.add(connector);
    }

    /**
     * Get all the variables declared in the scope of Service
     *
     * @return list of all Service scoped variables
     */
    public List<VariableDcl> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Service
     *
     * @param variables list of variables
     */
    public void setVariables(List<VariableDcl> variables) {
        this.variables = variables;
    }

    /**
     * Add a {@code Variable} to the Service
     *
     * @param variable variable to be added to the Service
     */
    public void addVariable(VariableDcl variable) {
        variables.add(variable);
    }

    /**
     * Get all the Resources associated to a Service
     *
     * @return array of Resources belongs to a Service
     */
    public Resource[] getResources() {
        return resources;
    }

    /**
     * Assign Resources to the Service
     *
     * @param resources List of Resources
     */
    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    /**
     * Add a {@code Resource} to the Service
     *
     * @param resource a Resource
     */
    public void addResource(Resource resource) {
        resourceList.add(resource);
    }

    @Override
    public boolean execute(Context context, BalCallback callback) {

        String protocol = (String) context.getProperty(Constants.PROTOCOL);

        ResourceDispatcher resourceDispatcher = DispatcherRegistry.getInstance().getResourceDispatcher(protocol);
        if (resourceDispatcher == null) {
            logger.error("No resource dispatcher available to handle protocol : " + protocol);
            return false;
        }

        return resourceDispatcher.dispatch(this, context, callback);
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
