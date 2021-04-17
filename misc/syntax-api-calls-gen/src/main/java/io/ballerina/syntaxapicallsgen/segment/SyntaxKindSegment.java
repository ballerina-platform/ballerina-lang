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

import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * Syntax kind segment which has the format.
 * "SyntaxKind.ABC_KIND"
 *
 * @since 2.0.0
 */
public class SyntaxKindSegment extends Segment {
    private static final String SYNTAX_KIND_PREFIX = "SyntaxKind.";

    private final SyntaxKind syntaxKind;

    public SyntaxKindSegment(SyntaxKind syntaxKind) {
        this.syntaxKind = syntaxKind;
    }

    @Override
    public StringBuilder stringBuilder() {
        return new StringBuilder(SYNTAX_KIND_PREFIX).append(syntaxKind.toString());
    }
}
