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
define(['lodash', './statement'], function (_, Statement) {

    /**
     * Class to represent a function invocation in ballerina.
     * @constructor
     */
    var FunctionInvocation = function (args) {
        this._packageName = _.get(args, 'package', '');
        this._functionName = _.get(args, 'function', 'callFunction');
        this._params = _.get(args, 'params');
        Statement.call(this, 'FunctionInvocation');
    };

    FunctionInvocation.prototype = Object.create(Statement.prototype);
    FunctionInvocation.prototype.constructor = FunctionInvocation;

    FunctionInvocation.prototype.setPackageName = function (packageName, options) {
        this.setAttribute('_packageName', packageName, options);
    };

    FunctionInvocation.prototype.setFunctionName = function (functionName, options) {
        this.setAttribute('_functionName', functionName, options);
    };

    FunctionInvocation.prototype.setParams = function (params, options) {
        this.setAttribute('_params', params, options);
    };

    FunctionInvocation.prototype.setFunctionalExpression = function(expression, options){
        if(!_.isNil(expression) && expression !== "") {
            var splittedText = expression.split("(",1)[0].split(":", 2);

            if(splittedText.length == 2){
                this.setPackageName(splittedText[0], options);
                this.setFunctionName(splittedText[1]);
            }else{
                this.setPackageName("", options);
                this.setFunctionName(splittedText[0]);
            }

            var params = expression.slice(((expression.indexOf(this._functionName) + 1)
                         + this._functionName.split("(", 1)[0].length), (expression.length - 1));

            this.setParams(params, options);
        }
    };

    FunctionInvocation.prototype.getFunctionalExpression = function(){
        var text = "";
        if (!_.isNil(this._packageName) && this._packageName !== "") {
            text += this._packageName + ":";
        }
        text += this._functionName + '('+ (this._params? this._params:'') +')';
        return text;
    };

    FunctionInvocation.prototype.getPackageName = function () {
        return this._packageName;
    };

    FunctionInvocation.prototype.getFunctionName = function () {
        return this._functionName;
    };

    FunctionInvocation.prototype.getParams = function () {
        var params;
        if(!_.isUndefined(this._params)) {
            params = this._params.split(',');
            return params;
        }
        else params = "";
        return params;
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    FunctionInvocation.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return FunctionInvocation;
});
