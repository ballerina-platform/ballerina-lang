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

import AbstractTreeUtil from './abstract-tree-util';

class TreeUtil extends AbstractTreeUtil {

    getFullPackageName(node) {
        const root = node.getRoot();
        if (!root) {
            return '';
        }
        const packageAlias = node.getPackageAlias();
        if (!packageAlias) {
            return '';
        }
        const importNode = root.filterTopLevelNodes(this.isImport).find((im) => {
            return im.getAlias().value === packageAlias.value;
        });
        let fullPackageName = '';
        if (importNode) {
            fullPackageName = importNode.getPackageName().map((pkgName) => {
                return pkgName.value;
            }).join('.');
        }
        return fullPackageName;
    }

    /**
     * Generate default name for root level statements.
     * @param {Node} root - root node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateDefaultName(root, node) {
        if (!root) {
            return undefined;
        }

        if (this.isFunction(node) && node.getName().value !== 'main') {
            const functionDefaultName = 'function';
            const functionNodes = root.filterTopLevelNodes(this.isFunction);
            const names = {};
            for (let i = 0; i < functionNodes.length; i++) {
                const name = functionNodes[i].getName().value;
                names[name] = name;
            }

            if (functionNodes.length > 0) {
                for (let i = 1; i <= functionNodes.length + 1; i++) {
                    if (!names[`${functionDefaultName}${i}`]) {
                        node.getName().setValue(`${functionDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                        break;
                    }
                }
            } else {
                node.getName().setValue(`${functionDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        }
        return undefined;
    }


    /**
     * Return true if the statement is a connector declaration.
     *
     * @param {any} node
     * @returns {boolean} true if the statement is a connector declaration.
     * @memberof TreeUtil
     */
    isConnectorDeclaration(node) {
        return false;
    }
}

export default new TreeUtil();
