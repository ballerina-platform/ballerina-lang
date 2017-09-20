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


    setInitFunction(newValue, silent, title) {
        let oldValue = this.initFunction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.initFunction = newValue;
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



    setFilteredParameter(newValue, silent, title) {
        let oldValue = this.filteredParameter;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.filteredParameter = newValue;
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


    setName(newValue, silent, title) {
        let oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;
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
