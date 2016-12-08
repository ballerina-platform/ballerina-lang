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
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.BalContext;
import org.wso2.ballerina.core.runtime.core.Executable;
import org.wso2.ballerina.core.runtime.core.dispatching.ResourceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  A {@code Service} is an HTTP web service described by a Swagger.
 *  A Service is the discrete unit of functionality that can be remotely accessed.
 *  <p>
 *
 *  A Service is defined as follows:
 *
 *  [ServiceAnnotations]
 *  service ServiceName {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      ResourceDefinition;+
 *  }
 *
 *  @since 1.0.0
 */
@SuppressWarnings("unused")
public class Service implements Executable {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private Identifier identifier;
    private Map<String, Annotation> annotations;
    private List<Connection> connections;
    private List<VariableDcl> variables;
    private List<Resource> resources;

    /**
     *
     * @param identifier Service Identifier
     */
    public Service(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the {@code Identifier} of the Service
     *
     * @return Service Identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Get an Annotation associated with a Service for a given name
     *
     * @param name name of the Annotation
     * @return Annotation
     */
    public Annotation getAnnotation(String name) {
        return annotations.get(name);
    }

    /**
     * Get all the Annotations associated with a Service
     * @return Map of Annotations
     */
    public Map<String, Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set list of all the Annotations
     *
     * @param annotations list of Annotations
     */
    public void setAnnotations(Map<String, Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an {@code Annotation} to the Service
     *
     * @param annotation Annotation to be added
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new HashMap<>();
        }
        annotations.put(annotation.getName(), annotation);
    }

    /**
     * Get all Connections declared within the Service scope
     *
     * @return list of all the Connections belongs to a Service
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Assign connections to the Service
     *
     * @param connections list of connections to be assigned to a Service
     */
    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    /**
     * Add a {@code Connection} to the Service
     *
     * @param connection Connection to be added to the Service
     */
    public void addConnection(Connection connection) {
        if (connections == null) {
            connections = new ArrayList<Connection>();
        }
        connections.add(connection);
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
        if (variables == null) {
            variables = new ArrayList<VariableDcl>();
        }
        variables.add(variable);
    }

    /**
     * Get all the Resources associated to a Service
     *
     * @return list of Resources belongs to a Service
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * Assign Resources to the Service
     *
     * @param resources List of Resources
     */
    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    /**
     * Add a {@code Resource} to the Service
     *
     * @param resource a Resource
     */
    public void addResource(Resource resource) {
        if (resources == null) {
            resources = new ArrayList<Resource>();
        }
        resources.add(resource);
    }

    @Override
    public boolean execute(BalContext context, BalCallback callback) {

        String protocol = (String) context.getProperty(Constants.PROTOCOL);

        ResourceDispatcher resourceDispatcher = DispatcherRegistry.getInstance().getResourceDispatcher(protocol);
        if (resourceDispatcher == null) {
            logger.error("No service dispatcher available to handle protocol : " + protocol);
            return false;
        }

        return resourceDispatcher.dispatch(this, context, callback);
    }
}
