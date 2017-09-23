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

class InvocationNodeAbstract extends Node {


    setPackageAlias(newValue, silent, title) {
        let oldValue = this.packageAlias;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageAlias = newValue;

        this.packageAlias.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageAlias',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getPackageAlias() {
        return this.packageAlias;
    }



    setExpression(newValue, silent, title) {
        let oldValue = this.expression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.expression = newValue;

        this.expression.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'expression',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getExpression() {
        return this.expression;
    }



    setArgumentExpressions(newValue, silent, title) {
        let oldValue = this.argumentExpressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.argumentExpressions = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'argumentExpressions',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getArgumentExpressions() {
        return this.argumentExpressions;
    }


    addArgumentExpressions(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.argumentExpressions.push(node);
            index = this.argumentExpressions.length;
        } else {
            this.argumentExpressions.splice(i, 0, node);
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

    removeArgumentExpressions(node, silent){
        const index = this.getIndexOfArgumentExpressions(node);
        this.removeArgumentExpressionsByIndex(index);
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

    removeArgumentExpressionsByIndex(index, silent){
        this.argumentExpressions.splice(index, 1);
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

    replaceArgumentExpressions(oldChild, newChild, silent){
        const index = this.getIndexOfArgumentExpressions(oldChild);
        this.argumentExpressions[index] = newChild;
    }

    getIndexOfArgumentExpressions(child){
        return _.findIndex(this.argumentExpressions, ['id', child.id]);
    }

    filterArgumentExpressions(predicateFunction){
        return _.filter(this.argumentExpressions, predicateFunction);
    }


    setName(newValue, silent, title) {
        let oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;

        this.name.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'name',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getName() {
        return this.name;
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

export default InvocationNodeAbstract;
