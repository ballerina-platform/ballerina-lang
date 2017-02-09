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
define(['lodash', 'log', 'event_channel', '../ast-visitor'], function(_, log, EventChannel, ASTVisitor) {

    /**
     * Constructor for the Abstract Source Generation Visitor
     * @param parent
     * @constructor
     */
    var AbstractSymbolTableGenVisitor = function(package) {
        ASTVisitor.call(this);
        this._package = package;
    };

    AbstractSymbolTableGenVisitor.prototype = Object.create(ASTVisitor.prototype);
    AbstractSymbolTableGenVisitor.prototype.constructor = AbstractSymbolTableGenVisitor;

    AbstractSymbolTableGenVisitor.prototype.getPackage = function () {
        return this._package;
    };

    return AbstractSymbolTableGenVisitor;
});