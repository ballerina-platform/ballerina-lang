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
 * The {@code LineRange} represents a pair of {@code LinePosition}.
 *
 * @since 2.0.0
 */
public class LineRange {
    private final String filePath;
    private final LinePosition startLine;
    private final LinePosition endLine;

    private LineRange(String filePath, LinePosition startLine, LinePosition endLine) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public static LineRange from(String filePath, LinePosition startLine, LinePosition endLine) {
        return new LineRange(filePath, startLine, endLine);
    }

    public String filePath() {
        return filePath;
    }

    public LinePosition startLine() {
        return startLine;
    }

    public LinePosition endLine() {
        return endLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LineRange lineRange = (LineRange) o;
        return Objects.equals(startLine, lineRange.startLine) &&
                Objects.equals(endLine, lineRange.endLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startLine, endLine);
    }

    @Override
    public String toString() {
        return "(" + startLine + "," + endLine + ")";
    }

}
