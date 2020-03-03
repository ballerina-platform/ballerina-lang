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

/**
 * A representation of a startOffset in the {@code TextDocument} in terms of a zero-based line number
 * and a zero-based character offset on that line.
 */
public class TextPosition {

    /**
     * Line number
     */
    private int line;

    /**
     * A zero-based character offset on the line
     */
    private int offset;

    public TextPosition(int line, int offset) {
        this.line = line;
        this.offset = offset;
    }

    public int line() {
        return line;
    }

    public int offset() {
        return offset;
    }
}
