/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util;

/**
 * {@code BLangDiagnostic} represents an error, warning or any other
 * message generated from the Ballerina parser or the semantic analyzer.
 * A diagnostic contains a problem at a specific location in a source file.
 *
 * @since 0.8.0
 */
public class BLangDiagnostic {

    /**
     * Kinds of diagnostics, for example, error or warning or a message.
     */
    enum Category {
        /**
         * Errors prevents normal completion of Ballerina parser or semantic analyzer.
         */
        ERROR,
        /**
         * Warnings does not usually prevent Ballerina from completing normally.
         */
        WARNING,
        /**
         * Informative message from the Ballerina parser or semantic analyzer.
         */
        INFO,
    }

    /**
     * Gets the category of this diagnostic, for example, error or
     * warning.
     *
     * @return the category of this diagnostic
     */
    Category getCategory() {
        return null;
    }

    /**
     * Gets the line number of the source node associated with this diagnostic
     *
     * @return line number
     */
    int getLineNumber() {
        return 0;
    }

    /**
     * Gets the line number of the source node associated with this diagnostic
     *
     * @return column number
     */
    int getColumnNumber() {
        return 0;
    }

    /**
     * Gets a message of this diagnostic.
     *
     * @return a diagnostic message
     */
    String getMessage() {
        return "";
    }
}
