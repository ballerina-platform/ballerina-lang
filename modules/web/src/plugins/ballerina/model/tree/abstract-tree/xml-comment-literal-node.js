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

class AbstractXmlCommentLiteralNode extends ExpressionNode {


    setTextFragments(newValue, silent, title) {
        const oldValue = this.textFragments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.textFragments = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'textFragments',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTextFragments() {
        return this.textFragments;
    }


    addTextFragments(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.textFragments.push(node);
            index = this.textFragments.length;
        } else {
            this.textFragments.splice(i, 0, node);
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

    removeTextFragments(node, silent) {
        const index = this.getIndexOfTextFragments(node);
        this.removeTextFragmentsByIndex(index, silent);
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

    removeTextFragmentsByIndex(index, silent) {
        this.textFragments.splice(index, 1);
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

    replaceTextFragments(oldChild, newChild, silent) {
        const index = this.getIndexOfTextFragments(oldChild);
        this.textFragments[index] = newChild;
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

    replaceTextFragmentsByIndex(index, newChild, silent) {
        this.textFragments[index] = newChild;
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

    getIndexOfTextFragments(child) {
        return _.findIndex(this.textFragments, ['id', child.id]);
    }

    filterTextFragments(predicateFunction) {
        return _.filter(this.textFragments, predicateFunction);
    }


}

export default AbstractXmlCommentLiteralNode;
