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
import Statement from './statement';

/**
 * Class to represent an Committed statement.
 */
class CommittedStatement extends Statement {
    /**
     * Constructor for committed statement
     */
    constructor() {
        super();
        this.type = 'CommittedStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
            3: ' ',
        };
    }

    /**
     * Initialize the node from the node json.
     * @param {object} jsonNode - Json model for the node.
     * @returns {void}
     * */
    initFromJson(jsonNode) {
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default CommittedStatement;
