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
 * Constructor for BackTickExpression
 * @param {Object} args - Arguments to create the BackTickExpression
 * @constructor
 */
class BackTickExpression extends Expression {
    constructor(args) {
        super('BackTickExpression');
        this._backTickEnclosedString = _.get(args, 'backTickEnclosedString', '');
    }

    /**
     * Setter for BackTickEnclosedString
     * @param backTickEnclosedString
     */
    setBackTickEnclosedString(backTickEnclosedString, options) {
        this.setAttribute('_backTickEnclosedString', backTickEnclosedString, options);
    }

    /**
     * Getter for BackTickEnclosedString
     * @returns backTickEnclosedString
     */
    getBackTickEnclosedString() {
        return this._backTickEnclosedString;
    }

    /**
     * initialize BackTickExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.back_tick_enclosed_string] - back quote enclosed string
     */
    initFromJson(jsonNode) {
        this.setBackTickEnclosedString(jsonNode.back_tick_enclosed_string, {doSilently: true});
        this.setExpression(this.generateExpression(), {doSilently: true});
    }

    generateExpression() {
        this._expression = '`' + this.getBackTickEnclosedString() + '`';
    }
}

export default BackTickExpression;

