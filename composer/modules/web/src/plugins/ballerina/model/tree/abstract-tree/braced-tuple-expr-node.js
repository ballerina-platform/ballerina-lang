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

class AbstractBracedTupleExprNode extends ExpressionNode {


    setExpressions(newValue, silent, title) {
        const oldValue = this.expressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.expressions = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'expressions',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getExpressions() {
        return this.expressions;
    }


    addExpressions(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.expressions.push(node);
            index = this.expressions.length;
        } else {
            this.expressions.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeExpressions(node, silent) {
        const index = this.getIndexOfExpressions(node);
        this.removeExpressionsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeExpressionsByIndex(index, silent) {
        this.expressions.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceExpressions(oldChild, newChild, silent) {
        const index = this.getIndexOfExpressions(oldChild);
        this.expressions[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceExpressionsByIndex(index, newChild, silent) {
        this.expressions[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfExpressions(child) {
        return _.findIndex(this.expressions, ['id', child.id]);
    }

    filterExpressions(predicateFunction) {
        return _.filter(this.expressions, predicateFunction);
    }


}

export default AbstractBracedTupleExprNode;
