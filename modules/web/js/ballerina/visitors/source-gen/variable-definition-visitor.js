/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

class VariableDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitVariableDefinition(variableDefinition) {
        return true;
    }

    beginVisitVariableDefinition(variableDefinition) {
        if (variableDefinition.getType()) {
            this.appendSource(`${variableDefinition.getTypeName()} `);
        }
    }

    visitVariableDefinition(variableDefinition) {
    }

    endVisitVariableDefinition(variableDefinition) {
        this.getParent().appendSource(this.getIndentation() +
                            this.getGeneratedSource());
    }
}

export default VariableDefinitionVisitor;
