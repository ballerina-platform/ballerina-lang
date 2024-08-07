/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.JVMValueType;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_XML_SEQUENCE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina xml variable type.
 */
public class BXmlSequence extends IndexedCompoundVariable {

    private int childrenCount = -1;
    private static final String GET_CHILDREN_LIST_METHOD = "getChildrenList";
    private static final String GET_XML_CHILD_METHOD = "getXmlChildrenInRange";
    private static final String SIZE_METHOD = "size";

    public BXmlSequence(SuspendedContext context, String name, Value value) {
        super(context, name, BVariableType.XML, value);
    }

    @Override
    public String computeValue() {
        try {
            return String.format("XMLSequence (size = %d)", getChildrenCount());
        } catch (Exception ignored) {
            return UNKNOWN_VALUE;
        }
    }

    @Override
    public Either<Map<String, Value>, List<Value>> computeChildVariables(int start, int count) {
        List<Value> childValues = new ArrayList<>();
        try {
            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_XML_SEQUENCE_CLASS);
            argTypeNames.add(JVMValueType.INT.getString());
            argTypeNames.add(JVMValueType.INT.getString());

            RuntimeStaticMethod getXmlChildrenInRange = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    GET_XML_CHILD_METHOD, argTypeNames);

            List<Value> argValues = new ArrayList<>();
            argValues.add(jvmValue);
            argValues.add(VMUtils.make(context, start).getJdiValue());
            argValues.add(VMUtils.make(context, count).getJdiValue());
            getXmlChildrenInRange.setArgValues(argValues);
            Value childArray = getXmlChildrenInRange.invokeSafely();

            if (childArray instanceof ArrayReference arrayReference) {
                childValues = arrayReference.getValues();
            }
            return Either.forRight(childValues);
        } catch (Exception e) {
            return Either.forRight(childValues);
        }
    }

    @Override
    public int getChildrenCount() {
        if (childrenCount < 0) {
            populateChildrenCount();
        }
        return childrenCount;
    }

    private void populateChildrenCount() {
        try {
            List<Method> methods = ((ObjectReference) jvmValue).referenceType().methodsByName(GET_CHILDREN_LIST_METHOD);
            RuntimeInstanceMethod getChildListMethod = new RuntimeInstanceMethod(context, jvmValue, methods.get(0));
            getChildListMethod.setArgValues(new ArrayList<>());
            Value childList = getChildListMethod.invokeSafely();

            methods = ((ObjectReference) childList).referenceType().methodsByName(SIZE_METHOD);
            RuntimeInstanceMethod getSizeMethod = new RuntimeInstanceMethod(context, childList, methods.get(0));
            getSizeMethod.setArgValues(new ArrayList<>());
            Value getSizeResult = getSizeMethod.invokeSafely();

            if (getSizeResult instanceof IntegerValue integerValue) {
                childrenCount = integerValue.intValue();
            } else if (getSizeResult instanceof LongValue longValue) {
                childrenCount = longValue.intValue();
            } else {
                childrenCount = 0;
            }
        } catch (Exception e) {
            childrenCount = 0;
        }
    }
}
