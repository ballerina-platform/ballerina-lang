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
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Class to represent a action invocation to ballerina.
 * @param args
 * @constructor
 */
class ActionInvocationStatement extends Statement {
    constructor(args) {
        super('ActionInvocationStatement');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '\n'
        };
    }

    /**
     * initialize ActionInvocationStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the statement string
     * @return {string} statement string
     * @override
     */
    getStatementString() {
        if (this.getChildren().length > 0) {
            return this.getChildren()[0].getExpressionString();
        }
    }

    /**
     * Set the statement string
     * @param {string} statementString
     * @param {function} callback
     */
    setStatementFromString(statementString, callback) {
        const fragment = FragmentUtils.createStatementFragment(statementString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'assignment_statement')) {

            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Assignment Statement Custom Tree modified',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({isValid: true});
            }
        } else {
            if (_.isFunction(callback)) {
                callback({isValid: false, response: parsedJson});
            }
        }
    }
}

export default ActionInvocationStatement;
