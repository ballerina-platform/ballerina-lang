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
define(['lodash', './ast-visitor', '../ast/reply-statement', 'd3'], function (_, ASTVisitor, ReplyStatement, d3) {

    var ReplyStatementView = function (model) {
        if (model instanceof ReplyStatement) {
            this.model = model;
        } else {
            this.model = undefined;
        }
    };

    ReplyStatementView.prototype.constructor = ReplyStatementView;

    ReplyStatementView.prototype.setModel = function (model) {
        if (model instanceof ReplyStatement) {
            this.model = model;
        } else {
            this.model = undefined;
        }
    };

    ReplyStatementView.prototype.getModel = function () {
        return this.model;
    };

    ReplyStatementView.prototype.render = function () {
        //Draw ReplyStatement

    };

    ReplyStatement.prototype.accept = function (visitor) {
        visitor.visitReplyStatementChildren(this);

        //invoke accept methods of children
    };

    ReplyStatement.prototype.visitChildren = function () {
        //invoke accept methods of children
    };

});