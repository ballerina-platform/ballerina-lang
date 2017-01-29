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
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code CallableUnitGroupBuilder} builds Services and Connectors.
 * <p/>
 * A CallableUnitGroup represents a Service or a Connector
 *
 * @since 0.8.0
 */
class CallableUnitGroupBuilder {
    private NodeLocation location;
    private SymbolName name;
    private List<Annotation> annotationList = new ArrayList<>();
    private List<Parameter> parameterList = new ArrayList<>();
    private List<ConnectorDcl> connectorDclList = new ArrayList<>();
    private List<VariableDcl> variableDclList = new ArrayList<>();
    private List<Resource> resourceList = new ArrayList<>();
    private List<BallerinaAction> actionList = new ArrayList<>();

    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }
    void setName(SymbolName name) {
        this.name = name;
    }

    void addAnnotation(Annotation annotation) {
        this.annotationList.add(annotation);
    }

    void addParameter(Parameter param) {
        this.parameterList.add(param);
    }

    void addConnectorDcl(ConnectorDcl connectorDcl) {
        this.connectorDclList.add(connectorDcl);
    }

    void addVariableDcl(VariableDcl variableDcl) {
        this.variableDclList.add(variableDcl);
    }

    void addResource(Resource resource) {
        this.resourceList.add(resource);
    }

    void addAction(BallerinaAction action) {
        this.actionList.add(action);
    }

    Service buildService() {
        return new Service(location, name, annotationList.toArray(new Annotation[annotationList.size()]),
                connectorDclList.toArray(new ConnectorDcl[connectorDclList.size()]),
                variableDclList.toArray(new VariableDcl[variableDclList.size()]),
                resourceList.toArray(new Resource[resourceList.size()]));
    }

    BallerinaConnector buildConnector() {
        return new BallerinaConnector(location, name, annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                connectorDclList.toArray(new ConnectorDcl[connectorDclList.size()]),
                variableDclList.toArray(new VariableDcl[variableDclList.size()]),
                actionList.toArray(new BallerinaAction[actionList.size()]));
    }
}
