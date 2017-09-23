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

class ConnectorNodeAbstract extends Node {


    setFilteredParameter(newValue, silent, title) {
        let oldValue = this.filteredParameter;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.filteredParameter = newValue;

        this.filteredParameter.parent = this;

        if(!silent) {
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
    }

    getFilteredParameter() {
        return this.filteredParameter;
    }



    setVariableDefs(newValue, silent, title) {
        let oldValue = this.variableDefs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.variableDefs = newValue;

        if(!silent) {
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
    }

    getVariableDefs() {
        return this.variableDefs;
    }


    addVariableDefs(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.variableDefs.push(node);
            index = this.variableDefs.length;
        } else {
            this.variableDefs.splice(i, 0, node);
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

    removeVariableDefs(node, silent){
        const index = this.getIndexOfVariableDefs(node);
        this.removeVariableDefsByIndex(index);
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

    removeVariableDefsByIndex(index, silent){
        this.variableDefs.splice(index, 1);
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

    replaceVariableDefs(oldChild, newChild, silent){
        const index = this.getIndexOfVariableDefs(oldChild);
        this.variableDefs[index] = newChild;
    }

    getIndexOfVariableDefs(child){
        return _.findIndex(this.variableDefs, ['id', child.id]);
    }

    filterVariableDefs(predicateFunction){
        return _.filter(this.variableDefs, predicateFunction);
    }


    setInitFunction(newValue, silent, title) {
        let oldValue = this.initFunction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.initFunction = newValue;

        this.initFunction.parent = this;

        if(!silent) {
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
    }

    getInitFunction() {
        return this.initFunction;
    }



    setInitAction(newValue, silent, title) {
        let oldValue = this.initAction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.initAction = newValue;

        this.initAction.parent = this;

        if(!silent) {
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
    }

    getInitAction() {
        return this.initAction;
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



    setActions(newValue, silent, title) {
        let oldValue = this.actions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.actions = newValue;

        if(!silent) {
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
    }

    getActions() {
        return this.actions;
    }


    addActions(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.actions.push(node);
            index = this.actions.length;
        } else {
            this.actions.splice(i, 0, node);
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

    removeActions(node, silent){
        const index = this.getIndexOfActions(node);
        this.removeActionsByIndex(index);
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

    removeActionsByIndex(index, silent){
        this.actions.splice(index, 1);
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

    replaceActions(oldChild, newChild, silent){
        const index = this.getIndexOfActions(oldChild);
        this.actions[index] = newChild;
    }

    getIndexOfActions(child){
        return _.findIndex(this.actions, ['id', child.id]);
    }

    filterActions(predicateFunction){
        return _.filter(this.actions, predicateFunction);
    }


    setParameters(newValue, silent, title) {
        let oldValue = this.parameters;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.parameters = newValue;

        if(!silent) {
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
    }

    getParameters() {
        return this.parameters;
    }


    addParameters(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.parameters.push(node);
            index = this.parameters.length;
        } else {
            this.parameters.splice(i, 0, node);
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

    removeParameters(node, silent){
        const index = this.getIndexOfParameters(node);
        this.removeParametersByIndex(index);
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

    removeParametersByIndex(index, silent){
        this.parameters.splice(index, 1);
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

    replaceParameters(oldChild, newChild, silent){
        const index = this.getIndexOfParameters(oldChild);
        this.parameters[index] = newChild;
    }

    getIndexOfParameters(child){
        return _.findIndex(this.parameters, ['id', child.id]);
    }

    filterParameters(predicateFunction){
        return _.filter(this.parameters, predicateFunction);
    }


    setFlags(newValue, silent, title) {
        let oldValue = this.flags;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.flags = newValue;

        if(!silent) {
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
    }

    getFlags() {
        return this.flags;
    }



    setAnnotationAttachments(newValue, silent, title) {
        let oldValue = this.annotationAttachments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.annotationAttachments = newValue;

        if(!silent) {
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
    }

    getAnnotationAttachments() {
        return this.annotationAttachments;
    }


    addAnnotationAttachments(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.annotationAttachments.push(node);
            index = this.annotationAttachments.length;
        } else {
            this.annotationAttachments.splice(i, 0, node);
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

    removeAnnotationAttachments(node, silent){
        const index = this.getIndexOfAnnotationAttachments(node);
        this.removeAnnotationAttachmentsByIndex(index);
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

    removeAnnotationAttachmentsByIndex(index, silent){
        this.annotationAttachments.splice(index, 1);
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

    replaceAnnotationAttachments(oldChild, newChild, silent){
        const index = this.getIndexOfAnnotationAttachments(oldChild);
        this.annotationAttachments[index] = newChild;
    }

    getIndexOfAnnotationAttachments(child){
        return _.findIndex(this.annotationAttachments, ['id', child.id]);
    }

    filterAnnotationAttachments(predicateFunction){
        return _.filter(this.annotationAttachments, predicateFunction);
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

export default ConnectorNodeAbstract;
