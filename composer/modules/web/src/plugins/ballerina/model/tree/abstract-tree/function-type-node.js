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

class AbstractFunctionTypeNode extends Node {


    setReturnParamTypeNode(newValue, silent, title) {
        const oldValue = this.returnParamTypeNode;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.returnParamTypeNode = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'returnParamTypeNode',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getReturnParamTypeNode() {
        return this.returnParamTypeNode;
    }


    addReturnParamTypeNode(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.returnParamTypeNode.push(node);
            index = this.returnParamTypeNode.length;
        } else {
            this.returnParamTypeNode.splice(i, 0, node);
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

    removeReturnParamTypeNode(node, silent) {
        const index = this.getIndexOfReturnParamTypeNode(node);
        this.removeReturnParamTypeNodeByIndex(index, silent);
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

    removeReturnParamTypeNodeByIndex(index, silent) {
        this.returnParamTypeNode.splice(index, 1);
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

    replaceReturnParamTypeNode(oldChild, newChild, silent) {
        const index = this.getIndexOfReturnParamTypeNode(oldChild);
        this.returnParamTypeNode[index] = newChild;
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

    replaceReturnParamTypeNodeByIndex(index, newChild, silent) {
        this.returnParamTypeNode[index] = newChild;
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

    getIndexOfReturnParamTypeNode(child) {
        return _.findIndex(this.returnParamTypeNode, ['id', child.id]);
    }

    filterReturnParamTypeNode(predicateFunction) {
        return _.filter(this.returnParamTypeNode, predicateFunction);
    }


    setParamTypeNode(newValue, silent, title) {
        const oldValue = this.paramTypeNode;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.paramTypeNode = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'paramTypeNode',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getParamTypeNode() {
        return this.paramTypeNode;
    }


    addParamTypeNode(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.paramTypeNode.push(node);
            index = this.paramTypeNode.length;
        } else {
            this.paramTypeNode.splice(i, 0, node);
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

    removeParamTypeNode(node, silent) {
        const index = this.getIndexOfParamTypeNode(node);
        this.removeParamTypeNodeByIndex(index, silent);
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

    removeParamTypeNodeByIndex(index, silent) {
        this.paramTypeNode.splice(index, 1);
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

    replaceParamTypeNode(oldChild, newChild, silent) {
        const index = this.getIndexOfParamTypeNode(oldChild);
        this.paramTypeNode[index] = newChild;
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

    replaceParamTypeNodeByIndex(index, newChild, silent) {
        this.paramTypeNode[index] = newChild;
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

    getIndexOfParamTypeNode(child) {
        return _.findIndex(this.paramTypeNode, ['id', child.id]);
    }

    filterParamTypeNode(predicateFunction) {
        return _.filter(this.paramTypeNode, predicateFunction);
    }



    isReturnKeywordExists() {
        return this.returnKeywordExists;
    }

    setReturnKeywordExists(newValue, silent, title) {
        const oldValue = this.returnKeywordExists;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.returnKeywordExists = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'returnKeywordExists',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractFunctionTypeNode;
