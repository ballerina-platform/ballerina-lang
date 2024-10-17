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

package io.ballerina.syntaxapicallsgen.segment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Node factory API call generation segment.
 * Number of parameters may be zero or more.
 * Method call of format: {@literal "NodeFactory.<Type>createNodeType(param1, param2)"}
 *
 * @since 2.0.0
 */
public class NodeFactorySegment extends Segment implements Iterable<Segment> {
    private static final int CREATE_PREFIX_LENGTH = 6;
    private static final String NODE_FACTORY_PREFIX = "NodeFactory.";
    private static final String GENERIC_OPEN_SIGN = "<";
    private static final String GENERIC_CLOSE_SIGN = ">";
    private static final String PARAM_OPEN_SIGN = "(";
    private static final String PARAM_SEP_SIGN = ",";
    private static final String PARAM_CLOSE_SIGN = ")";

    private final boolean isMinutiae;
    private final String methodName;
    protected final String genericType;
    private final List<Segment> parameters;

    public NodeFactorySegment(String methodName, @Nullable String genericType) {
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.genericType = genericType;
        this.isMinutiae = false;
    }

    public NodeFactorySegment(String methodName) {
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.genericType = null;
        this.isMinutiae = false;
    }

    public NodeFactorySegment(String methodName, boolean isMinutiae) {
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.genericType = null;
        this.isMinutiae = isMinutiae;
    }

    /**
     * Adds a new parameter to the method call.
     *
     * @param parameter Parameter segment.
     */
    public void addParameter(Segment parameter) {
        parameters.add(parameter);
    }

    @Override
    public StringBuilder stringBuilder() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(NODE_FACTORY_PREFIX)
                .append(getMethodName()).append(PARAM_OPEN_SIGN);

        // Create comma separated parameter list.
        for (int i = 0; i < parameters.size(); i++) {
            if (i != 0) {
                stringBuilder.append(PARAM_SEP_SIGN);
            }
            stringBuilder.append(parameters.get(i).stringBuilder());
        }
        stringBuilder.append(PARAM_CLOSE_SIGN);

        return stringBuilder;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Segment> iterator() {
        return Objects.requireNonNull(parameters.iterator());
    }

    /**
     * @return Name of the method of this factory call.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Type of the method call. Found via stripping the create part from method name.
     */
    public String getType() {
        return methodName.substring(CREATE_PREFIX_LENGTH);
    }

    /**
     * @return Whether this node factory creates a minutiae node.
     */
    public boolean isMinutiae() {
        return isMinutiae;
    }

    /**
     * @return Formatted Generic type of the method call.
     * Empty string if doesn't have a generic type.
     * If it has a generic type: {@literal <GENERIC_TYPE>}
     */
    public String getGenericType() {
        if (genericType == null) {
            return "";
        }
        return GENERIC_OPEN_SIGN + genericType + GENERIC_CLOSE_SIGN;
    }

    /**
     * Create a copy of the method call.
     * This will create a method call segment with same name but no args.
     *
     * @return Created segment copy.
     */
    public NodeFactorySegment createCopy() {
        return new NodeFactorySegment(methodName, genericType);
    }
}
