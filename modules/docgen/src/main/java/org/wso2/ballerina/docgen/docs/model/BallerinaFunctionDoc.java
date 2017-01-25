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

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina function document object.
 */
public class BallerinaFunctionDoc {

    /**
     * <functionName>(paramType0 paramValue0, paramType1 paramValue1 ...) eg: myFunction(int param0, int param1)
     */
    private String signature;

    /**
     * return types of the function.
     */
    private String returnTypes;

    /**
     * comment specified via description annotation.
     */
    private String description;

    /**
     * comments specified via param annotations.
     */
    private List<String> parameters;

    /**
     * comments specified via return annotations.
     */
    private List<String> returnParams;

    /**
     * comments specified via exceptions annotations.
     */
    private List<String> thrownExceptions;

    public BallerinaFunctionDoc(BallerinaFunction func) {
        parameters = new ArrayList<String>();
        returnParams = new ArrayList<String>();
        thrownExceptions = new ArrayList<String>();
        buildSignature(func);
        buildReturnTypes(func);
        extractAnnotations(func);
    }

    private void extractAnnotations(BallerinaFunction func) {
        for (Annotation annotation : func.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("param")) {
                parameters.add(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("description")) {
                description = annotation.getValue();
            } else if (annotation.getName().equalsIgnoreCase("return")) {
                returnParams.add(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("throws")) {
                thrownExceptions.add(annotation.getValue());
            }
        }
    }

    private void buildReturnTypes(BallerinaFunction func) {
        StringBuilder s = new StringBuilder();
        for (Parameter p : func.getReturnParameters()) {
            s.append(BallerinaDocUtils.getType(p.getType()) + ",");
        }
        this.returnTypes = s.length() == 0 ? "" : s.substring(0, s.length() - 1);
    }

    private void buildSignature(BallerinaFunction func) {

        StringBuilder s = new StringBuilder(func.getFunctionName() + " (");
        for (Parameter p : func.getParameters()) {
            s.append(BallerinaDocUtils.getType(p.getType()) + " " + p.getName() + ",");
        }
        this.signature = s.substring(0, s.length() - 1).concat(")");
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReturnType() {
        return returnTypes;
    }

    public void setReturnType(String returnType) {
        this.returnTypes = returnType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public List<String> getReturnParams() {
        return returnParams;
    }

    public void setReturnParams(List<String> returnParams) {
        this.returnParams = returnParams;
    }

    public List<String> getThrownExceptions() {
        return thrownExceptions;
    }

    public void setThrownExceptions(List<String> thrownExceptions) {
        this.thrownExceptions = thrownExceptions;
    }

    @Override
    public String toString() {
        return "BallerinaFunctionDoc [signature=" + signature + ", returnType=" + returnTypes + ", description="
                + description + ", parameters=" + parameters + ", returnParams=" + returnParams + ", thrownExceptions="
                + thrownExceptions + "]";
    }

}
