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
define(['lodash', './expression'], function (_, Expression) {

    var StructFieldAccessExpression = function (args) {
        Expression.call(this, 'StructFieldAccessExpression');
        this._isLHSExpr = _.get(args, 'isLHSExpr', false);
    };

    StructFieldAccessExpression.prototype = Object.create(Expression.prototype);
    StructFieldAccessExpression.prototype.constructor = StructFieldAccessExpression;

    /**
     * Flag indicating whether the entire expression is a left hand side expression
     * @returns {boolean}
     */
    StructFieldAccessExpression.prototype.isLHSExpression = function () {
       return this._isLHSExpr;
    };

    /**
     * A StructFieldAccessExpression can have either 1 or 2 child/children. First one being a
     * {@link VariableReferenceExpression} and the 2nd being {@link StructFieldAccessExpression} or another expression
     * such as {@link FunctionInvocationExpression}. Hence if 2nd child exists, we call getExpression() on that child.
     * @return {string}
     */
    StructFieldAccessExpression.prototype.getExpression = function () {
        var variableReferenceExpression = "";
          if (_.isEqual(_.size(this.getChildren()), 2)) {
              variableReferenceExpression = this.getChildren()[0].generateExpression();
              var structFieldAccessExpression = this.getChildren()[1].getExpression();
              return variableReferenceExpression + "." + structFieldAccessExpression;
          } else if (_.isEqual(_.size(this.getChildren()), 1)) {
              variableReferenceExpression = this.getChildren()[0].generateExpression();
              return variableReferenceExpression;
          }
    };

    /**
     * initialize StructFieldAccessExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    StructFieldAccessExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };
    
    return StructFieldAccessExpression;
});