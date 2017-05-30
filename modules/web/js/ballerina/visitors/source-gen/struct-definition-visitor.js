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
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';

/**
 * @param parent
 * @constructor
 */
class StructDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitStructDefinition(structDefinition) {
        return true;
    }

    beginVisitStructDefinition(structDefinition) {
        var constructedSourceSegment = '\n' + this.getIndentation() +  'struct ' + structDefinition.getStructName() + " {\n";
        this.indent();
        _.forEach(structDefinition.getVariableDefinitionStatements(), (variableDefStatement) => {
            constructedSourceSegment = constructedSourceSegment + this.getIndentation()
                          + variableDefStatement.getStatementString() + ";\n";
        });
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit FunctionDefinition');
    }

    visitStructDefinition(structDefinition) {
        log.debug('Visit FunctionDefinition');
    }

    endVisitStructDefinition(structDefinition) {
        this.outdent();
        this.appendSource(this.getIndentation() + "}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit FunctionDefinition');
    }
}

export default StructDefinitionVisitor;
