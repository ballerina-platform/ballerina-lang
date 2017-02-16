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
define(['lodash', 'log', 'event_channel', '../expression-visitor', './abstract-source-gen-visitor'], function(_, log, 
       EventChannel, ExpressionVisitor, AbstractSourceGenVisitor) {

    /**
     * Constructor for the Abstract Source Generation Visitor for the expressions
     * @param parent
     * @constructor
     */
    var AbstractExpressionSourceGenVisitor = function(parent) {
        this._generatedSource = '';
        this.parent = parent;
        ExpressionVisitor.call(this);
    };

    AbstractExpressionSourceGenVisitor.prototype = Object.create(ExpressionVisitor.prototype);
    AbstractExpressionSourceGenVisitor.prototype.constructor = AbstractSourceGenVisitor;

    AbstractExpressionSourceGenVisitor.prototype.getGeneratedSource = function () {
        return this._generatedSource;
    };

    AbstractExpressionSourceGenVisitor.prototype.setGeneratedSource = function (generatedSource) {
        this._generatedSource = generatedSource;
    };

    AbstractExpressionSourceGenVisitor.prototype.appendSource = function (source) {
        this._generatedSource += source;
    };

    AbstractExpressionSourceGenVisitor.prototype.getParent = function () {
        return this.parent;
    };

    return AbstractExpressionSourceGenVisitor;
});