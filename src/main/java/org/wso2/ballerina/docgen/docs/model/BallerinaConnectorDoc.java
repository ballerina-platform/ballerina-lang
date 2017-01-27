/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerina.docgen.docs.model;

import org.wso2.ballerina.core.model.BallerinaConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina connector document object.
 */
public class BallerinaConnectorDoc {

    /**
     * <connectorName>(paramType0 paramValue0, paramType1 paramValue1 ...) eg: myConnector(string param0, string param1)
     */
    private String signature;

    /**
     * comments specified via param annotations.
     */
    private List<String> parameters;

    /**
     * actions resides in connector
     */
    private List<BallerinaActionDoc> actions;

    /**
     * comment specified via description annotation.
     */
    private String description;

    public BallerinaConnectorDoc(BallerinaConnector conn) {
        parameters = new ArrayList<String>();
        actions = new ArrayList<BallerinaActionDoc>();
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BallerinaActionDoc> getActions() {
        return actions;
    }

    public void setActions(List<BallerinaActionDoc> actions) {
        this.actions = actions;
    }

    public void addAction(BallerinaActionDoc doc) {
        this.actions.add(doc);
    }

    @Override
    public String toString() {
        return "BallerinaConnectorDoc [signature=" + signature + ", description="
                + description + ", parameters=" + parameters + ", actions=" + actions + "]";
    }
}
