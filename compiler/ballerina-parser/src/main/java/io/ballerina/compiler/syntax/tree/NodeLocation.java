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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * The {@code NodeLocation} represent the location of a {@code Node} in source code.
 * <p>
 * It is a combination of source file path, start and end line numbers, and start and end column numbers.
 *
 * @since 2.0.0
 */
public class NodeLocation implements Location {
    private final Node node;

    NodeLocation(Node node) {
        this.node = node;
    }

    public LineRange lineRange() {
        return node.lineRange();
    }

    public TextRange textRange() {
        return node.textRange();
    }
}
