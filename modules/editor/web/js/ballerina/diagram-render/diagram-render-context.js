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

define(['ballerina/env/environment','ballerina/env/package-scoped-environment'], function (Environment,PackageScopedEnvironemnt) {

    /**
     * Context data conveyor for diagram rendering.
     * @constructor
     */
    var DiagramRenderContext = function () {
        // map object for storing view references against models
        this.viewModelMap = {};
        this.environment = Environment;
        this.packagedScopedEnvironemnt = PackageScopedEnvironemnt;
    };

    /**
     * getter for viewModelMap
     * @returns {{}|*}
     */
    DiagramRenderContext.prototype.getViewModelMap = function () {
        return this.viewModelMap;
    };

    /**
     * get view for node
     * @returns {{}|*}
     */
    DiagramRenderContext.prototype.getViewOfModel = function (model) {
        return _.get(this.viewModelMap, model.id);
    };

    /**
     * set view of node
     * @returns {{}|*}
     */
    DiagramRenderContext.prototype.setViewOfModel = function (model, view) {
        return _.set(this.viewModelMap, model.id, view);
    };

    /**
     * get environment
     * @returns {*}
     */
    DiagramRenderContext.prototype.getEnvironment = function () {
        return this.environment;
    };

    /**
     * get packageScopedEnvironment
     * @returns {*}
     */
    DiagramRenderContext.prototype.getPackagedScopedEnvironment = function () {
        return this.packagedScopedEnvironemnt;
    };

    return DiagramRenderContext;
});