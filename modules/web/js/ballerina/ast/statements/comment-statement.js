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
import Statement from './statement';

/**
 * Class for comment statement in ballerina.
 * @constructor
 * @augments Statement
 */
class CommentStatement extends Statement {
    constructor(args) {
        super();
        this.type = 'CommentStatement';
        this._commentString = _.get(args, 'commentString', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '\n',
        };
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        this._commentString = jsonNode.comment_string;
    }

    /**
     * returns comment string
     * @returns {string} statement string
     * @override
     */
    getStatementString() {
        return this._commentString;
    }
}

export default CommentStatement;
