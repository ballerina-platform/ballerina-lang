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
define(['lodash', './callable-definition'], function (_, CallableDefinition) {

    var ActionDefinition = function (connectionDeclarations, variableDeclarations, workerDeclarations, statements,args) {
        CallableDefinition.call(this, connectionDeclarations, variableDeclarations, workerDeclarations, statements)
        this.args = args || [];
    };

    ActionDefinition.prototype = Object.create(CallableDefinition.prototype);
    ActionDefinition.prototype.constructor = ActionDefinition;

    ActionDefinition.prototype.setArgs = function(args){
        if(!_.isNil(args)){
            this.args = args;
        }
    };

    ActionDefinition.prototype.getArgs = function () {
        return this.args;
    };

    ActionDefinition.prototype.accept = function(visitor) {
        visitor.visitActionDefinition();
    };

    return ActionDefinition;

});