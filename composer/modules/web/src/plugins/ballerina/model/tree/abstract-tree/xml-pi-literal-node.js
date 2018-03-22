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

class AbstractXmlPiLiteralNode extends ExpressionNode {


    setDataTextFragments(newValue, silent, title) {
        const oldValue = this.dataTextFragments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.dataTextFragments = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'dataTextFragments',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getDataTextFragments() {
        return this.dataTextFragments;
    }


    addDataTextFragments(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.dataTextFragments.push(node);
            index = this.dataTextFragments.length;
        } else {
            this.dataTextFragments.splice(i, 0, node);
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

    removeDataTextFragments(node, silent) {
        const index = this.getIndexOfDataTextFragments(node);
        this.removeDataTextFragmentsByIndex(index, silent);
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

    removeDataTextFragmentsByIndex(index, silent) {
        this.dataTextFragments.splice(index, 1);
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

    replaceDataTextFragments(oldChild, newChild, silent) {
        const index = this.getIndexOfDataTextFragments(oldChild);
        this.dataTextFragments[index] = newChild;
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

    replaceDataTextFragmentsByIndex(index, newChild, silent) {
        this.dataTextFragments[index] = newChild;
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

    getIndexOfDataTextFragments(child) {
        return _.findIndex(this.dataTextFragments, ['id', child.id]);
    }

    filterDataTextFragments(predicateFunction) {
        return _.filter(this.dataTextFragments, predicateFunction);
    }


    setTarget(newValue, silent, title) {
        const oldValue = this.target;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.target = newValue;

        this.target.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'target',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTarget() {
        return this.target;
    }



}

export default AbstractXmlPiLiteralNode;
