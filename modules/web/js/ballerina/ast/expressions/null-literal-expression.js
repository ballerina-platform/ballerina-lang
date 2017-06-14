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
import Expression from './expression';

/**
 * Constructor for NullLiteralExpression
 * @param {Object} args - Arguments to create the NullLiteralExpression
 * @constructor
 */
class NullLiteralExpression extends Expression {

    constructor(args) {
        super('NullLiteralExpression');
        this._expression = 'null';
    }

    /**
     * setting parameters from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        // nothing to do
    }

    getExpressionString() {
        // directly return null sine this is a null expression.
        // todo need to add white space.
        return 'null';
    }

}

export default NullLiteralExpression;

