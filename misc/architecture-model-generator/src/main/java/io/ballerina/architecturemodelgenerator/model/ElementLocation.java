/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.model;

/**
 * Represent the location of a component model element.
 *
 * @since 2201.3.1
 */
public class ElementLocation {

    private final String filePath;
    private final LinePosition startPosition;
    private final LinePosition endPosition;

    private ElementLocation(String filePath, LinePosition startPosition, LinePosition endPosition) {
        this.filePath = filePath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public static ElementLocation from(String filePath, LinePosition startLine, LinePosition endLine) {
        return new ElementLocation(filePath, startLine, endLine);
    }

    public String getFilePath() {
        return filePath;
    }

    public LinePosition getStartPosition() {
        return startPosition;
    }

    public LinePosition getEndPosition() {
        return endPosition;
    }

    /**
     * Represent the line position of a component model element.
     *
     * @since 2201.3.1
     */
    public static class LinePosition {

        private final int line;
        private final int offset;

        private LinePosition(int line, int offset) {
            this.line = line;
            this.offset = offset;
        }

        public static LinePosition from(int line, int offset) {
            return new LinePosition(line, offset);
        }

        public int getLine() {
            return line;
        }

        public int getOffset() {
            return offset;
        }
    }
}


