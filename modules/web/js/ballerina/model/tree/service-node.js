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

import AbstractServiceNode from './abstract-tree/service-node';

class ServiceNode extends AbstractServiceNode {
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
            for (let i = 1; i <= resourceNodes.length + 1; i++) {
                if (!names[`${resourceDefaultName}${i}`]) {
                    node.getName().setValue(`${resourceDefaultName}${i}`, true);
                    node.setName(node.getName(), true);
                    break;
                }
            }
        } else {
            node.getName().setValue(`${resourceDefaultName}1`, true);
            node.setName(node.getName(), true);
        }
        return undefined;
    }
}

export default ServiceNode;
