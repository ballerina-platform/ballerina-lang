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

class InvocationNodeAbstract extends Node {


    setPackageAlias(newValue, title) {
        let oldValue = this.packageAlias;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.packageAlias = newValue;
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

    getPackageAlias() {
        return this.packageAlias;
    }



    setExpression(newValue, title) {
        let oldValue = this.expression;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.expression = newValue;
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

    getExpression() {
        return this.expression;
    }



    setArgumentExpressions(newValue, title) {
        let oldValue = this.argumentExpressions;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.argumentExpressions = newValue;
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

    getArgumentExpressions() {
        return this.argumentExpressions;
    }


    addArgumentExpressions(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.argumentExpressions.push(node);
            index = this.argumentExpressions.length;
        } else {
            this.argumentExpressions.splice(i, 0, node);
        }
        this.trigger('tree-modified', {
            origin: this,
            type: 'child-added',
            title: `Add ${child.kind}`,
            data: {
                child,
                index,
            },
        });
    }


    setName(newValue, title) {
        let oldValue = this.name;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.name = newValue;
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

    getName() {
        return this.name;
    }



    setWS(newValue, title) {
        let oldValue = this.wS;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.wS = newValue;
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

    getWS() {
        return this.wS;
    }



    setKind(newValue, title) {
        let oldValue = this.kind;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.kind = newValue;
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

    getKind() {
        return this.kind;
    }



    setPosition(newValue, title) {
        let oldValue = this.position;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.position = newValue;
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

    getPosition() {
        return this.position;
    }



}

export default InvocationNodeAbstract;
