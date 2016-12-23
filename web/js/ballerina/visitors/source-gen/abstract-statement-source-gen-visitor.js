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
define(['lodash', 'log', 'event_channel', '../statement-visitor', './abstract-source-gen-visitor'], function(_, log, EventChannel, StatementVisitor, AbstractSourceGenVisitor) {

    /**
     * Constructor for the Abstract Source Generation Visitor for the statements
     * @param parent
     * @constructor
     */
    var AbstractStatementSourceGenVisitor = function(parent) {
        this._generatedSource = '';
        this.parent = parent;
        StatementVisitor.call(this);
    };

    AbstractStatementSourceGenVisitor.prototype = Object.create(StatementVisitor.prototype);
    AbstractStatementSourceGenVisitor.prototype.constructor = AbstractSourceGenVisitor;

    AbstractStatementSourceGenVisitor.prototype.getGeneratedSource = function () {
        return this._generatedSource;
    };

    AbstractStatementSourceGenVisitor.prototype.setGeneratedSource = function (generatedSource) {
        this._generatedSource = generatedSource;
    };

    AbstractStatementSourceGenVisitor.prototype.appendSource = function (source) {
        this._generatedSource += source;
    };

    AbstractStatementSourceGenVisitor.prototype.getParent = function () {
        return this.parent;
    };

    return AbstractStatementSourceGenVisitor;
});