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

define(['lodash', 'jquery', 'event_channel', './../ast/connection-declaration', 'log', 'd3utils'],
    function (_, $, EventChannel, ConnectionDeclaration, log, D3Utils) {

    var ConnectionDeclaration = function (model) {
        this._model = model;
    };

    ConnectionDeclaration.prototype = Object.create(EventChannel.prototype);
    ConnectionDeclaration.prototype.constructor = ConnectionDeclaration;

    ConnectionDeclaration.prototype.init = function (model, container) {

        if (_.isUndefined(model)) {
            log.error('Model Definition undefined');
            throw 'Model Definition Undefined';
        }
        this._model = model;

        if (_.isUndefined(container)) {
            log.error('Container is undefined');
            throw 'Container is undefined';
        }
        this._container = container;
    };

    ConnectionDeclaration.prototype.getModel = function () {
        return this._model;
    };

    ConnectionDeclaration.prototype.getContainer = function () {
        return this._container;
    };

    ConnectionDeclaration.prototype.render = function () {
        var group = D3Utils.draw.group(this._container);
        var rect = D3Utils.draw.rect(10, 10, 100, 100, 0, 0, group, "#FFFFFF");
        window.console.log("Rendering the Worker Declaration");

        group.rect = rect;

        return group;
        window.console.log("Rendering the Connection Declaration");
    };

    return ConnectionDeclaration;
});