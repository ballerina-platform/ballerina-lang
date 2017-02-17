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
define(['require','lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor','./expression-visitor-factory'],
    function(require, _, log, EventChannel, AbstractStatementSourceGenVisitor,ExpressionVisitorFactory) {

    var LeftOperandExpressionVisitor = function(parent){
        AbstractStatementSourceGenVisitor.call(this,parent);
    };

    LeftOperandExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
    LeftOperandExpressionVisitor.prototype.constructor = LeftOperandExpressionVisitor;

    LeftOperandExpressionVisitor.prototype.canVisitLeftOperandExpression = function(leftOperandExpression){
        return true;
    };

    LeftOperandExpressionVisitor.prototype.beginVisitLeftOperandExpression = function(leftOperandExpression){
        if (!_.isUndefined(leftOperandExpression.getLeftOperandExpressionString())) {
            this.appendSource(leftOperandExpression.getLeftOperandExpressionString());
        }
        log.debug('Begin Visit Left Operand Expression');
    };

    LeftOperandExpressionVisitor.prototype.endVisitLeftOperandExpression = function(leftOperandExpression){
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Left Operand Expression');
    };

    return LeftOperandExpressionVisitor;
});