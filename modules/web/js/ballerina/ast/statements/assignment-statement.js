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
import FragmentUtils from './../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';

/**
 * Class to represent an Assignment statement.
 * @constructor
 */
class AssignmentStatement extends Statement {
    /**
     * Constructor for assignment statement
     * @param {object} args constructor arguments
     */
    constructor(args) {
        super('AssignmentStatement');
        this._fullPackageName = _.get(args, 'fullPackageName', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: '\n',
        };
    }

    /**
     * initialize AssignmentStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.children = [];
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the assignment statement string
     * @return {string} assignment statement string
     */
    getStatementString() {
        return ((!_.isNil(this.getChildren()[0])
                ? this.getChildren()[0].getExpressionString() : '')
                // default tailing whitespace of expressions is emtpy - hence we need to
                // append a sapce here
                + ((!_.isNil(this.getChildren()[0]) && !_.isEmpty(this.getChildren()[0].children)
                      && _.last(this.getChildren()[0].children).whiteSpace.useDefault) ? ' ' : '')) + '=' +
            (!_.isNil(this.getChildren()[1])
                // we are getting following whitespace of = from assignment statement
                ? this.getWSRegion(2) + this.getChildren()[1].getExpressionString() : '');
    }

    /**
     * Get the left expression of the assignment statement.
     * @returns {Expression} left expression
     * @memberof AssignmentStatement
     */
    getLeftExpression() {
        return this.getChildren()[0];
    }

    /**
     * Get the right expression of the assignment statement.
     * @returns {Expression} right expression
     * @memberof AssignmentStatement
     */
    getRightExpression() {
        return this.getChildren()[1];
    }

    /**
     * Set the statement from the statement string
     * @param {string} stmtString statement string
     * @param {function} callback function
     * @returns {void}
     */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'assignment_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.has(parsedJson, 'type')) {
                // user may want to change the statement type
                const newNode = this.getFactory().createFromJson(parsedJson);
                if (this.getFactory().isStatement(newNode)) {
                    // somebody changed the type of statement to an assignment
                    // to capture retun value of function Invocation
                    const parent = this.getParent();
                    const index = parent.getIndexOfChild(this);
                    parent.removeChild(this, true);
                    parent.addChild(newNode, index, true, true);
                    newNode.initFromJson(parsedJson);
                    nodeToFireEvent = newNode;
                }
            } else {
                log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            }

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Assignment Statement',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    /**
     * Set the full package name.
     * @param {String} fullPkgName full package name
     * @param {Object} options set attribute options
     * @returns {void}
     */
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
