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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import VariableDefinitionStatementVisitor from './variable-definition-statement-visitor';

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
        let useDefaultWS = structDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        var constructedSourceSegment = 'struct' + structDefinition.getWSRegion(0)
              + structDefinition.getStructName() + structDefinition.getWSRegion(1)
              + '{' + structDefinition.getWSRegion(2);
        this.appendSource(constructedSourceSegment);
        this.appendSource((useDefaultWS) ? this.getIndentation() : '');
        this.indent();
        _.forEach(structDefinition.getVariableDefinitionStatements(), (variableDefStatement) => {
            let varDefVisitor = new VariableDefinitionStatementVisitor(this);
            variableDefStatement.accept(varDefVisitor);
        });
        log.debug('Begin Visit FunctionDefinition');
    }

    visitStructDefinition(structDefinition) {
        log.debug('Visit FunctionDefinition');
    }

    endVisitStructDefinition(structDefinition) {
        this.outdent();
        this.appendSource('}' + structDefinition.getWSRegion(3));
        this.appendSource((structDefinition.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit FunctionDefinition');
    }
}

export default StructDefinitionVisitor;
