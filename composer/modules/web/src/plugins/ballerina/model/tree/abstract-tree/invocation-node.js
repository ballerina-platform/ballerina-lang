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

class AbstractInvocationNode extends Node {


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



    setPackageAlias(newValue, silent, title) {
        const oldValue = this.packageAlias;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageAlias = newValue;

        this.packageAlias.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageAlias',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageAlias() {
        return this.packageAlias;
    }



    setArgumentExpressions(newValue, silent, title) {
        const oldValue = this.argumentExpressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.argumentExpressions = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'argumentExpressions',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getArgumentExpressions() {
        return this.argumentExpressions;
    }


    addArgumentExpressions(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.argumentExpressions.push(node);
            index = this.argumentExpressions.length;
        } else {
            this.argumentExpressions.splice(i, 0, node);
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

    removeArgumentExpressions(node, silent) {
        const index = this.getIndexOfArgumentExpressions(node);
        this.removeArgumentExpressionsByIndex(index, silent);
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

    removeArgumentExpressionsByIndex(index, silent) {
        this.argumentExpressions.splice(index, 1);
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

    replaceArgumentExpressions(oldChild, newChild, silent) {
        const index = this.getIndexOfArgumentExpressions(oldChild);
        this.argumentExpressions[index] = newChild;
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

    replaceArgumentExpressionsByIndex(index, newChild, silent) {
        this.argumentExpressions[index] = newChild;
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

    getIndexOfArgumentExpressions(child) {
        return _.findIndex(this.argumentExpressions, ['id', child.id]);
    }

    filterArgumentExpressions(predicateFunction) {
        return _.filter(this.argumentExpressions, predicateFunction);
    }


    setName(newValue, silent, title) {
        const oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;

        this.name.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'name',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getName() {
        return this.name;
    }




    isAsync() {
        return this.async;
    }

    setAsync(newValue, silent, title) {
        const oldValue = this.async;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.async = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'async',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isIterableOperation() {
        return this.iterableOperation;
    }

    setIterableOperation(newValue, silent, title) {
        const oldValue = this.iterableOperation;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.iterableOperation = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'iterableOperation',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isActionInvocation() {
        return this.actionInvocation;
    }

    setActionInvocation(newValue, silent, title) {
        const oldValue = this.actionInvocation;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.actionInvocation = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'actionInvocation',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractInvocationNode;
