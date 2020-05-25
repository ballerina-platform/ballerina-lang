/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.text;

import io.ballerinalang.compiler.internal.parser.CharReader;

/**
 * This is an abstract representation of a Ballerina source file (.bal).
 *
 * @since 2.0.0
 */
public abstract class TextDocument {
    private LineMap lineMap;

    public abstract TextDocument apply(TextDocumentChange textDocumentChange);

    protected abstract LineMap populateTextLineMap();

    public abstract CharReader getCharacterReader();

    public TextLine line(int line) {
        return lines().textLine(line);
    }

    public LinePosition linePositionFrom(int textPosition) {
        return lines().linePositionFrom(textPosition);
    }

    public int textPositionFrom(LinePosition linePosition) {
        return lines().textPositionFrom(linePosition);
    }

    protected LineMap lines() {
        if (lineMap != null) {
            return lineMap;
        }

        lineMap = populateTextLineMap();
        return lineMap;
    }
}
