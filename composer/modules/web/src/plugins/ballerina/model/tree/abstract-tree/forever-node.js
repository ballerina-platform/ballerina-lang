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

class AbstractForeverNode extends Node {


    setGlobalVariables(newValue, silent, title) {
        const oldValue = this.globalVariables;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.globalVariables = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'globalVariables',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getGlobalVariables() {
        return this.globalVariables;
    }


    addGlobalVariables(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.globalVariables.push(node);
            index = this.globalVariables.length;
        } else {
            this.globalVariables.splice(i, 0, node);
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

    removeGlobalVariables(node, silent) {
        const index = this.getIndexOfGlobalVariables(node);
        this.removeGlobalVariablesByIndex(index, silent);
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

    removeGlobalVariablesByIndex(index, silent) {
        this.globalVariables.splice(index, 1);
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

    replaceGlobalVariables(oldChild, newChild, silent) {
        const index = this.getIndexOfGlobalVariables(oldChild);
        this.globalVariables[index] = newChild;
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

    replaceGlobalVariablesByIndex(index, newChild, silent) {
        this.globalVariables[index] = newChild;
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

    getIndexOfGlobalVariables(child) {
        return _.findIndex(this.globalVariables, ['id', child.id]);
    }

    filterGlobalVariables(predicateFunction) {
        return _.filter(this.globalVariables, predicateFunction);
    }


    setStreamingQueryStatements(newValue, silent, title) {
        const oldValue = this.streamingQueryStatements;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.streamingQueryStatements = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'streamingQueryStatements',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStreamingQueryStatements() {
        return this.streamingQueryStatements;
    }


    addStreamingQueryStatements(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.streamingQueryStatements.push(node);
            index = this.streamingQueryStatements.length;
        } else {
            this.streamingQueryStatements.splice(i, 0, node);
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

    removeStreamingQueryStatements(node, silent) {
        const index = this.getIndexOfStreamingQueryStatements(node);
        this.removeStreamingQueryStatementsByIndex(index, silent);
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

    removeStreamingQueryStatementsByIndex(index, silent) {
        this.streamingQueryStatements.splice(index, 1);
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

    replaceStreamingQueryStatements(oldChild, newChild, silent) {
        const index = this.getIndexOfStreamingQueryStatements(oldChild);
        this.streamingQueryStatements[index] = newChild;
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

    replaceStreamingQueryStatementsByIndex(index, newChild, silent) {
        this.streamingQueryStatements[index] = newChild;
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

    getIndexOfStreamingQueryStatements(child) {
        return _.findIndex(this.streamingQueryStatements, ['id', child.id]);
    }

    filterStreamingQueryStatements(predicateFunction) {
        return _.filter(this.streamingQueryStatements, predicateFunction);
    }


    setFunctionVariables(newValue, silent, title) {
        const oldValue = this.functionVariables;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.functionVariables = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'functionVariables',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getFunctionVariables() {
        return this.functionVariables;
    }


    addFunctionVariables(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.functionVariables.push(node);
            index = this.functionVariables.length;
        } else {
            this.functionVariables.splice(i, 0, node);
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

    removeFunctionVariables(node, silent) {
        const index = this.getIndexOfFunctionVariables(node);
        this.removeFunctionVariablesByIndex(index, silent);
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

    removeFunctionVariablesByIndex(index, silent) {
        this.functionVariables.splice(index, 1);
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

    replaceFunctionVariables(oldChild, newChild, silent) {
        const index = this.getIndexOfFunctionVariables(oldChild);
        this.functionVariables[index] = newChild;
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

    replaceFunctionVariablesByIndex(index, newChild, silent) {
        this.functionVariables[index] = newChild;
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

    getIndexOfFunctionVariables(child) {
        return _.findIndex(this.functionVariables, ['id', child.id]);
    }

    filterFunctionVariables(predicateFunction) {
        return _.filter(this.functionVariables, predicateFunction);
    }


    setParameters(newValue, silent, title) {
        const oldValue = this.parameters;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.parameters = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'parameters',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getParameters() {
        return this.parameters;
    }


    addParameters(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.parameters.push(node);
            index = this.parameters.length;
        } else {
            this.parameters.splice(i, 0, node);
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

    removeParameters(node, silent) {
        const index = this.getIndexOfParameters(node);
        this.removeParametersByIndex(index, silent);
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

    removeParametersByIndex(index, silent) {
        this.parameters.splice(index, 1);
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

    replaceParameters(oldChild, newChild, silent) {
        const index = this.getIndexOfParameters(oldChild);
        this.parameters[index] = newChild;
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

    replaceParametersByIndex(index, newChild, silent) {
        this.parameters[index] = newChild;
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

    getIndexOfParameters(child) {
        return _.findIndex(this.parameters, ['id', child.id]);
    }

    filterParameters(predicateFunction) {
        return _.filter(this.parameters, predicateFunction);
    }


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



}

export default AbstractForeverNode;
