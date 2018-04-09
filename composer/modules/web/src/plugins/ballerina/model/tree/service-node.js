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
import AbstractServiceNode from './abstract-tree/service-node';
import TreeUtil from './../tree-util';

/**
 * Tree node for a service definition.
 * @class ServiceNode
 * @extends {AbstractServiceNode}
 */
class ServiceNode extends AbstractServiceNode {
    /**
     * Gets the base path value in the @http:configuration annotation.
     * @param {string} [httpPackageAlias='http'] The ballerina.net.http package alias.
     * @returns {string} The base path value.
     * @memberof ResourceNode
     */
    getBasePathAnnotationValue(httpPackageAlias = 'http') {
        let basePathValue;
        this.getAnnotationAttachments().forEach((annotationNode) => {
            if (annotationNode.getPackageAlias().getValue() === httpPackageAlias &&
                annotationNode.getAnnotationName().getValue() === 'configuration') {
                annotationNode.getAttributes().forEach((annotationAttribute) => {
                    if (annotationAttribute.getName().getValue() === 'basePath') {
                        const pathAnnotationAttributeValue = annotationAttribute.getValue();
                        basePathValue = pathAnnotationAttributeValue.getValue().getValue();
                    }
                });
            }
        });
        return basePathValue;
    }

    /**
     * Generate default name for service level statements.
     * @param {Node} parent - parent node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateDefaultName(parent, node) {
        if (!parent) {
            return undefined;
        }

        const resourceDefaultName = 'echo';
        const resourceNodes = parent.getResources();
        const names = {};
        for (let i = 0; i < resourceNodes.length; i++) {
            const name = resourceNodes[i].getName().value;
            names[name] = name;
        }

        if (resourceNodes.length > 0) {
            for (let j = 1; j <= resourceNodes.length; j++) {
                if (!names[`${resourceDefaultName}${j}`]) {
                    node.getName().setValue(`${resourceDefaultName}${j}`, true);
                    node.setName(node.getName(), false);
                    break;
                }
            }
        } else {
            node.getName().setValue(`${resourceDefaultName}1`, true);
            node.setName(node.getName(), false);
        }
        return undefined;
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return TreeUtil.isEndpointTypeVariableDef(node);
    }

    /**
     * Accept a node which is dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @param {Node} dropBefore Drop before given node
     *
     */
    acceptDrop(node, dropBefore) {
        if (TreeUtil.isEndpointTypeVariableDef(node)) {
            // If there are no variables we'll add it to 0
            let index = 0;
            const lastIndexOfConnectors = _.findLastIndex(this.getVariables(),
                variable => TreeUtil.isEndpointTypeVariableDef(variable));
            if (lastIndexOfConnectors !== -1) {
                index = lastIndexOfConnectors + 1;
            }
            TreeUtil.getNewTempVarName(this, 'endpoint')
                .then((varNames) => {
                    node.getVariable().getName().setValue(varNames[0]);
                    this.addVariables(node, index);
                });
        }
    }

    /**
     * Set the full package name of the service
     * @param fullPackageName
     */
    setFullPackageName(fullPackageName) {
        this._fullPackageName = fullPackageName;
    }

    /**
     * Get the full package name of the service
     * @param fullPackageName
     * @returns {*}
     */
    getFullPackageName(fullPackageName) {
        return this._fullPackageName;
    }


    /**
     * Returns the type of the service.
     *
     * @memberof ServiceNode
     */
    getType() {
        let pkg;
        let name;
        if (this.serviceTypeStruct) {
            pkg = this.serviceTypeStruct.packageAlias.value;
            name = this.serviceTypeStruct.typeName.value;
        } else {
            const epName = this.boundEndpoints[0].variableName.value;
            const root = this.getRoot();
            const endpoint = root.find((node) => {
                if (TreeUtil.isEndpoint(node)) {
                    return epName === node.name.value;
                }
                return false;
            });
            pkg = endpoint.endPointType.packageAlias.value;
            name = endpoint.endPointType.typeName.value;
        }
        return pkg;
    }

}

export default ServiceNode;
