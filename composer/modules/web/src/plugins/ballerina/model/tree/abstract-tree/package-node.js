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

class AbstractPackageNode extends Node {


    setImports(newValue, silent, title) {
        const oldValue = this.imports;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.imports = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'imports',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getImports() {
        return this.imports;
    }


    addImports(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.imports.push(node);
            index = this.imports.length;
        } else {
            this.imports.splice(i, 0, node);
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

    removeImports(node, silent) {
        const index = this.getIndexOfImports(node);
        this.removeImportsByIndex(index, silent);
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

    removeImportsByIndex(index, silent) {
        this.imports.splice(index, 1);
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

    replaceImports(oldChild, newChild, silent) {
        const index = this.getIndexOfImports(oldChild);
        this.imports[index] = newChild;
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

    replaceImportsByIndex(index, newChild, silent) {
        this.imports[index] = newChild;
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

    getIndexOfImports(child) {
        return _.findIndex(this.imports, ['id', child.id]);
    }

    filterImports(predicateFunction) {
        return _.filter(this.imports, predicateFunction);
    }


    setCompilationUnits(newValue, silent, title) {
        const oldValue = this.compilationUnits;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.compilationUnits = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'compilationUnits',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getCompilationUnits() {
        return this.compilationUnits;
    }


    addCompilationUnits(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.compilationUnits.push(node);
            index = this.compilationUnits.length;
        } else {
            this.compilationUnits.splice(i, 0, node);
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

    removeCompilationUnits(node, silent) {
        const index = this.getIndexOfCompilationUnits(node);
        this.removeCompilationUnitsByIndex(index, silent);
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

    removeCompilationUnitsByIndex(index, silent) {
        this.compilationUnits.splice(index, 1);
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

    replaceCompilationUnits(oldChild, newChild, silent) {
        const index = this.getIndexOfCompilationUnits(oldChild);
        this.compilationUnits[index] = newChild;
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

    replaceCompilationUnitsByIndex(index, newChild, silent) {
        this.compilationUnits[index] = newChild;
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

    getIndexOfCompilationUnits(child) {
        return _.findIndex(this.compilationUnits, ['id', child.id]);
    }

    filterCompilationUnits(predicateFunction) {
        return _.filter(this.compilationUnits, predicateFunction);
    }


    setPackageDeclaration(newValue, silent, title) {
        const oldValue = this.packageDeclaration;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageDeclaration = newValue;

        this.packageDeclaration.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageDeclaration',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageDeclaration() {
        return this.packageDeclaration;
    }



    setNamespaceDeclarations(newValue, silent, title) {
        const oldValue = this.namespaceDeclarations;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.namespaceDeclarations = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'namespaceDeclarations',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getNamespaceDeclarations() {
        return this.namespaceDeclarations;
    }


    addNamespaceDeclarations(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.namespaceDeclarations.push(node);
            index = this.namespaceDeclarations.length;
        } else {
            this.namespaceDeclarations.splice(i, 0, node);
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

    removeNamespaceDeclarations(node, silent) {
        const index = this.getIndexOfNamespaceDeclarations(node);
        this.removeNamespaceDeclarationsByIndex(index, silent);
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

    removeNamespaceDeclarationsByIndex(index, silent) {
        this.namespaceDeclarations.splice(index, 1);
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

    replaceNamespaceDeclarations(oldChild, newChild, silent) {
        const index = this.getIndexOfNamespaceDeclarations(oldChild);
        this.namespaceDeclarations[index] = newChild;
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

    replaceNamespaceDeclarationsByIndex(index, newChild, silent) {
        this.namespaceDeclarations[index] = newChild;
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

    getIndexOfNamespaceDeclarations(child) {
        return _.findIndex(this.namespaceDeclarations, ['id', child.id]);
    }

    filterNamespaceDeclarations(predicateFunction) {
        return _.filter(this.namespaceDeclarations, predicateFunction);
    }


    setGlobalEndpoints(newValue, silent, title) {
        const oldValue = this.globalEndpoints;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.globalEndpoints = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'globalEndpoints',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getGlobalEndpoints() {
        return this.globalEndpoints;
    }


    addGlobalEndpoints(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.globalEndpoints.push(node);
            index = this.globalEndpoints.length;
        } else {
            this.globalEndpoints.splice(i, 0, node);
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

    removeGlobalEndpoints(node, silent) {
        const index = this.getIndexOfGlobalEndpoints(node);
        this.removeGlobalEndpointsByIndex(index, silent);
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

    removeGlobalEndpointsByIndex(index, silent) {
        this.globalEndpoints.splice(index, 1);
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

    replaceGlobalEndpoints(oldChild, newChild, silent) {
        const index = this.getIndexOfGlobalEndpoints(oldChild);
        this.globalEndpoints[index] = newChild;
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

    replaceGlobalEndpointsByIndex(index, newChild, silent) {
        this.globalEndpoints[index] = newChild;
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

    getIndexOfGlobalEndpoints(child) {
        return _.findIndex(this.globalEndpoints, ['id', child.id]);
    }

    filterGlobalEndpoints(predicateFunction) {
        return _.filter(this.globalEndpoints, predicateFunction);
    }


    setGlobalVariables(newValue, silent, title) {
        const oldValue = this.globalVariables;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.globalVariables = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'globalVariables',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getGlobalVariables() {
        return this.globalVariables;
    }


    addGlobalVariables(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.globalVariables.push(node);
            index = this.globalVariables.length;
        } else {
            this.globalVariables.splice(i, 0, node);
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

    removeGlobalVariables(node, silent) {
        const index = this.getIndexOfGlobalVariables(node);
        this.removeGlobalVariablesByIndex(index, silent);
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

    removeGlobalVariablesByIndex(index, silent) {
        this.globalVariables.splice(index, 1);
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

    replaceGlobalVariables(oldChild, newChild, silent) {
        const index = this.getIndexOfGlobalVariables(oldChild);
        this.globalVariables[index] = newChild;
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

    replaceGlobalVariablesByIndex(index, newChild, silent) {
        this.globalVariables[index] = newChild;
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

    getIndexOfGlobalVariables(child) {
        return _.findIndex(this.globalVariables, ['id', child.id]);
    }

    filterGlobalVariables(predicateFunction) {
        return _.filter(this.globalVariables, predicateFunction);
    }


    setServices(newValue, silent, title) {
        const oldValue = this.services;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.services = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'services',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getServices() {
        return this.services;
    }


    addServices(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.services.push(node);
            index = this.services.length;
        } else {
            this.services.splice(i, 0, node);
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

    removeServices(node, silent) {
        const index = this.getIndexOfServices(node);
        this.removeServicesByIndex(index, silent);
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

    removeServicesByIndex(index, silent) {
        this.services.splice(index, 1);
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

    replaceServices(oldChild, newChild, silent) {
        const index = this.getIndexOfServices(oldChild);
        this.services[index] = newChild;
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

    replaceServicesByIndex(index, newChild, silent) {
        this.services[index] = newChild;
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

    getIndexOfServices(child) {
        return _.findIndex(this.services, ['id', child.id]);
    }

    filterServices(predicateFunction) {
        return _.filter(this.services, predicateFunction);
    }


    setConnectors(newValue, silent, title) {
        const oldValue = this.connectors;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.connectors = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'connectors',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getConnectors() {
        return this.connectors;
    }


    addConnectors(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.connectors.push(node);
            index = this.connectors.length;
        } else {
            this.connectors.splice(i, 0, node);
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

    removeConnectors(node, silent) {
        const index = this.getIndexOfConnectors(node);
        this.removeConnectorsByIndex(index, silent);
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

    removeConnectorsByIndex(index, silent) {
        this.connectors.splice(index, 1);
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

    replaceConnectors(oldChild, newChild, silent) {
        const index = this.getIndexOfConnectors(oldChild);
        this.connectors[index] = newChild;
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

    replaceConnectorsByIndex(index, newChild, silent) {
        this.connectors[index] = newChild;
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

    getIndexOfConnectors(child) {
        return _.findIndex(this.connectors, ['id', child.id]);
    }

    filterConnectors(predicateFunction) {
        return _.filter(this.connectors, predicateFunction);
    }


    setFunctions(newValue, silent, title) {
        const oldValue = this.functions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.functions = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'functions',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getFunctions() {
        return this.functions;
    }


    addFunctions(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.functions.push(node);
            index = this.functions.length;
        } else {
            this.functions.splice(i, 0, node);
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

    removeFunctions(node, silent) {
        const index = this.getIndexOfFunctions(node);
        this.removeFunctionsByIndex(index, silent);
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

    removeFunctionsByIndex(index, silent) {
        this.functions.splice(index, 1);
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

    replaceFunctions(oldChild, newChild, silent) {
        const index = this.getIndexOfFunctions(oldChild);
        this.functions[index] = newChild;
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

    replaceFunctionsByIndex(index, newChild, silent) {
        this.functions[index] = newChild;
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

    getIndexOfFunctions(child) {
        return _.findIndex(this.functions, ['id', child.id]);
    }

    filterFunctions(predicateFunction) {
        return _.filter(this.functions, predicateFunction);
    }


    setStructs(newValue, silent, title) {
        const oldValue = this.structs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.structs = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'structs',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStructs() {
        return this.structs;
    }


    addStructs(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.structs.push(node);
            index = this.structs.length;
        } else {
            this.structs.splice(i, 0, node);
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

    removeStructs(node, silent) {
        const index = this.getIndexOfStructs(node);
        this.removeStructsByIndex(index, silent);
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

    removeStructsByIndex(index, silent) {
        this.structs.splice(index, 1);
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

    replaceStructs(oldChild, newChild, silent) {
        const index = this.getIndexOfStructs(oldChild);
        this.structs[index] = newChild;
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

    replaceStructsByIndex(index, newChild, silent) {
        this.structs[index] = newChild;
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

    getIndexOfStructs(child) {
        return _.findIndex(this.structs, ['id', child.id]);
    }

    filterStructs(predicateFunction) {
        return _.filter(this.structs, predicateFunction);
    }


    setObjects(newValue, silent, title) {
        const oldValue = this.objects;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.objects = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'objects',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getObjects() {
        return this.objects;
    }


    addObjects(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.objects.push(node);
            index = this.objects.length;
        } else {
            this.objects.splice(i, 0, node);
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

    removeObjects(node, silent) {
        const index = this.getIndexOfObjects(node);
        this.removeObjectsByIndex(index, silent);
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

    removeObjectsByIndex(index, silent) {
        this.objects.splice(index, 1);
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

    replaceObjects(oldChild, newChild, silent) {
        const index = this.getIndexOfObjects(oldChild);
        this.objects[index] = newChild;
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

    replaceObjectsByIndex(index, newChild, silent) {
        this.objects[index] = newChild;
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

    getIndexOfObjects(child) {
        return _.findIndex(this.objects, ['id', child.id]);
    }

    filterObjects(predicateFunction) {
        return _.filter(this.objects, predicateFunction);
    }


    setTypeDefinitions(newValue, silent, title) {
        const oldValue = this.typeDefinitions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.typeDefinitions = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'typeDefinitions',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTypeDefinitions() {
        return this.typeDefinitions;
    }


    addTypeDefinitions(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.typeDefinitions.push(node);
            index = this.typeDefinitions.length;
        } else {
            this.typeDefinitions.splice(i, 0, node);
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

    removeTypeDefinitions(node, silent) {
        const index = this.getIndexOfTypeDefinitions(node);
        this.removeTypeDefinitionsByIndex(index, silent);
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

    removeTypeDefinitionsByIndex(index, silent) {
        this.typeDefinitions.splice(index, 1);
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

    replaceTypeDefinitions(oldChild, newChild, silent) {
        const index = this.getIndexOfTypeDefinitions(oldChild);
        this.typeDefinitions[index] = newChild;
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

    replaceTypeDefinitionsByIndex(index, newChild, silent) {
        this.typeDefinitions[index] = newChild;
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

    getIndexOfTypeDefinitions(child) {
        return _.findIndex(this.typeDefinitions, ['id', child.id]);
    }

    filterTypeDefinitions(predicateFunction) {
        return _.filter(this.typeDefinitions, predicateFunction);
    }


    setEnums(newValue, silent, title) {
        const oldValue = this.enums;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.enums = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'enums',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getEnums() {
        return this.enums;
    }


    addEnums(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.enums.push(node);
            index = this.enums.length;
        } else {
            this.enums.splice(i, 0, node);
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

    removeEnums(node, silent) {
        const index = this.getIndexOfEnums(node);
        this.removeEnumsByIndex(index, silent);
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

    removeEnumsByIndex(index, silent) {
        this.enums.splice(index, 1);
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

    replaceEnums(oldChild, newChild, silent) {
        const index = this.getIndexOfEnums(oldChild);
        this.enums[index] = newChild;
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

    replaceEnumsByIndex(index, newChild, silent) {
        this.enums[index] = newChild;
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

    getIndexOfEnums(child) {
        return _.findIndex(this.enums, ['id', child.id]);
    }

    filterEnums(predicateFunction) {
        return _.filter(this.enums, predicateFunction);
    }


    setTransformers(newValue, silent, title) {
        const oldValue = this.transformers;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.transformers = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'transformers',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTransformers() {
        return this.transformers;
    }


    addTransformers(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.transformers.push(node);
            index = this.transformers.length;
        } else {
            this.transformers.splice(i, 0, node);
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

    removeTransformers(node, silent) {
        const index = this.getIndexOfTransformers(node);
        this.removeTransformersByIndex(index, silent);
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

    removeTransformersByIndex(index, silent) {
        this.transformers.splice(index, 1);
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

    replaceTransformers(oldChild, newChild, silent) {
        const index = this.getIndexOfTransformers(oldChild);
        this.transformers[index] = newChild;
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

    replaceTransformersByIndex(index, newChild, silent) {
        this.transformers[index] = newChild;
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

    getIndexOfTransformers(child) {
        return _.findIndex(this.transformers, ['id', child.id]);
    }

    filterTransformers(predicateFunction) {
        return _.filter(this.transformers, predicateFunction);
    }


    setAnnotations(newValue, silent, title) {
        const oldValue = this.annotations;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.annotations = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'annotations',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getAnnotations() {
        return this.annotations;
    }


    addAnnotations(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.annotations.push(node);
            index = this.annotations.length;
        } else {
            this.annotations.splice(i, 0, node);
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

    removeAnnotations(node, silent) {
        const index = this.getIndexOfAnnotations(node);
        this.removeAnnotationsByIndex(index, silent);
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

    removeAnnotationsByIndex(index, silent) {
        this.annotations.splice(index, 1);
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

    replaceAnnotations(oldChild, newChild, silent) {
        const index = this.getIndexOfAnnotations(oldChild);
        this.annotations[index] = newChild;
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

    replaceAnnotationsByIndex(index, newChild, silent) {
        this.annotations[index] = newChild;
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

    getIndexOfAnnotations(child) {
        return _.findIndex(this.annotations, ['id', child.id]);
    }

    filterAnnotations(predicateFunction) {
        return _.filter(this.annotations, predicateFunction);
    }


}

export default AbstractPackageNode;
