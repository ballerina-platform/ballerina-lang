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

class AbstractPatternStreamingInputNode extends Node {


    setPatternStreamingInput(newValue, silent, title) {
        const oldValue = this.patternStreamingInput;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.patternStreamingInput = newValue;

        this.patternStreamingInput.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'patternStreamingInput',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPatternStreamingInput() {
        return this.patternStreamingInput;
    }



    setPatternStreamingEdgeInputs(newValue, silent, title) {
        const oldValue = this.patternStreamingEdgeInputs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.patternStreamingEdgeInputs = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'patternStreamingEdgeInputs',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPatternStreamingEdgeInputs() {
        return this.patternStreamingEdgeInputs;
    }


    addPatternStreamingEdgeInputs(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.patternStreamingEdgeInputs.push(node);
            index = this.patternStreamingEdgeInputs.length;
        } else {
            this.patternStreamingEdgeInputs.splice(i, 0, node);
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

    removePatternStreamingEdgeInputs(node, silent) {
        const index = this.getIndexOfPatternStreamingEdgeInputs(node);
        this.removePatternStreamingEdgeInputsByIndex(index, silent);
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

    removePatternStreamingEdgeInputsByIndex(index, silent) {
        this.patternStreamingEdgeInputs.splice(index, 1);
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

    replacePatternStreamingEdgeInputs(oldChild, newChild, silent) {
        const index = this.getIndexOfPatternStreamingEdgeInputs(oldChild);
        this.patternStreamingEdgeInputs[index] = newChild;
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

    replacePatternStreamingEdgeInputsByIndex(index, newChild, silent) {
        this.patternStreamingEdgeInputs[index] = newChild;
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

    getIndexOfPatternStreamingEdgeInputs(child) {
        return _.findIndex(this.patternStreamingEdgeInputs, ['id', child.id]);
    }

    filterPatternStreamingEdgeInputs(predicateFunction) {
        return _.filter(this.patternStreamingEdgeInputs, predicateFunction);
    }


    setTimeExpr(newValue, silent, title) {
        const oldValue = this.timeExpr;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.timeExpr = newValue;

        this.timeExpr.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'timeExpr',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTimeExpr() {
        return this.timeExpr;
    }




    isFollowedBy() {
        return this.followedBy;
    }

    setFollowedBy(newValue, silent, title) {
        const oldValue = this.followedBy;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.followedBy = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'followedBy',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isAndWithNot() {
        return this.andWithNot;
    }

    setAndWithNot(newValue, silent, title) {
        const oldValue = this.andWithNot;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.andWithNot = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'andWithNot',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isForWithNot() {
        return this.forWithNot;
    }

    setForWithNot(newValue, silent, title) {
        const oldValue = this.forWithNot;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.forWithNot = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'forWithNot',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isAndOnly() {
        return this.andOnly;
    }

    setAndOnly(newValue, silent, title) {
        const oldValue = this.andOnly;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.andOnly = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'andOnly',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isOrOnly() {
        return this.orOnly;
    }

    setOrOnly(newValue, silent, title) {
        const oldValue = this.orOnly;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.orOnly = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'orOnly',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractPatternStreamingInputNode;
