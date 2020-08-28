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
package io.ballerina.tools.text;

/**
 * A representation of a single line in the {@code TextDocument}.
 *
 * @since 2.0.0
 */
public class TextLine {
    private final int lineNo;
    private final String text;
    /**
     * The span of the line within the {@code TextDocument}.
     */
    private final int startOffset;
    private final int endOffset;
    private final int lengthOfNewLineChars;

    TextLine(int lineNo, String text, int startOffset, int endOffset, int lengthOfNewLineChars) {
        this.text = text;
        this.lineNo = lineNo;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.lengthOfNewLineChars = lengthOfNewLineChars;
    }

    public int lineNo() {
        return lineNo;
    }

    public String text() {
        return text;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public int endOffsetWithNewLines() {
        return endOffset + lengthOfNewLineChars;
    }

    public int length() {
        return endOffset - startOffset;
    }

    public int lengthWithNewLineChars() {
        return endOffset - startOffset + lengthOfNewLineChars;
    }
}
