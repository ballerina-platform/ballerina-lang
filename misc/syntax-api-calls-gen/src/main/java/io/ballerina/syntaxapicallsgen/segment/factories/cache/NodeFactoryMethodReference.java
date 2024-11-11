/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.segment.factories.cache;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGenException;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;
import io.ballerina.syntaxapicallsgen.segment.factories.SegmentFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Method reference object to cache the parameter types and generic types.
 *
 * @since 2.0.0
 */
public class NodeFactoryMethodReference {
    private static final char DOT_CHAR = '.';

    private final Method method;
    private final Type[] parameterTypes;
    private final Type[] parameterGenericTypes;
    private final int offset;

    NodeFactoryMethodReference(Method method) {
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.parameterGenericTypes = method.getGenericParameterTypes();
        offset = requiresSyntaxKind() ? 1 : 0;
    }

    /**
     * @return the method name.
     */
    public String getName() {
        return method.getName();
    }

    /**
     * Get the type of the parameter with the index.
     *
     * @param parameterIndex Index of the parameter to find the type of.
     *                       (Syntax kind params are ignored when finding the index)
     * @return Type of the parameter.
     */
    public Type getParameterType(int parameterIndex) {
        return parameterTypes[parameterIndex + offset];
    }

    /**
     * Get the generic type of the parameter given.
     * If the parameter does not have a generic type, throws an error.
     *
     * @param parameterIndex Index of the parameter to find the type of.
     *                       (Syntax kind params are ignored when finding the index)
     * @return Simple name of the generic type.
     */
    public String getParameterGeneric(int parameterIndex) {
        String fullParameter = parameterGenericTypes[parameterIndex + offset].getTypeName();
        int lastDot = fullParameter.lastIndexOf(DOT_CHAR);
        if (lastDot == -1) {
            throw new SyntaxApiCallsGenException("" +
                    "Attempted to extract generic type of a parameter without generic type");
        }
        return fullParameter.substring(lastDot + 1, fullParameter.length() - 1);
    }

    /**
     * Creates a segment from this method call.
     *
     * @return Created segment.
     */
    public NodeFactorySegment toSegment() {
        return SegmentFactory.createNodeFactorySegment(getName());
    }

    /**
     * @return whether method requires a syntax kind as a parameter.
     */
    public final boolean requiresSyntaxKind() {
        if (parameterTypes.length == 0) {
            return false;
        }
        return parameterTypes[0] == SyntaxKind.class;
    }
}
