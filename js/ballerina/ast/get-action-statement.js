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
define(['lodash', 'log', './action-invocation-statement'], function (_, log, actionInvocationStatement) {


    var getActionStatement = function (args) {
        this._connector = _.get(args, 'connector');
        this._message =  _.get(args, 'message')    || [];
        this._path = _.get(args, 'path');
        this.isUserDropped = _.get(args, 'isUserDropped') || false;
        actionInvocationStatement.call(this);
    };

    getActionStatement.prototype = Object.create(actionInvocationStatement.prototype);
    getActionStatement.prototype.constructor = getActionStatement;

    getActionStatement.prototype.setConnector = function(connector){
        if(!_.isNil(connector)){
            this._connector = connector;
        }
    };

    getActionStatement.prototype.getConnector = function(){
        return this._connector;
    };

    getActionStatement.prototype.canBeActionOf = function(node){

    }

    return getActionStatement;
});