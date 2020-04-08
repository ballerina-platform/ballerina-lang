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
package io.ballerinalang.compiler.syntax.tree;

import java.util.Objects;

public class Span {
    private final int startOffset;
    private final int endOffset;
    private final int width;

    public Span(int position, int width) {
        this.startOffset = position;
        this.width = width;
        this.endOffset = position + width;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public int width() {
        return width;
    }

    public boolean contains(int position) {
        return startOffset <= position && position < endOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Span span = (Span) o;
        return startOffset == span.startOffset &&
                endOffset == span.endOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startOffset, endOffset);
    }

    @Override
    public String toString() {
        return "(" + startOffset + "," + endOffset + ")";
    }
}
