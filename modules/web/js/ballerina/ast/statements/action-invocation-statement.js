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
import FragmentUtils from './../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';
/**
 * Class to represent a action invocation to ballerina.
 */
class ActionInvocationStatement extends Statement {
    /**
     * Constructor for Action invocation statement
     */
    constructor() {
        super('ActionInvocationStatement');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '\n',
        };
    }

    /**
     * initialize ActionInvocationStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the statement string
     * @returns {string} statement string
     * @override
     */
    getStatementString() {
        let statementString = '';
        if (this.getChildren().length > 0) {
            statementString = this.getChildren()[0].getExpressionString();
        }
        return statementString;
    }

    /**
     * Set the statement string
     * @param {string} stmtString statement string
     * @param {function} callback callback function
     * @returns {void}
     */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'action_invocation_statement')) {
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
                title: 'Modify Action Invocation',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }
}

export default ActionInvocationStatement;
