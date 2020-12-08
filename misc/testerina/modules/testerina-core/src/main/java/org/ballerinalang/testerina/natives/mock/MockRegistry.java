/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.natives.mock;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton registry to hold cases registered for mocking.
 */
public class MockRegistry {

    public static final String ANY = "__ANY__";
    private static MockRegistry instance = new MockRegistry();

    public static MockRegistry getInstance() {
        return instance;
    }
    private Map<String, Object> casesMap = new HashMap();
    private Map<String, Integer> memberFuncHitsMap = new HashMap();

    /**
     * Register a case for object mocking when a sequence of return value is provided.
     *
     * @param mockObject mock object
     * @param functionName function to mock
     * @param argsList arguments list passed to the function
     * @param returnVal value to return when the function is called
     * @param hittingCount count to check the return value from the sequence of return values provided
     */
    public void registerCase(BObject mockObject, String functionName, BArray argsList, Object returnVal,
                             int hittingCount) {
        String caseId = constructCaseId(mockObject, functionName, argsList);
        if (!hasHitCount(caseId)) {
            memberFuncHitsMap.put(caseId, 1);
        }
        caseId += "-" + hittingCount;
        casesMap.put(caseId, returnVal);
    }

    /**
     * Register a case for object mocking when a single return value is provided.
     *
     * @param mockObject mock object
     * @param functionName function to mock
     * @param argsList arguments list passed to the function
     * @param returnVal value to return when the function is called
     */
    public void registerCase(BObject mockObject, String functionName, BArray argsList, Object returnVal) {
        String caseId = constructCaseId(mockObject, functionName, argsList);
        casesMap.put(caseId, returnVal);
    }

    private String constructCaseId(BObject mockObject, String functionName, BArray argsList) {
        StringBuilder caseIdBuilder = new StringBuilder();
        if (mockObject != null) {
            caseIdBuilder.append(mockObject.hashCode());
            if (functionName != null) {
                caseIdBuilder.append("-").append(functionName);
            }
            if (argsList != null && argsList.size() > 0) {
                BIterator argIterator = argsList.getIterator();
                while (argIterator.hasNext()) {
                    caseIdBuilder.append("-").append(argIterator.next().toString());
                }
            }
        }
        return caseIdBuilder.toString();
    }

    /**
     * Returns the return value of the provided case.
     *
     * @param caseId case id
     * @return return value
     */
    public Object getReturnValue(String caseId) {
        return casesMap.get(caseId);
    }

    /**
     * Check if case exists in mock registry.
     *
     * @param caseId case id
     * @return whether the case exists
     */
    public boolean hasCase(String caseId) {
        return casesMap.containsKey(caseId);
    }

    /**
     * Returns the map of function hits used when a sequence of return values is provided.
     *
     * @return map containing current hitting count of the function
     */
    public Map<String, Integer> getMemberFuncHitsMap() {
        return memberFuncHitsMap;
    }

    /**
     * Check if a hit count is registered for the case.
     *
     * @param caseId case id
     * @return whether the case exists
     */
    public boolean hasHitCount(String caseId) {
        return memberFuncHitsMap.containsKey(caseId);
    }
}
