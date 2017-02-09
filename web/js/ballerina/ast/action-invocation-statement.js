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
define(['lodash', 'log', './statement'], function (_, log, Statement) {

    /**
     * Class to represent a action invocation to ballerina.
     * @param args
     * @constructor
     */
    var ActionInvocationStatement = function (args) {
        Statement.call(this, args);
        this._connector = _.get(args, 'connector');
        this._action = _.get(args, 'action');
        this._message =  _.get(args, 'message') || [];
        this._path = _.get(args, 'path');
        this.type = "ActionInvocationStatement";
    };

    ActionInvocationStatement.prototype = Object.create(Statement.prototype);
    ActionInvocationStatement.prototype.constructor = ActionInvocationStatement;

    ActionInvocationStatement.prototype.setConnector = function(connector, options){
        if(!_.isNil(connector)){
            this.setAttribute('_connector', connector, options);
        }
    };

    ActionInvocationStatement.prototype.getConnector = function(){
        return this._connector;
    };

    ActionInvocationStatement.prototype.setAction = function(action, options){
        if(!_.isNil(action)){
            this.setAttribute('_action', action, options);
        }
    };

    ActionInvocationStatement.prototype.getAction = function(){
        return this._action;
    };

    ActionInvocationStatement.prototype.getMessage = function () {
        return this._message;
    };

    ActionInvocationStatement.prototype.setMessage = function (message, options) {
        this.setAttribute('_message', message, options);
    };

    ActionInvocationStatement.prototype.getPath = function () {
        return this._path;
    };

    ActionInvocationStatement.prototype.setPath = function (path, options) {
        this.setAttribute('_path', path, options);
    };

    return ActionInvocationStatement;

});