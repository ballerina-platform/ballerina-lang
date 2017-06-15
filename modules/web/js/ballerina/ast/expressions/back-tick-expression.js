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
import FragmentUtils from './../../utils/fragment-utils';

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
     * initialize BackTickExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.back_tick_enclosed_string] - back quote enclosed string
     */
    initFromJson(jsonNode) {
        this._backTickEnclosedString = jsonNode.back_tick_enclosed_string;
    }

    getExpressionString() {
        return ('`' + this._backTickEnclosedString + '`');
    }

    setExpressionFromString(expressionString, callback) {
        const fragment = FragmentUtils.createExpressionFragment(expressionString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'back_tick_expression')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Back Tick Expression Custom Tree modified',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }
}

export default BackTickExpression;

