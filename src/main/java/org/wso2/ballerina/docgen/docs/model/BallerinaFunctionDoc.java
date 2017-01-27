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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Ballerina function document object.
 */
public class BallerinaFunctionDoc {

    /**
     * <functionName>(paramType0 paramValue0, paramType1 paramValue1 ...) eg: myFunction(int param0, int param1)
     */
    private String signature;

    /**
     * comment specified via description annotation.
     */
    private String description;

    /**
     * parameters.
     */
    private Map<String, BallerinaParameterDoc> parameters;

    /**
     * return parameters.
     */
    private Map<String, BallerinaParameterDoc> returnParameters;

    /**
     * comments specified via exceptions annotations.
     */
    private List<String> thrownExceptions;

    public BallerinaFunctionDoc() {
        parameters = new HashMap<String, BallerinaParameterDoc>();
        returnParameters = new HashMap<String, BallerinaParameterDoc>();
        thrownExceptions = new ArrayList<String>();
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

    public Collection<BallerinaParameterDoc> getParameters() {
        return parameters.values();
    }

    public void addParameter(BallerinaParameterDoc parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public Collection<BallerinaParameterDoc> getReturnParameters() {
        return returnParameters.values();
    }

    public BallerinaParameterDoc getReturnParameter(String name) {
        return returnParameters.get(name);
    }

    public void addReturnParameter(BallerinaParameterDoc parameter) {
        returnParameters.put(parameter.getName(), parameter);
    }

    public BallerinaParameterDoc getParameter(String name) {
        return parameters.get(name);
    }

    public List<String> getThrownExceptions() {
        return thrownExceptions;
    }

    public void setThrownExceptions(List<String> thrownExceptions) {
        this.thrownExceptions = thrownExceptions;
    }

    @Override
    public String toString() {
        return "BallerinaFunctionDoc [signature=" + signature + ", description="
                + description + ", parameters=" + parameters + ", returnParameters=" + returnParameters + ", " +
                "thrownExceptions=" + thrownExceptions + "]";
    }
}
