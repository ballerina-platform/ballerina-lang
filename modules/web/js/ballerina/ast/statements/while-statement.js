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
import ConditionalStatement from './conditional-statement';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Class for while statement in ballerina.
 * @param {Object} args - Argument object for creating an if statement.
 * @param {string} [args.condition="true"] - The condition for "while".
 * @param {Statement} [args.statements="[]] - Statements of the "while".
 * @constructor
 * @augments ConditionalStatement
 */
class WhileStatement extends ConditionalStatement {
    constructor(args) {
        super();
        this.type = 'WhileStatement';
        if (!_.isNil(_.get(args, "condition"))) {
            this.setCondition(_.get(args, "condition"));
        }
        else {
            // create default condition
            this.setCondition(this.getFactory().createBasicLiteralExpression(
                {
                    basicLiteralType: 'boolean',
                    basicLiteralValue: true
                }
            ))
        }
        this._statements = _.get(args, "statements", []);
    }

    getConditionString() {
        return this.getCondition().getExpressionString();
    }

    setConditionFromString(conditionString) {
        if(!_.isNil(conditionString) || !_.isEmpty(conditionString)){
            let fragment = FragmentUtils.createExpressionFragment(conditionString);
            let parsedJson = FragmentUtils.parseFragment(fragment);
            let condition = this.getFactory().createFromJson(parsedJson);
            condition.initFromJson(parsedJson);
            this.setCondition(condition);
            condition.setParent(this);
        }
    }

    setCondition(condition, options) {
        if(!_.isNil(condition)){
            this.setAttribute('_condition', condition, options);
        }
    }

    getCondition() {
        return this._condition;
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var self = this;
        if (!_.isNil(jsonNode.condition)) {
            let condition = self.getFactory().createFromJson(jsonNode.condition);
            condition.initFromJson(jsonNode.condition);
            self.setCondition(condition, {doSilently: true});
            condition.setParent(this);
        }
        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            //TODO : generalize this logic
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.getFactory().createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.getFactory().createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }
}

export default WhileStatement;
