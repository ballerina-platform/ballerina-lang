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

import java.util.Objects;

/**
 * Describes a contiguous sequence of unicode code points in the {@code TextDocument}.
 *
 * @since 1.3.0
 */
public class TextRange {
    private final int startOffset;
    private final int endOffset;
    private final int length;

    private TextRange(int startOffset, int length) {
        this.startOffset = startOffset;
        this.length = length;
        this.endOffset = startOffset + length;
    }

    public static TextRange from(int startOffset, int length) {
        return new TextRange(startOffset, length);
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public int length() {
        return length;
    }

    public boolean contains(int position) {
        return startOffset <= position && position < endOffset;
    }

    /**
     * Tests whether there exists an intersection of this range and the given range.
     * <p>
     * The ranges R1(S1, E1) and R2(S2, E2) intersects if S1 is greater than or equal to E2 and
     * S2 is less than or equal to E1
     *
     * @param textRange The range to test the intersection
     * @return {@code true} if there exists an intersection; {@code false} otherwise
     */
    public boolean intersectionExists(TextRange textRange) {
        return startOffset <= textRange.endOffset && textRange.startOffset <= endOffset;
    }

    public String toString() {
        return "(" + startOffset + "," + endOffset + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextRange textRange = (TextRange) o;
        return startOffset == textRange.startOffset &&
                endOffset == textRange.endOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startOffset, endOffset);
    }
}
