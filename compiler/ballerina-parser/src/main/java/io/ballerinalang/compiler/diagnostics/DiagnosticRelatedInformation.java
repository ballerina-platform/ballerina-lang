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
package io.ballerinalang.compiler.diagnostics;

import io.ballerinalang.compiler.syntax.tree.NodeLocation;

/**
 * Represents a message and location related to a particular {@code Diagnostic}.
 * <p>
 * A sample usage would be to record all symbol information related to duplicate symbol error.
 *
 * @since 2.0.0
 */
public class DiagnosticRelatedInformation {
    private final NodeLocation location;
    private final String message;

    public DiagnosticRelatedInformation(NodeLocation location, String message) {
        this.location = location;
        this.message = message;
    }

    public NodeLocation location() {
        return this.location;
    }

    public String message() {
        return this.message;
    }
}
