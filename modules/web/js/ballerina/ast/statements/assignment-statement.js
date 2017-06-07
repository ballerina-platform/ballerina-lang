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
import FragmentUtils from '../../utils/fragment-utils';
import log from 'log';

/**
 * Class to represent an Assignment statement.
 * @constructor
 */
class AssignmentStatement extends Statement {
    constructor(args) {
        super('AssignmentStatement');
        this._fullPackageName = _.get(args, 'fullPackageName', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: '\n'
        };
    }

    /**
     * initialize AssignmentStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.children = [];
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the assignment statement string
     * @return {string} assignment statement string
     */
    getStatementString() {
        return (!_.isNil(this.getChildren()[0].getExpressionString())
                ? this.getChildren()[0].getExpressionString() : "leftExpression") + "=" +
            (!_.isNil(this.getChildren()[1].getExpressionString())
                ? this.getChildren()[1].getExpressionString() : "rightExpression");
    }

    /**
     * Set the statement from the statement string
     * @param {string} statementString
     */
    setStatementFromString(statementString, callback) {
        if (!_.isNil(statementString)) {
            let fragment = FragmentUtils.createStatementFragment(statementString + ';');
            let parsedJson = FragmentUtils.parseFragment(fragment);

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

    /**
     * Set the full package name.
     * @param {String} fullPkgName full package name
     * @param {Object} options
     * */
    setFullPackageName(fullPkgName, options) {
        this.setAttribute('_fullPackageName', fullPkgName, options);
    }

    /**
     * Get full package name.
     * @return {String} full package name
     * */
    getFullPackageName() {
        return this._fullPackageName;
    }
}

export default AssignmentStatement;
