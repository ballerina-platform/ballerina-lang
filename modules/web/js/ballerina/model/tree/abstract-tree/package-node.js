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

class PackageNodeAbstract extends Node {


    setImports(newValue, silent, title) {
        let oldValue = this.imports;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.imports = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'imports',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getImports() {
        return this.imports;
    }


    addImports(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.imports.push(node);
            index = this.imports.length;
        } else {
            this.imports.splice(i, 0, node);
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


    setPackageDeclaration(newValue, silent, title) {
        let oldValue = this.packageDeclaration;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageDeclaration = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageDeclaration',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getPackageDeclaration() {
        return this.packageDeclaration;
    }



    setNamespaceDeclarations(newValue, silent, title) {
        let oldValue = this.namespaceDeclarations;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.namespaceDeclarations = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'namespaceDeclarations',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getNamespaceDeclarations() {
        return this.namespaceDeclarations;
    }


    addNamespaceDeclarations(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.namespaceDeclarations.push(node);
            index = this.namespaceDeclarations.length;
        } else {
            this.namespaceDeclarations.splice(i, 0, node);
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


    setGlobalVariables(newValue, silent, title) {
        let oldValue = this.globalVariables;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.globalVariables = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'globalVariables',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getGlobalVariables() {
        return this.globalVariables;
    }


    addGlobalVariables(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.globalVariables.push(node);
            index = this.globalVariables.length;
        } else {
            this.globalVariables.splice(i, 0, node);
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


    setServices(newValue, silent, title) {
        let oldValue = this.services;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.services = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'services',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getServices() {
        return this.services;
    }


    addServices(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.services.push(node);
            index = this.services.length;
        } else {
            this.services.splice(i, 0, node);
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


    setConnectors(newValue, silent, title) {
        let oldValue = this.connectors;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.connectors = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'connectors',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getConnectors() {
        return this.connectors;
    }


    addConnectors(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.connectors.push(node);
            index = this.connectors.length;
        } else {
            this.connectors.splice(i, 0, node);
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


    setFunctions(newValue, silent, title) {
        let oldValue = this.functions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.functions = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'functions',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getFunctions() {
        return this.functions;
    }


    addFunctions(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.functions.push(node);
            index = this.functions.length;
        } else {
            this.functions.splice(i, 0, node);
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


    setStructs(newValue, silent, title) {
        let oldValue = this.structs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.structs = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'structs',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getStructs() {
        return this.structs;
    }


    addStructs(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.structs.push(node);
            index = this.structs.length;
        } else {
            this.structs.splice(i, 0, node);
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


    setCompilationUnits(newValue, silent, title) {
        let oldValue = this.compilationUnits;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.compilationUnits = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'compilationUnits',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getCompilationUnits() {
        return this.compilationUnits;
    }


    addCompilationUnits(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.compilationUnits.push(node);
            index = this.compilationUnits.length;
        } else {
            this.compilationUnits.splice(i, 0, node);
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


    setAnnotations(newValue, silent, title) {
        let oldValue = this.annotations;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.annotations = newValue;
        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'annotations',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getAnnotations() {
        return this.annotations;
    }


    addAnnotations(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.annotations.push(node);
            index = this.annotations.length;
        } else {
            this.annotations.splice(i, 0, node);
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

export default PackageNodeAbstract;
