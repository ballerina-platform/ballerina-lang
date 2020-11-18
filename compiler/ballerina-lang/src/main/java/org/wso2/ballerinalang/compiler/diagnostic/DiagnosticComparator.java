/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;

import java.util.Comparator;

/**
 * A comparator for sorting diagnostics. Diagnostics are sorted by:
 * 1) File name
 * 2) Line number
 * 3) Column number
 * 4) Message
 * 5) By the hash code - to allow duplicates
 * This comparator does not use the package ID info for sorting, since the diagnostics
 * are collected per package.
 * 
 * @since 2.0.0
 */
public class DiagnosticComparator implements Comparator<Diagnostic> {

    @Override
    public int compare(Diagnostic d1, Diagnostic d2) {
        Location l1 = d1.location();
        Location l2 = d2.location();
        LineRange lineRange1 = l1.lineRange();
        LineRange lineRange2 = l2.lineRange();

        // Compare file name
        // TODO: handle if one is null and other is not
        String file1 = lineRange1.filePath();
        String file2 = lineRange2.filePath();
        if (file1 != null && file2 != null) {
            int fileComparison = file1.compareTo(file2);
            if (fileComparison != 0) {
                return fileComparison;
            }
        }

        // Compare line number
        int lineComparison = lineRange1.startLine().line() - lineRange2.startLine().line();
        if (lineComparison != 0) {
            return lineComparison;
        }

        // Compare column number
        int columnComparison = lineRange1.startLine().offset() - lineRange2.startLine().offset();
        if (columnComparison != 0) {
            return columnComparison;
        }

        // Compare message
        int msgComparison = d1.message().compareTo(d2.message());
        if (msgComparison != 0) {
            return msgComparison;
        }

        // If everything is identical, check the instance equality (hash code), so that we can
        // allow duplicates in the diagnostics.
        return d1.hashCode() - d2.hashCode();
    }
}
