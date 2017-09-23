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

import Node from '../node';
import _ from 'lodash';

class TransformNodeAbstract extends Node {


    setBody(newValue, silent, title) {
        let oldValue = this.body;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.body = newValue;

        this.body.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'body',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getBody() {
        return this.body;
    }



    setInputExpressions(newValue, silent, title) {
        let oldValue = this.inputExpressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.inputExpressions = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'inputExpressions',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getInputExpressions() {
        return this.inputExpressions;
    }


    addInputExpressions(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.inputExpressions.push(node);
            index = this.inputExpressions.length;
        } else {
            this.inputExpressions.splice(i, 0, node);
        }
        if(!silent) {
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

    removeInputExpressions(node, silent){
        const index = this.getIndexOfInputExpressions(node);
        this.removeInputExpressionsByIndex(index);
        if(!silent) {
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

    removeInputExpressionsByIndex(index, silent){
        this.inputExpressions.splice(index, 1);
        if(!silent) {
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

    replaceInputExpressions(oldChild, newChild, silent){
        const index = this.getIndexOfInputExpressions(oldChild);
        this.inputExpressions[index] = newChild;
    }

    getIndexOfInputExpressions(child){
        return _.findIndex(this.inputExpressions, ['id', child.id]);
    }

    filterInputExpressions(predicateFunction){
        return _.filter(this.inputExpressions, predicateFunction);
    }


    setOutputExpressions(newValue, silent, title) {
        let oldValue = this.outputExpressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.outputExpressions = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'outputExpressions',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getOutputExpressions() {
        return this.outputExpressions;
    }


    addOutputExpressions(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.outputExpressions.push(node);
            index = this.outputExpressions.length;
        } else {
            this.outputExpressions.splice(i, 0, node);
        }
        if(!silent) {
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

    removeOutputExpressions(node, silent){
        const index = this.getIndexOfOutputExpressions(node);
        this.removeOutputExpressionsByIndex(index);
        if(!silent) {
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

    removeOutputExpressionsByIndex(index, silent){
        this.outputExpressions.splice(index, 1);
        if(!silent) {
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

    replaceOutputExpressions(oldChild, newChild, silent){
        const index = this.getIndexOfOutputExpressions(oldChild);
        this.outputExpressions[index] = newChild;
    }

    getIndexOfOutputExpressions(child){
        return _.findIndex(this.outputExpressions, ['id', child.id]);
    }

    filterOutputExpressions(predicateFunction){
        return _.filter(this.outputExpressions, predicateFunction);
    }


    setWS(newValue, silent, title) {
        let oldValue = this.wS;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.wS = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'wS',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getWS() {
        return this.wS;
    }



    setKind(newValue, silent, title) {
        let oldValue = this.kind;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.kind = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'kind',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getKind() {
        return this.kind;
    }



    setPosition(newValue, silent, title) {
        let oldValue = this.position;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.position = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'position',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getPosition() {
        return this.position;
    }



}

export default TransformNodeAbstract;
