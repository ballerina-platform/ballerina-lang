/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.docgen.docs.model;

import org.wso2.ballerina.core.model.BallerinaFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina package document object.
 */
public class BallerinaPackageDoc {

    private String name;
    private List<BallerinaFunction> functions;
    private List<BallerinaConnectorDoc> connectorDocs;

    /**
     * Constructor
     * @param name Package name
     */
    public BallerinaPackageDoc(String name) {
        this.name = name;
        functions = new ArrayList<BallerinaFunction>();
        connectorDocs = new ArrayList<BallerinaConnectorDoc>();
    }

    /**
     * Get package name
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Add a ballerina function to the package
     * @param ballerinaFunction Ballerina function
     */
    public void addFunction(BallerinaFunction ballerinaFunction) {
        functions.add(ballerinaFunction);
    }

    /**
     * Add a ballerina connector document to the package
     * @param ballerinaConnectorDoc Ballerina connector document
     */
    public void addConnectorDoc(BallerinaConnectorDoc ballerinaConnectorDoc) {
        connectorDocs.add(ballerinaConnectorDoc);
    }

    /**
     * Add a list of ballerina function documents to the package
     * @param ballerinaFunction Ballerina function documents
     */
    public void addFunctionDocs(List<BallerinaFunction> ballerinaFunction) {
        this.functions.addAll(ballerinaFunction);
    }

    /**
     * Remove ballerina function document
     * @param ballerinaFunction Ballerina function document
     */
    public void removeFunctionDocs(BallerinaFunction ballerinaFunction) {
        functions.remove(ballerinaFunction);
    }

    /**
     * Get ballerina function documents.
     * @return
     */
    public List<BallerinaFunction> getFunctionDocs() {
        return functions;
    }

    /**
     * Get ballerina connector documents.
     * @return
     */
    public List<BallerinaConnectorDoc> getBallerinaConnectorDocs() {
        return connectorDocs;
    }

    /**
     * Set ballerina connector documents.
     * @param connectorDocs Ballerina connector documents
     */
    public void setBallerinaConnectorDocs(List<BallerinaConnectorDoc> connectorDocs) {
        this.connectorDocs = connectorDocs;
    }

    @Override
    public String toString() {
        return "BallerinaPackageDoc [name=" + name + ", ballerinaFunctionDocs=" + functions
                + ", ballerinaConnectorDocs=" + connectorDocs + "]";
    }
}
