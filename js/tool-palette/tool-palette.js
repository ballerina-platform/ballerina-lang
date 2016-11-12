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
define(['require', 'log', 'jquery', 'backbone', './tool-group-view', './tool-group',
        'main_elements', 'processors'],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup,
              MainElements, Processors) {

    var ToolPalette = Backbone.View.extend({
        initialize: function (options) {
            var errMsg;
            if (!_.has(options, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(options, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;
            this._initTools();
        },

        _initTools: function(){

            var _toolGroups = [];
            // Create main tool group
            var mainToolGroup = new ToolGroup({
                toolGroupName: "Main Elements",
                toolGroupID: "main-tool-group",
                toolDefinitions: MainElements.lifelines
            });
            _toolGroups.push(mainToolGroup);

            // Create mediators tool group
            var mediatorsToolGroup = new ToolGroup({
                toolGroupName: "Mediators",
                toolGroupID: "mediators-tool-group",
                toolDefinitions: _.assign(Processors.manipulators, Processors.flowControllers)
            });
            _toolGroups.push(mediatorsToolGroup);

            this._toolGroups = _toolGroups;
        },

        render: function () {
            var self = this;
            var toolPaletteDiv = $('<div></div>');
            this._$parent_el.append(toolPaletteDiv);
            this.$el = toolPaletteDiv;

            this._toolGroups.forEach(function (group) {
                var groupView = new ToolGroupView({model: group});
                groupView.render(self.$el);
                self.$el.addClass('non-user-selectable');
            });

            return this;
        }
    });

    return ToolPalette;
});
