/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.formatting;

import com.google.gson.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Visitor for formatting source.
 */
public class FormattingVisitor {

    private boolean isFunctionMatch(JsonObject node, String methodName) {
        String functionToCall = String.format("format%sNode", node.get("kind").getAsString());
        return functionToCall.equals(methodName);
    }

    /**
     * Begin the visit (top to bottom).
     *
     * @param node ballerina node as a json object
     */
    public void beginVisit(JsonObject node) {

        FormattingNodeTree formattingTreeUtil = new FormattingNodeTree();
        Class cls = formattingTreeUtil.getClass();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (isFunctionMatch(node, method.getName())) {
                Method methodcall1 = null;
                try {
                    methodcall1 = cls.getDeclaredMethod(method.getName(), node.getClass());
                    methodcall1.invoke(cls.newInstance(), node);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                        InstantiationException e) {
                    // TODO: Handle exception properly
                }
            }
        }
    }

    /**
     * End the visit (bottom to top).
     *
     * @param node ballerina node as a json object
     */
    public void endVisit(JsonObject node) {
        // No Implementation
    }
}
