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

import java.util.ArrayList;
import java.util.List;

/**
 *  A service is an HTTP web service described by a Swagger.
 *  A service is the discrete unit of functionality that can be remotely accessed.
 *  <p>
 *
 *  A service is defined as follows:
 *
 *  [ServiceAnnotations]
 *  service ServiceName {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      ResourceDefinition;+
 *  }
 */
public class Service {

    private List<Annotation> annotations;
    private List<Connection> connections;
    private List<Variable> variables;
    private List<Resource> resources;

    /**
     * Get all the Annotations associated with a Service
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
     * Add an Annotation to the Service
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
     * Add a Connection to the Service
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
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Assign variables to the Service
     *
     * @param variables list of variables
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable to the Service
     *
     * @param variable variable to be added to the Service
     */
    public void addVariable(Variable variable) {
        if (variables == null) {
            variables = new ArrayList<Variable>();
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
     * Add a Resource to the Service
     *
     * @param resource a Resource
     */
    public void addResource(Resource resource) {
        if (resources == null) {
            resources = new ArrayList<Resource>();
        }
        resources.add(resource);
    }

}
