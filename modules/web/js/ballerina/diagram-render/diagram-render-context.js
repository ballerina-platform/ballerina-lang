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

import Environment from 'ballerina/env/environment';
import PackageScopedEnvironemnt from 'ballerina/env/package-scoped-environment';
import _ from 'lodash';

/**
 * Context data conveyor for diagram rendering.
 * @constructor
 */
class DiagramRenderContext {
    constructor(options) {
        // map object for storing view references against models
        this.viewModelMap = {};
        this.environment = Environment;
        this.packagedScopedEnvironemnt = PackageScopedEnvironemnt;
        this._application = _.get(options, 'application');
    }

    /**
    * getter for viewModelMap
    * @returns {{}|*}
    */
    getViewModelMap() {
        return this.viewModelMap;
    }

    /**
    * get view for node
    * @returns {{}|*}
    */
    getViewOfModel(model) {
        return _.get(this.viewModelMap, model.id);
    }

    /**
    * set view of node
    * @returns {{}|*}
    */
    setViewOfModel(model, view) {
        return _.set(this.viewModelMap, model.id, view);
    }

    /**
    * get environment
    * @returns {*}
    */
    getEnvironment() {
        return this.environment;
    }

    /**
    * get packageScopedEnvironment
    * @returns {*}
    */
    getPackagedScopedEnvironment() {
        return this.packagedScopedEnvironemnt;
    }

    /**
     * Get the applications
     * @return {*}
     */
     getApplication() {
         return this._application;
     }

    /**
     * Get the language server client controller
     * @return {LangServerClientController}
     */
     getLanguageServerClientController() {
         return this.getApplication().langseverClientController;
     }

}

export default DiagramRenderContext;
