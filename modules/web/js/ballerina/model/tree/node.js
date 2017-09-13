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
import EventChannel from 'event_channel';

const uuid = function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
};

/**
 * Base of all tree nodes.
 *
 * @class Node
 */
class Node extends EventChannel {

    /**
     *
     * @param {NodeVisitor} visitor
     */
    accept(visitor) {
        visitor.beginVisit(this);
        // eslint-disable-next-line guard-for-in
        for (const childName in this) {
            if (childName !== 'parent' && childName !== 'position' && childName !== 'ws') {
                const child = this[childName];
                if (child instanceof Node) {
                    child.accept(visitor);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (childItem instanceof Node) {
                            childItem.accept(visitor);
                        }
                    }
                }
            }
        }
        visitor.endVisit(this);
    }
}

export default Node;
