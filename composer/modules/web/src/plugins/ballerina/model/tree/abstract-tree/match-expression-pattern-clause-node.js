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
import Node from '../node';

class AbstractMatchExpressionPatternClauseNode extends Node {


    setVariableNode(newValue, silent, title) {
        const oldValue = this.variableNode;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.variableNode = newValue;

        this.variableNode.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'variableNode',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getVariableNode() {
        return this.variableNode;
    }



    setStatement(newValue, silent, title) {
        const oldValue = this.statement;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.statement = newValue;

        this.statement.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'statement',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStatement() {
        return this.statement;
    }



}

export default AbstractMatchExpressionPatternClauseNode;
