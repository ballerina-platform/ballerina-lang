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
package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.AbstractObjectValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic mock object to create a mock of any given typedesc.
 */
public class GenericMockObjectValue extends AbstractObjectValue {

    private ObjectValue mockObj;

    public GenericMockObjectValue(BObjectType type, ObjectValue mockObj) {
        super(type);
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
    public void set(BString fieldName, Object value) {

    }


    @Override
    public FutureValue start(Strand strand, String funcName, Object... args) {
        return null;
    }

    public ObjectValue getMockObj() {
        return this.mockObj;
    }

    private String getCaseIds(ObjectValue mockObj, String fieldName) {
        return mockObj.hashCode() + "-" + fieldName;
    }

    private List<String> getCaseIds(ObjectValue mockObj, String funcName, Object[] args) {
        List<String> caseIdList = new ArrayList<>();
        StringBuilder caseId = new StringBuilder();
        // args contain an extra boolean value arg after every proper argument.
        // These should be removed before constructing case ids
        args = removeUnnecessaryArgs(args);

        // add case for function without args
        caseId.append(mockObj.hashCode()).append("-").append(funcName);
        caseIdList.add(caseId.toString());

        // add case for function with ANY specified for objects
        for (Object arg : args) {
            caseId.append("-");
            if (arg instanceof AbstractObjectValue) {
                caseId.append(MockRegistry.ANY);
            } else {
                caseId.append(arg);
            }
        }
        caseIdList.add(caseId.toString());
        caseId.setLength(0);
        caseId.append(mockObj.hashCode()).append("-").append(funcName);

        // add case for function with ANY specified for objects and records
        for (Object arg : args) {
            caseId.append("-");
            if (arg instanceof AbstractObjectValue || arg instanceof BRecordType) {
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
        Object[] newArgs = new Object[args.length / 2];
        int i = 0;
        int j = 0;
        while (i < args.length) {
            newArgs[j] = args[i];
            i += 2;
            j += 1;
        }
        return newArgs;
    }
}
