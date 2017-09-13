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

/**
 * Base of all tree nodes.
 *
 * @class Node
 */
class Node {

    /**
     * Will convert any branch of json serialized ballerina AST tree to a node branch
     * of client side model.
     *
     * @static
     * @param {Object} json Serialized json of a ast tree or branch.
     * @param {Node=} parent Parent node.
     * @returns {Node}
     * @memberof Node
     */
    static initFromJson(json, parent) {
        let childName;
        const node = new Node();
        for (childName in json) {
            // if child name is position || whitespace skip convection.
            if (childName !== 'position' && childName !== 'ws') {
                const child = json[childName];
                if (_.isPlainObject(child)) {
                    json[childName] = Node.initFromJson(child, node);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (_.isPlainObject(childItem)) {
                            child[i] = Node.initFromJson(childItem, node);
                        }
                    }
                }
            }
        }
        // TODO: Special case node creation with kind.
        json.parent = parent;
        return Object.assign(node, json);
    }


}

export default Node;
