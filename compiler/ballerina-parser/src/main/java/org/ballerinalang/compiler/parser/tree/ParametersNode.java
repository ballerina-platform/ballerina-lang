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
package org.ballerinalang.compiler.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class ParametersNode extends ASTNode {

    public List<ASTNode> parameters = new ArrayList<>();

    public ParametersNode() {
        this.kind = NodeKind.PARAMETERS;
    }

    public void add(ASTNode param) {
        this.parameters.add(param);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ASTNode param : this.parameters) {
            sb.append(param.toString());
        }
        return sb.toString();
    }
}
