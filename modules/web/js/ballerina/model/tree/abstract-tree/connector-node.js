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

class ConnectorNodeAbstract extends Node {


    setFilteredParameter(newValue, title) {
        let oldValue = this.filteredParameter;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.filteredParameter = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'filteredParameter',
                newValue,
                oldValue,
            }
        });
    }

    getFilteredParameter() {
        return this.filteredParameter;
    }



    setVariableDefs(newValue, title) {
        let oldValue = this.variableDefs;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.variableDefs = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'variableDefs',
                newValue,
                oldValue,
            }
        });
    }

    getVariableDefs() {
        return this.variableDefs;
    }


    addVariableDefs(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.variableDefs.push(node);
            index = this.variableDefs.length;
        } else {
            this.variableDefs.splice(i, 0, node);
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


    setInitAction(newValue, title) {
        let oldValue = this.initAction;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.initAction = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'initAction',
                newValue,
                oldValue,
            }
        });
    }

    getInitAction() {
        return this.initAction;
    }



    setInitFunction(newValue, title) {
        let oldValue = this.initFunction;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.initFunction = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'initFunction',
                newValue,
                oldValue,
            }
        });
    }

    getInitFunction() {
        return this.initFunction;
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



    setActions(newValue, title) {
        let oldValue = this.actions;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.actions = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'actions',
                newValue,
                oldValue,
            }
        });
    }

    getActions() {
        return this.actions;
    }


    addActions(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.actions.push(node);
            index = this.actions.length;
        } else {
            this.actions.splice(i, 0, node);
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


    setParameters(newValue, title) {
        let oldValue = this.parameters;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.parameters = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'parameters',
                newValue,
                oldValue,
            }
        });
    }

    getParameters() {
        return this.parameters;
    }


    addParameters(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.parameters.push(node);
            index = this.parameters.length;
        } else {
            this.parameters.splice(i, 0, node);
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


    setFlags(newValue, title) {
        let oldValue = this.flags;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.flags = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'flags',
                newValue,
                oldValue,
            }
        });
    }

    getFlags() {
        return this.flags;
    }



    setAnnotationAttachments(newValue, title) {
        let oldValue = this.annotationAttachments;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.annotationAttachments = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'annotationAttachments',
                newValue,
                oldValue,
            }
        });
    }

    getAnnotationAttachments() {
        return this.annotationAttachments;
    }


    addAnnotationAttachments(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.annotationAttachments.push(node);
            index = this.annotationAttachments.length;
        } else {
            this.annotationAttachments.splice(i, 0, node);
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

export default ConnectorNodeAbstract;
