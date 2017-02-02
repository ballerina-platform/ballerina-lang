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

import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina package document object.
 */
public class BallerinaPackageDoc {

    private String name;
    private List<BallerinaFunction> functions;
    private List<BallerinaConnector> connectors;

    /**
     * Constructor
     *
     * @param name Package name
     */
    public BallerinaPackageDoc(String name) {
        this.name = name;
        functions = new ArrayList<BallerinaFunction>();
        connectors = new ArrayList<BallerinaConnector>();
    }

    /**
     * Get package name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Add a ballerina function to the package
     *
     * @param ballerinaFunction Ballerina function
     */
    public void addFunction(BallerinaFunction ballerinaFunction) {
        functions.add(ballerinaFunction);
    }

    /**
     * Add a ballerina connector to the package
     *
     * @param ballerinaConnector Ballerina connector
     */
    public void addConnector(BallerinaConnector ballerinaConnector) {
        connectors.add(ballerinaConnector);
    }

    /**
     * Add a list of ballerina functions to the package
     *
     * @param ballerinaFunction Ballerina functions
     */
    public void addFunctions(List<BallerinaFunction> ballerinaFunction) {
        this.functions.addAll(ballerinaFunction);
    }

    /**
     * Remove ballerina function
     *
     * @param ballerinaFunction Ballerina function
     */
    public void removeFunction(BallerinaFunction ballerinaFunction) {
        functions.remove(ballerinaFunction);
    }

    /**
     * Get ballerina functions.
     *
     * @return
     */
    public List<BallerinaFunction> getFunctions() {
        return functions;
    }

    /**
     * Get ballerina connectors.
     *
     * @return
     */

    public List<BallerinaConnector> getBallerinaConnectors() {
        return connectors;
    }

    @Override
    public String toString() {
        return "BallerinaPackageDoc [name=" + name + ", ballerinaFunctions=" + functions
                + ", ballerinaConnectors=" + connectors + "]";
    }
}
