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

class AbstractStreamActionNode extends Node {


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



    setIdentifier(newValue, silent, title) {
        const oldValue = this.identifier;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.identifier = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'identifier',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getIdentifier() {
        return this.identifier;
    }



    setSetClause(newValue, silent, title) {
        const oldValue = this.setClause;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.setClause = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'setClause',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getSetClause() {
        return this.setClause;
    }


    addSetClause(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.setClause.push(node);
            index = this.setClause.length;
        } else {
            this.setClause.splice(i, 0, node);
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

    removeSetClause(node, silent) {
        const index = this.getIndexOfSetClause(node);
        this.removeSetClauseByIndex(index, silent);
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

    removeSetClauseByIndex(index, silent) {
        this.setClause.splice(index, 1);
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

    replaceSetClause(oldChild, newChild, silent) {
        const index = this.getIndexOfSetClause(oldChild);
        this.setClause[index] = newChild;
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

    replaceSetClauseByIndex(index, newChild, silent) {
        this.setClause[index] = newChild;
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

    getIndexOfSetClause(child) {
        return _.findIndex(this.setClause, ['id', child.id]);
    }

    filterSetClause(predicateFunction) {
        return _.filter(this.setClause, predicateFunction);
    }


    setActionType(newValue, silent, title) {
        const oldValue = this.actionType;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.actionType = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'actionType',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getActionType() {
        return this.actionType;
    }



    setOutputEventType(newValue, silent, title) {
        const oldValue = this.outputEventType;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.outputEventType = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'outputEventType',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOutputEventType() {
        return this.outputEventType;
    }



}

export default AbstractStreamActionNode;
