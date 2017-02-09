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

define(['jquery', 'lodash', 'log', 'event_channel', 'jquery_context_menu'],
    function ($, _, log, EventChannel) {

        /**
         * @class ContextMenu
         * @augments EventChannel
         * @param args {Object}
         * @constructor
         */
        var ContextMenu = function (args) {
            _.assign(this, args);
            this.init();
        };

        ContextMenu.prototype = Object.create(EventChannel.prototype);
        ContextMenu.prototype.constructor = ContextMenu;

        ContextMenu.prototype.init = function () {
            if (_.isFunction(this.provider)) {
                this.container.contextMenu({
                    selector: this.selector,
                    build: this.provider,
                    zIndex: 4
                });
            } else {
                this.container.contextMenu({
                    selector: this.selector,
                    callback: this.callback,
                    items: this.items,
                    zIndex: 4
                });
            }
        };

        return ContextMenu;

    });