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
     * @returns {Node}
     * @memberof Node
     */
    static initFromJson(json, parent) {
        let childName;
        for (childName in json) {
            // if child name is position || whitespace skip convertion.
            if (childName !== 'position' && childName !== 'ws') {
                if (_.isPlainObject(json)) {
                    if (json[childName] instanceof Array) {
                        json[childName] = json[childName].map((element) => {
                            if (_.isPlainObject(json)) {
                                return Node.initFromJson(element, json[childName]);
                            } else {
                                return json[childName];
                            }
                        }, this);
                    } else {
                        json[childName] = Node.initFromJson(json[childName], json);
                    }
                }
            }
        }
        // TODO: Special case node creation with kind.
        json.parent = parent;
        return Object.assign(new Node(), json);
    }



}

export default Node;
