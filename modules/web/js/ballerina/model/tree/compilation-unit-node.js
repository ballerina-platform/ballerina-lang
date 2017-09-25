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
import log from 'log';
import CompilationUnitNodeAbstract from './abstract-tree/compilation-unit-node';
import TreeUtils from '../tree-util';


class CompilationUnitNode extends CompilationUnitNodeAbstract {

    /**
     * Get package declaration nodes
     * @returns {*}
     */
    getPackageDeclaration() {
        let pkgNode = null;
        this.getTopLevelNodes().forEach((node) => {
            if (TreeUtils.isPackageDeclaration(node)) {
                pkgNode = node;
            }
        });
        return pkgNode;
    }

    /**
     * Get the import Nodes
     * @returns {Array}
     */
    getImports() {
        const imports = [];
        this.getTopLevelNodes().forEach((node) => {
            if (TreeUtils.isImport(node)) {
                imports.push(node);
            }
        });
        return imports;
    }

    /**
     * Get the constant definitions
     * @returns {Array}
     */
    getConstantDefinitions() {
        const constantDefs = [];
        this.getTopLevelNodes().forEach((node) => {
            if (TreeUtils.isVariable(node)) {
                if (node.const) {
                    constantDefs.push(node);
                }
            }
        });
        return constantDefs;
    }

    /**
     * Get the global variable definitions
     * @returns {Array}
     */
    getGlobalVariableDefinitions() {
        const globalVariableDefs = [];
        this.getTopLevelNodes().forEach((node) => {
            if (TreeUtils.isVariable(node)) {
                if (!node.const) {
                    globalVariableDefs.push(node);
                }
            }
        });
        return globalVariableDefs;
    }

    /**
     * Adds new import declaration.
     */
    addImport(importDeclaration) {
        let index = -1;
        if (this.isExistingPackage(importDeclaration.package)) {
            const errorString = 'Package "' + importDeclaration.package + '" is already imported.';
            log.debug(errorString);
            return;
        }
        index = this.getImports().length - 1;

        // If there are no imports index is -1. Then we need to add the first import after the package declaration
        if (index === -1) {
            index = 0;
        }
        index += 1;
        this.setTopLevelNodes(importDeclaration, index);
    }

    /**
     * Deletes an import with given package name.
     */
    deleteImport(node) {
        this.removeTopLevelNodes(node);
    }

    /**
     * Add global definition
     *
     */
    addGlobal(jsonNode, position) {
        let index = -1;
        if (TreeUtils.isVariable(jsonNode)) {
            // only constants and global variables can be added at global level
            return;
        }
        if (!_.isNil(position) && position >= 0) {
            index = position;
        } else {
            // Get the index of the last global definition
            index = (this.getGlobalVariableDefinitions().concat(this.getConstantDefinitions())).length - 1;

            // If index is still -1, then get the index of the last import.
            if (index === -1) {
                index = this.getImports().length - 1;
            }

            // If index is still -1, then consider the package definition.
            if (_.isEqual(index, -1) && !_.isNil(this.getPackageDeclaration())) {
                index = 0;
            }

            index += 1;
        }
        this.setTopLevelNodes(jsonNode, index);
    }

    /**
     * Delete global definitions
     */
    deleteGlobal(node) {
        this.removeTopLevelNodes(node);
    }

    /**
     * Check whether package name is existing one or not.
     *
     * if exist returns true if doesn't return false
     * */
    isExistingPackage(packageName) {
        return !!_.find(this.getImports(), (child) => {
            return _.isEqual(child.package, packageName);
        });
    }

    /**
     * Get package name
     */
    getPackageName(node) {
        let pkgName = '';
        if (node.getPackageName()) {
            node.getPackageName().forEach((identifier) => {
                pkgName = pkgName.concat(identifier.value);
                const index = node.getPackageName().indexOf(identifier);
                if (index !== node.getPackageName().length - 1) {
                    pkgName = pkgName.concat('.');
                }
            });
        }
        return pkgName;
    }

}

export default CompilationUnitNode;
