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
 * Constructor for KeyValueExpression
 * @param {Object} args - Arguments to create the KeyValueExpression
 * @constructor
 */
class KeyValueExpression extends Expression {
    constructor(args) {
        super('KeyValueExpression');
        this._key = _.get(args, 'key', '');
        this._expression = _.get(args, 'valueExpression', '');
    }

 /**
  * setting parameters from json
  * @param jsonNode
  */
    initFromJson(jsonNode) {
        this.setExpression(this.generateKeyValueExpression(jsonNode), {doSilently: true});
    }

 /**
  * function for generate string for key-value-expression
  * @param sonNode
  */
    generateKeyValueExpression(jsonNode) {
        var expString = "";
        var self = this;
        var keyExpressionNode = self.getFactory().createFromJson(jsonNode.children[1][0]);
        var valueExpressionNode = self.getFactory().createFromJson(jsonNode.children[0]);
        valueExpressionNode.initFromJson(jsonNode.children[0]);
        keyExpressionNode.initFromJson(jsonNode.children[1][0]);
        expString += keyExpressionNode.getExpression() + ":" + valueExpressionNode.getExpression();
        return expString;
    }
}

export default KeyValueExpression;

