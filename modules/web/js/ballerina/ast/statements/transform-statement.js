/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Statement from './statement';

/**
 * Class for transform statement in ballerina.
 * @param {Object} args - Argument object for creating a transform statement.
 * @constructor
 */
class TransformStatement extends Statement {
    constructor(args) {
        super('TransformStatement');
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var self = this;
        _.each(jsonNode.children, (childNode) => {
            var child = self.getFactory().createFromJson(childNode);
            this.addChild(child);
            this.initFromJson(childNode);
        });
    }
}

export default TransformStatement;

