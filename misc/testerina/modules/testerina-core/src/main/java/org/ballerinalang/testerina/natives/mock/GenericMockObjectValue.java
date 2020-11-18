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

import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic mock object to create a mock of any given typedesc.
 */
public class GenericMockObjectValue implements BObject {

    private BObject mockObj;

    private ObjectType type;

    public GenericMockObjectValue(ObjectType type, BObject mockObj) {
        this.type = type;
        this.mockObj = mockObj;
    }

    @Override
    public Object call(Strand strand, String funcName, Object... args) {
        if (!this.mockObj.getType().getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle function calls for user-defined mock objects
            return mockObj.call(strand, funcName, args);
        } else { // handle function calls for default mock objects
            List<String> caseIds = getCaseIds(this.mockObj, funcName, args);
            for (String caseId : caseIds) {
                if (MockRegistry.getInstance().hasCase(caseId)) {
                    String hitId = this.mockObj.hashCode() + "-" + funcName;
                    if (MockRegistry.getInstance().hasHitCount(hitId)) {
                        int currentHit = MockRegistry.getInstance().getMemberFuncHitsMap().get(hitId);
                        MockRegistry.getInstance().getMemberFuncHitsMap().put(hitId, currentHit + 1);
                    }
                    return MockRegistry.getInstance().getReturnValue(caseId);
                }
            }
        }
        throw new BallerinaException("no cases registered for member function '" + funcName +
                "' of object type '" + mockObj.getType().getName() + "'.");
    }

    @Override
    public Object get(BString fieldName) {
        if (!this.mockObj.getType().getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle fields values of user-defined mock objects
            return mockObj.get(fieldName);
        } else { // handle fields values of default mock objects
            String caseId = getCaseIds(this.mockObj, fieldName.toString());
            if (MockRegistry.getInstance().hasCase(caseId)) {
                return MockRegistry.getInstance().getReturnValue(caseId);
            }
        }
        throw new BallerinaException("no cases registered for member field '" + fieldName +
                "' of object type '" + mockObj.getType().getName() + "'.");
    }

    @Override
    public long getIntValue(BString fieldName) {
        return 0;
    }

    @Override
    public double getFloatValue(BString fieldName) {
        return 0;
    }

    @Override
    public BString getStringValue(BString fieldName) {
        return null;
    }

    @Override
    public boolean getBooleanValue(BString fieldName) {
        return false;
    }

    @Override
    public BMap getMapValue(BString fieldName) {
        return null;
    }

    @Override
    public BObject getObjectValue(BString fieldName) {
        return null;
    }

    @Override
    public BArray getArrayValue(BString fieldName) {
        return null;
    }

    @Override
    public void addNativeData(String key, Object data) {

    }

    @Override
    public Object getNativeData(String key) {
        return null;
    }

    @Override
    public HashMap<String, Object> getNativeData() {
        return null;
    }

    @Override
    public void set(BString fieldName, Object value) {

    }


    @Override
    public BFuture start(Strand strand, String funcName, Object... args) {
        return null;
    }


    @Override
    public String stringValue(BLink parent) {
        return null;
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return null;
    }

    @Override
    public ObjectType getType() {
        return type;
    }

    public BObject getMockObj() {
        return this.mockObj;
    }

    private String getCaseIds(BObject mockObj, String fieldName) {
        return mockObj.hashCode() + "-" + fieldName;
    }

    private List<String> getCaseIds(BObject mockObj, String funcName, Object[] args) {
        List<String> caseIdList = new ArrayList<>();
        StringBuilder caseId = new StringBuilder();
        // args contain an extra boolean value arg after every proper argument.
        // These should be removed before constructing case ids
        args = removeUnnecessaryArgs(args);

        // 1) add case for function without args
        caseId.append(mockObj.hashCode()).append("-").append(funcName);
        caseIdList.add(caseId.toString());

        // 2) add case for function with ANY specified for objects and records
        for (Object arg : args) {
            caseId.append("-");
            if (arg instanceof BObject || arg instanceof RecordType) {
                caseId.append(MockRegistry.ANY);
            } else {
                caseId.append(arg);
            }
        }
        caseIdList.add(caseId.toString());
        caseId.setLength(0);
        caseId.append(mockObj.hashCode()).append("-").append(funcName);

        // 3) add case for function with ANY specified for objects
        for (Object arg : args) {
            caseId.append("-");
            if (arg instanceof BObject) {
                caseId.append(MockRegistry.ANY);
            } else {
                caseId.append(arg);
            }
        }
        // skip if entry exists in list
        if (!caseIdList.contains(caseId.toString())) {
            caseIdList.add(caseId.toString());
        }
        caseId.setLength(0);

        // 4) add case for return sequence if available
        caseId.append(mockObj.hashCode()).append("-").append(funcName);
        if (MockRegistry.getInstance().hasHitCount(caseId.toString())) {
            int hittingCount = MockRegistry.getInstance().getMemberFuncHitsMap().get(caseId.toString());
            caseId.append("-").append(hittingCount);
            caseIdList.add(caseId.toString());
        }

        // reversing the list to prioritize cases that have arguments specified
        Collections.reverse(caseIdList);
        return caseIdList;
    }

    private Object[] removeUnnecessaryArgs(Object[] args) {
        List<Object> newArgs = new ArrayList<>();
        int i = 0;
        while (i < args.length) {
            if (args[i] != null) {
                newArgs.add(args[i]);
            }
            i += 2;
        }
        return newArgs.toArray();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }
}
