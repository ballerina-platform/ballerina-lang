/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

class AbstractRecordLiteralExprNode extends ExpressionNode {


    setKeyValuePairs(newValue, silent, title) {
        const oldValue = this.keyValuePairs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.keyValuePairs = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'keyValuePairs',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getKeyValuePairs() {
        return this.keyValuePairs;
    }


    addKeyValuePairs(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.keyValuePairs.push(node);
            index = this.keyValuePairs.length;
        } else {
            this.keyValuePairs.splice(i, 0, node);
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

    removeKeyValuePairs(node, silent) {
        const index = this.getIndexOfKeyValuePairs(node);
        this.removeKeyValuePairsByIndex(index, silent);
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

    removeKeyValuePairsByIndex(index, silent) {
        this.keyValuePairs.splice(index, 1);
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

    replaceKeyValuePairs(oldChild, newChild, silent) {
        const index = this.getIndexOfKeyValuePairs(oldChild);
        this.keyValuePairs[index] = newChild;
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

    replaceKeyValuePairsByIndex(index, newChild, silent) {
        this.keyValuePairs[index] = newChild;
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

    getIndexOfKeyValuePairs(child) {
        return _.findIndex(this.keyValuePairs, ['id', child.id]);
    }

    filterKeyValuePairs(predicateFunction) {
        return _.filter(this.keyValuePairs, predicateFunction);
    }


}

export default AbstractRecordLiteralExprNode;
