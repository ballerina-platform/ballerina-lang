/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina package document object.
 */
public class BallerinaPackageDoc {

    private String name;
    private List<BallerinaFunctionDoc> ballerinaFunctionDocs;
    private List<BallerinaConnectorDoc> ballerinaConnectorDocs;

    /**
     * Constructor
     * @param name Package name
     */
    public BallerinaPackageDoc(String name) {
        this.name = name;
        ballerinaFunctionDocs = new ArrayList<BallerinaFunctionDoc>();
        ballerinaConnectorDocs = new ArrayList<BallerinaConnectorDoc>();
    }

    /**
     * Get package name
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Add a ballerina function document to the package
     * @param ballerinaFunctionDoc Ballerina function document
     */
    public void addBallerinaFunctionDoc(BallerinaFunctionDoc ballerinaFunctionDoc) {
        ballerinaFunctionDocs.add(ballerinaFunctionDoc);
    }

    /**
     * Add a ballerina connector document to the package
     * @param ballerinaConnectorDoc Ballerina connector document
     */
    public void addBallerinaConnectorDoc(BallerinaConnectorDoc ballerinaConnectorDoc) {
        ballerinaConnectorDocs.add(ballerinaConnectorDoc);
    }

    /**
     * Add a list of ballerina function documents to the package
     * @param ballerinaFunctionDocs Ballerina function documents
     */
    public void addBallerinaFunctionDocs(List<BallerinaFunctionDoc> ballerinaFunctionDocs) {
        this.ballerinaFunctionDocs.addAll(ballerinaFunctionDocs);
    }

    /**
     * Remove ballerina function document
     * @param ballerinaFunctionDoc Ballerina function document
     */
    public void removeBallerinaFunctionDocs(BallerinaFunctionDoc ballerinaFunctionDoc) {
        ballerinaFunctionDocs.remove(ballerinaFunctionDoc);
    }

    /**
     * Get ballerina function documents.
     * @return
     */
    public List<BallerinaFunctionDoc> getBallerinaFunctionDocs() {
        return ballerinaFunctionDocs;
    }

    /**
     * Get ballerina connector documents.
     * @return
     */
    public List<BallerinaConnectorDoc> getBallerinaConnectorDocs() {
        return ballerinaConnectorDocs;
    }

    /**
     * Set ballerina connector documents.
     * @param ballerinaConnectorDocs Ballerina connector documents
     */
    public void setBallerinaConnectorDocs(List<BallerinaConnectorDoc> ballerinaConnectorDocs) {
        this.ballerinaConnectorDocs = ballerinaConnectorDocs;
    }

    @Override
    public String toString() {
        return "BallerinaPackageDoc [name=" + name + ", ballerinaFunctionDocs=" + ballerinaFunctionDocs
                + ", ballerinaConnectorDocs=" + ballerinaConnectorDocs + "]";
    }
}
