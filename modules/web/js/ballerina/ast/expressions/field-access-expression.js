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
import Expression from './expression';

class FieldAccessExpression extends Expression {
    constructor(args) {
        super('FieldAccessExpression');
    }

    getExpression(){
        return this.generateExpression();
    }

    /**
     * A FieldAccessExpression can have either 1 or 2 child/children. First one being a
     * {@link VariableReferenceExpression} and the 2nd being {@link FieldAccessExpression} or another expression
     * such as {@link FunctionInvocationExpression}. Hence if 2nd child exists, we call getExpression() on that child.
     * @return {string}
     */
    generateExpression(isArrayExpression) {
        if (_.isEqual(_.size(this.getChildren()), 2)) {
            var firstVar = this.getChildren()[0];
            var secondVar = this.getChildren()[1];
            if (firstVar.getIsArrayExpression()) {
                return firstVar.generateExpression() + '[' + secondVar.generateExpression(true) + ']';
            } else {
                return firstVar.generateExpression() + '.' + secondVar.generateExpression(false);
            }
        } else if (_.isEqual(_.size(this.getChildren()), 1)) {
            var exp = this.getChildren()[0];
            if (this.getFactory().isBasicLiteralExpression(exp)) {
                if (isArrayExpression){
                    return exp.generateExpression();
                } else {
                    return exp.getBasicLiteralValue();
                }
            } else if (this.getFactory().isFieldAccessExpression(exp)) {
                return exp.generateExpression(isArrayExpression);
            } else {
                return exp.generateExpression();
            }
        } else {
            log.error('Error in determining Field Access expression');
        }
    }

    /**
     * initialize FieldAccessExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        _.each(jsonNode.children, childNode => {
            var child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default FieldAccessExpression;
