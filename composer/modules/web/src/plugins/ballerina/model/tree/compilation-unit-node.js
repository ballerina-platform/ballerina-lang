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
import AbstractCompilationUnitNode from './abstract-tree/compilation-unit-node';
import TreeUtils from '../tree-util';


class CompilationUnitNode extends AbstractCompilationUnitNode {

    /**
     * Check whether package name is existing one or not.
     *
     * if exist returns true if doesn't return false
     * */
    isExistingPackage(packageName) {
        return !!_.find(this.filterTopLevelNodes({ kind: 'Import' }), (child) => {
            return _.isEqual(child.parent.getPackageName(child), packageName);
        });
    }

    /**
     * Check whether a global with the given identifier exists one or not.
     *
     * if exist returns true if doesn't return false
     * */
    isExistingGlobalIdentifier(identifier) {
        return !!_.find(this.filterTopLevelNodes({ kind: 'Variable' })
            .concat(this.filterTopLevelNodes({ kind: 'Xmlns' })), (child) => {
            if (child.kind === 'Variable') {
                return _.isEqual(child.getName().value, identifier);
            } else {
                return _.isEqual(child.namespaceDeclaration.getPrefix()
                    ? child.namespaceDeclaration.getPrefix().getValue() : '', identifier);
            }
        });
    }

    /**
     * Get package name
     */
    getPackageName(node) {
        let pkgName = '';
        if (node.packageName) {
            node.packageName.forEach((identifier) => {
                pkgName = pkgName.concat(identifier.value);
                const index = node.packageName.indexOf(identifier);
                if (index !== node.packageName.length - 1) {
                    pkgName = pkgName.concat('.');
                }
            });
        }
        return pkgName;
    }
    /**
     * Add import as a top level node
     * @param importNode
     */
    addImport(importNode, silent) {
        const pkgName = this.getPackageName(importNode);
        if (this.isExistingPackage(pkgName)) {
            const errorString = 'Package "' + pkgName + '" is already imported.';
            log.debug(errorString);
            return;
        }
        const pkgDeclIndex = _.findLastIndex(this.getTopLevelNodes(), node => TreeUtils.isPackageDeclaration(node));
        const lastImportIndex = _.findLastIndex(this.getTopLevelNodes(), node => TreeUtils.isImport(node));
        let targetIndex = 0; // If there is no a pck node or any import, we'll add it to 0
        if (lastImportIndex !== -1) {
            targetIndex = lastImportIndex + 1;
        } else if (pkgDeclIndex !== -1) {
            targetIndex = pkgDeclIndex + 1;
        }
        this.addTopLevelNodes(importNode, targetIndex, silent);
    }

    getImports() {
        return this.getTopLevelNodes().filter((node) => {
            return TreeUtils.isImport(node);
        });
    }

    /**
     * Add a global declaration as a top level node
     * @param globalNode
     */
    addGlobal(globalNode, silent) {
        if (!TreeUtils.isVariable(globalNode) && !(globalNode.kind === 'Xmlns')) {
            // only constants and global variables can be added at global level
            return;
        }
        const lastGlobalIndex = _.findLastIndex(this.getTopLevelNodes(),
                node => TreeUtils.isVariable(node) || node.kind === 'Xmlns');
        const pkgDeclIndex = _.findLastIndex(this.getTopLevelNodes(), node => TreeUtils.isPackageDeclaration(node));
        const lastImportIndex = _.findLastIndex(this.getTopLevelNodes(), node => TreeUtils.isImport(node));
        let targetIndex = 0; // If there is no a pck node or any import/any global, we'll add it to 0
        if (lastGlobalIndex !== -1) {
            targetIndex = lastGlobalIndex + 1;
        } else if (lastImportIndex !== -1) {
            targetIndex = lastImportIndex + 1;
        } else if (pkgDeclIndex !== -1) {
            targetIndex = pkgDeclIndex + 1;
        }
        this.addTopLevelNodes(globalNode, targetIndex, silent);
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return TreeUtils.isFunction(node)
            || TreeUtils.isConnector(node)
            || TreeUtils.isService(node)
            || TreeUtils.isStruct(node)
            || TreeUtils.isAnnotation(node)
            || TreeUtils.isTransformer(node)
            || TreeUtils.isEnum(node);
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     */
    acceptDrop(node) {
        if (node instanceof Array) {
            node.forEach((element, i, array) => {
                const silent = (i !== (array.length - 1));
                TreeUtils.generateDefaultName(this, element);
                this.addTopLevelNodes(element, -1, silent);
            });
        } else {
            TreeUtils.generateDefaultName(this, node);
            this.addTopLevelNodes(node);
        }
    }

    /**
     * Returns true if the compilation unit does not have a toplevel node.
     *
     * @param {Node} node Node instance to be dropped
     */
    isEmpty() {
        let empty = true;
        for (let i = 0; i < this.topLevelNodes.length; i++) {
            if (TreeUtils.isService(this.topLevelNodes[i]) ||
                TreeUtils.isFunction(this.topLevelNodes[i]) ||
                TreeUtils.isConnectorDeclaration(this.topLevelNodes[i]) ||
                TreeUtils.isEnum(this.topLevelNodes[i]) ||
                TreeUtils.isStruct(this.topLevelNodes[i]) ||
                TreeUtils.isTransformer(this.topLevelNodes[i])
                ) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    /**
     * Returns the package declaration node
     */
    getPackageDeclaration() {
        const pkgDecNodes = this.filterTopLevelNodes({ kind: 'PackageDeclaration' });
        return _.isEmpty(pkgDecNodes) ? undefined : pkgDecNodes[0];
    }
}

export default CompilationUnitNode;
