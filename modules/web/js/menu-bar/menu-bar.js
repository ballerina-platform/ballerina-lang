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
define(['require', 'log', 'jquery', 'lodash', 'event_channel', './menu-definitions', './menu-group'],

    function (require, log, $, _, EventChannel, menuDefinitions, MenuGroup) {

        var MenuBar = function (options) {
            var errMsg;
            if(!_.has(options, 'container')){
                errMsg = 'Unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(options, 'container'));
            this._$parent_el = container;
            this._options = options;
            this._menuGroups = {};
        };

        MenuBar.prototype = Object.create(EventChannel.prototype);
        MenuBar.prototype.constructor = MenuBar;

        MenuBar.prototype.render = function () {
            var parent = this._$parent_el;
            var self = this;
            var _options = this._options;
            var application = _.get(this._options, "application");

            // Iterate over menu groups
            _.forEach(menuDefinitions, function (menuGroupDefinition) {
                    var menuGroupOpts = {definition: _.cloneDeep(menuGroupDefinition)};
                    _.set(menuGroupOpts, 'options', _.cloneDeep(_.get(_options, 'menu_group')));
                    _.set(menuGroupOpts, 'options.parent', parent);
                    _.set(menuGroupOpts, 'options.application', application);
                    var menuGroup = new MenuGroup(menuGroupOpts);
                    menuGroup.render();
                    _.set(self._menuGroups, menuGroup.getID(), menuGroup);
                }
            );
        };

        MenuBar.prototype.getMenuItemByID = function (id) {
            return _.get(this._menuGroups, id);
        };

        return MenuBar;
});