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
import Statement from './statement';
import FragmentUtils from './../../utils/fragment-utils';


/**
 * Class to represent a function invocation statement in ballerina.
 * @class FunctionInvocationStatement
 * @constructor
 */
class FunctionInvocationStatement extends Statement {
    constructor(args) {
        super('FunctionInvocationStatement');
        this.whiteSpace.defaultDescriptor.regions =  {
            0: '',
            1: '',
            2: '',
            3: ' ',
            4: '\n'
        }
        this.whiteSpace.defaultDescriptor.children =  {
            nameRef: {
                0: '',
                1: '',
                2: '',
                3: ''
            }
        }
    }

    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'function_invocation_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.isEqual(parsedJson.type, 'assignment_statement')
                || _.isEqual(parsedJson.type, 'variable_definition_statement')) {
                // somebody changed the type of statement to an assignment
                // to capture retun value of function Invocation
                let parent = this.getParent();
                let index = parent.getIndexOfChild(this);
                let newNode = this.getFactory().createFromJson(parsedJson);
                parent.removeChild(this, true);
                parent.addChild(newNode, index, true, true);
                newNode.initFromJson(parsedJson);
                nodeToFireEvent = newNode;
            }

            if (_.isFunction(callback)) {
                callback({isValid: true});
            }
            nodeToFireEvent.whiteSpace.useDefault = true;
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Function Invocation Expression Custom Tree modified',
                context: nodeToFireEvent,
            });
        } else {
            if (_.isFunction(callback)) {
                callback({isValid: false, response: parsedJson});
            }
        }
    }


    getStatementString() {
        return !_.isEmpty(this.children) ? this.children[0].getExpressionString() : '';
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default FunctionInvocationStatement;
