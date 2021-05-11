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
 * Segment which encompasses a code segment.
 * These can be any random code segments, number literals, etc...
 * Eg: "5", "null"
 *
 * @since 2.0.0
 */
public class CodeSegment extends Segment {
    protected final String content;

    public CodeSegment(String content) {
        this.content = content;
    }

    @Override
    public StringBuilder stringBuilder() {
        return new StringBuilder(content);
    }
}
