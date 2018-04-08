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
import StatementNode from '../statement-node';

class AbstractCompoundAssignmentNode extends StatementNode {


    setExpression(newValue, silent, title) {
        const oldValue = this.expression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.expression = newValue;

        this.expression.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'expression',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getExpression() {
        return this.expression;
    }



    setOperatorKind(newValue, silent, title) {
        const oldValue = this.operatorKind;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.operatorKind = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'operatorKind',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOperatorKind() {
        return this.operatorKind;
    }



    setVariable(newValue, silent, title) {
        const oldValue = this.variable;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.variable = newValue;

        this.variable.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'variable',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getVariable() {
        return this.variable;
    }



}

export default AbstractCompoundAssignmentNode;
