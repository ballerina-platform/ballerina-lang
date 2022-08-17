/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.internal;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * Represents the null location. This can used to create diagnostics
 * which cannot be related to a location in the document.
 * @since 2.0.0
 */
public class NullLocation implements Location {
    protected final String filepath;

    public NullLocation() {
        this.filepath = "";
    }

    public NullLocation(String filePath) {
        this.filepath = filePath;
    }

    @Override
    public LineRange lineRange() {
        LinePosition from = LinePosition.from(0, 0);
        return LineRange.from(filepath, from, from);
    }

    @Override
    public TextRange textRange() {
        return TextRange.from(0, 0);
    }
}
