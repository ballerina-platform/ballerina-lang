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

/**
 * Basic code block that can be converted to String and
 * can build any SyntaxAPI call.
 * Represents a tree of code, {@link NodeFactorySegment} is
 * the non terminal nodes.
 *
 * @since 2.0.0
 */
public abstract class Segment {
    /**
     * String representation of the segment as a StringBuilder.
     *
     * @return The {@link StringBuilder} representation of the
     * source code of the subtree starting from this segment.
     */
    public abstract StringBuilder stringBuilder();

    @Override
    public String toString() {
        return stringBuilder().toString();
    }
}
