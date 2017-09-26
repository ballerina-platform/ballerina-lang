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
     * Check whether a global with the given identifier exists one or not.
     *
     * if exist returns true if doesn't return false
     * */
    isExistingGlobalIdentifier(identifier) {
        return !!_.find(this.getGlobalVariableDefinitions().concat(this.getConstantDefinitions()), (child) => {
            return _.isEqual(child.getName().value, identifier);
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
