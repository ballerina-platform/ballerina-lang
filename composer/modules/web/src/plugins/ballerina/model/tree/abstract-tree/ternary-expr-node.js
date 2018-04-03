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
import ExpressionNode from '../expression-node';

class AbstractTernaryExprNode extends ExpressionNode {


    setThenExpression(newValue, silent, title) {
        const oldValue = this.thenExpression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.thenExpression = newValue;

        this.thenExpression.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'thenExpression',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getThenExpression() {
        return this.thenExpression;
    }



    setElseExpression(newValue, silent, title) {
        const oldValue = this.elseExpression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.elseExpression = newValue;

        this.elseExpression.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'elseExpression',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getElseExpression() {
        return this.elseExpression;
    }



    setCondition(newValue, silent, title) {
        const oldValue = this.condition;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.condition = newValue;

        this.condition.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'condition',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getCondition() {
        return this.condition;
    }



}

export default AbstractTernaryExprNode;
