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

class AbstractTupleDestructureNode extends StatementNode {


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



    setVariableRefs(newValue, silent, title) {
        const oldValue = this.variableRefs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.variableRefs = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'variableRefs',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getVariableRefs() {
        return this.variableRefs;
    }


    addVariableRefs(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.variableRefs.push(node);
            index = this.variableRefs.length;
        } else {
            this.variableRefs.splice(i, 0, node);
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

    removeVariableRefs(node, silent) {
        const index = this.getIndexOfVariableRefs(node);
        this.removeVariableRefsByIndex(index, silent);
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

    removeVariableRefsByIndex(index, silent) {
        this.variableRefs.splice(index, 1);
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

    replaceVariableRefs(oldChild, newChild, silent) {
        const index = this.getIndexOfVariableRefs(oldChild);
        this.variableRefs[index] = newChild;
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

    replaceVariableRefsByIndex(index, newChild, silent) {
        this.variableRefs[index] = newChild;
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

    getIndexOfVariableRefs(child) {
        return _.findIndex(this.variableRefs, ['id', child.id]);
    }

    filterVariableRefs(predicateFunction) {
        return _.filter(this.variableRefs, predicateFunction);
    }



    isDeclaredWithVar() {
        return this.declaredWithVar;
    }

    setDeclaredWithVar(newValue, silent, title) {
        const oldValue = this.declaredWithVar;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.declaredWithVar = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'declaredWithVar',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractTupleDestructureNode;
