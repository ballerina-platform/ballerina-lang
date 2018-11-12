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

class AbstractMatchExpressionNode extends ExpressionNode {


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



    setPatternClauses(newValue, silent, title) {
        const oldValue = this.patternClauses;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.patternClauses = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'patternClauses',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPatternClauses() {
        return this.patternClauses;
    }


    addPatternClauses(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.patternClauses.push(node);
            index = this.patternClauses.length;
        } else {
            this.patternClauses.splice(i, 0, node);
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

    removePatternClauses(node, silent) {
        const index = this.getIndexOfPatternClauses(node);
        this.removePatternClausesByIndex(index, silent);
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

    removePatternClausesByIndex(index, silent) {
        this.patternClauses.splice(index, 1);
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

    replacePatternClauses(oldChild, newChild, silent) {
        const index = this.getIndexOfPatternClauses(oldChild);
        this.patternClauses[index] = newChild;
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

    replacePatternClausesByIndex(index, newChild, silent) {
        this.patternClauses[index] = newChild;
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

    getIndexOfPatternClauses(child) {
        return _.findIndex(this.patternClauses, ['id', child.id]);
    }

    filterPatternClauses(predicateFunction) {
        return _.filter(this.patternClauses, predicateFunction);
    }


}

export default AbstractMatchExpressionNode;
