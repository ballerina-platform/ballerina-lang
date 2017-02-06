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

/**
 * Module to provide items(tool groups, tools) for a given tool palette.
 */
define(['log', 'lodash', './../env/package', './../tool-palette/tool-palette', './../tool-palette/tool-group',
        './../env/environment', './initial-definitions', 'event_channel', './../ast/ballerina-ast-factory'],
    function (log, _, Package, ToolPalette, ToolGroup, Environment, InitialTools, EventChannel, BallerinaASTFactory) {

        /**
         * constructs ToolPaletteItemProvider
         * @param {Object} args - data to create ToolPaletteItemProvider
         * @constructor
         */
        var ToolPaletteItemProvider = function (args) {
            // array which contains initial tool groups
            this._initialToolGroups = _.get(args, 'initialToolGroups', []);
            // array which contains all the tool groups for a particular tool palette item provider
            this._toolGroups = _.get(args, 'toolGroups', []);
            // array which contains tool groups that are added on the fly
            this._dynamicToolGroups = _.get(args, 'dynamicToolGroups', []);
            this.init();
        };

        ToolPaletteItemProvider.prototype = Object.create(EventChannel.prototype);
        ToolPaletteItemProvider.prototype.constructor = ToolPaletteItemProvider;

        /**
         * init function
         */
        ToolPaletteItemProvider.prototype.init = function () {
            this._initialToolGroups = _.slice(InitialTools);
            this._toolGroups = _.merge(this._initialToolGroups, this._dynamicToolGroups);
        };

        /**
         * returns initial tool groups
         * @returns {Object[]}
         */
        ToolPaletteItemProvider.prototype.getInitialToolGroups = function () {
            return this._initialToolGroups;
        };

        /**
         * returns all the tool groups
         * @returns {Object[]}
         */
        ToolPaletteItemProvider.prototype.getToolGroups = function () {
            return this._toolGroups;
        };

        /**
         * sets tool palette
         * @param toolPalette - tool palette
         */
        ToolPaletteItemProvider.prototype.setToolPalette = function (toolPalette) {
            this._toolPalette = toolPalette;
        };

        /**
         * function to add imports. Packages will be converted to a ToolGroup and added to relevant arrays
         * @param package - package to be imported
         */
        ToolPaletteItemProvider.prototype.addImport = function (package) {
            if (package instanceof Package) {
                var group = this.getToolGroup(package);
                this._dynamicToolGroups.push(group);
                this._toolGroups.push(group);
            }
        };

        /**
         * Adds a tool group view to the tool palette for a given package
         * @param package - package to be added
         */
        ToolPaletteItemProvider.prototype.addImportToolGroup = function (package) {
            if (package instanceof Package) {
                var group = this.getToolGroup(package);
                this._toolPalette.addVerticallyFormattedToolGroup({group: group});
            }
        };

        /**
         * returns the array of dynamicToolGroups
         * @returns {Object[]}
         */
        ToolPaletteItemProvider.prototype.getDynamicToolGroups = function () {
            return this._dynamicToolGroups;
        };

        /**
         * Create and return a ToolGroup object for a given package
         * @param package {Package} Package Object.
         */
        ToolPaletteItemProvider.prototype.getToolGroup = function (package) {
            var definitions = [];
            var self = this;
            _.each(package.getConnectors(), function (connector) {
                var packageName = _.last(_.split(package.getName(), '.'));
                connector.nodeFactoryMethod = BallerinaASTFactory.createConnectorDeclaration;
                connector.meta = {
                    connectorName: connector.getName(),
                    connectorPackageName: packageName
                };
                //TODO : use a generic icon
                connector.icon = "images/tool-icons/connector.svg";
                connector.title = connector.getTitle();
                connector.id = connector.getName();
                definitions.push(connector);
                _.each(connector.getActions(), function (action, index, collection) {
                    /* We need to add a special class to actions to indent them in tool palette. */
                    action.classNames = "tool-connector-action";
                    if ((index + 1 ) == collection.length) {
                        action.classNames = "tool-connector-action tool-connector-last-action";
                    }
                    action.meta = {
                        action: action.getAction(),
                        actionConnectorName: connector.getName(),
                        actionPackageName: packageName
                    };
                    action.icon = "images/tool-icons/action.svg";
                    action.title = action.getTitle();
                    action.nodeFactoryMethod = BallerinaASTFactory.createAggregatedActionInvocationExpression;
                    action.id = connector.getName() + '-' + action.getAction();
                    definitions.push(action);
                });
            });

            _.each(package.getFunctionDefinitions(), function (functionDef) {
                var packageName = _.last(_.split(package.getName(), '.'));
                functionDef.nodeFactoryMethod = BallerinaASTFactory.createAggregatedFunctionInvocationStatement;

                functionDef.meta = {
                    package: functionDef.getName(),
                    function: functionDef.getName(),
                    params: functionDef.getName()
                };
                //TODO : use a generic icon
                functionDef.icon = "images/tool-icons/function.svg";
                functionDef.title = functionDef.getTitle();
                functionDef.id = functionDef.getName();
                definitions.push(functionDef);

                var toolGroupID = package.getName() + "-tool-group";
                // registering function name-modified event
                functionDef.on('name-modified', function(newName, oldName){
                    self.updateToolItem(toolGroupID, functionDef, newName);
                });
            });

            var group = new ToolGroup({
                toolGroupName: package.getName(),
                toolGroupID: package.getName() + "-tool-group",
                toolOrder: "vertical",
                toolDefinitions: definitions
            });

            package.on('connector-defs-added', function (child) {
                var nodeFactoryMethod = BallerinaASTFactory.createConnectorDeclaration;
                var toolGroupID = package.getName() + "-tool-group";
                var icon = "images/tool-icons/connector.svg";
                this.addToToolGroup(toolGroupID, child, nodeFactoryMethod, icon);
            }, this);

            package.on('function-defs-added', function (child) {
                var nodeFactoryMethod = BallerinaASTFactory.createAggregatedFunctionInvocationStatement;
                var toolGroupID = package.getName() + "-tool-group";
                var icon = "images/tool-icons/function.svg";
                this.addToToolGroup(toolGroupID, child, nodeFactoryMethod, icon);

                child.on('name-modified', function(newName, oldName){
                    self.updateToolItem(toolGroupID, child, newName);
                });
            }, this);

            var self = this;
            package.on('function-def-removed', function (functionDef) {
                var toolGroupID = package.getName() + "-tool-group";
                var toolId = functionDef.getFunctionName();
                self._toolPalette.removeToolFromGroup(toolGroupID, toolId);
            });

            return group;
        };

        /**
         * Adds the tool view to the given tool group
         * @param toolGroupID - tool group id
         * @param toolItem - tool item to be added
         * @param nodeFactoryMethod - factory method to create instance when the tool being dragged and dropped
         */
        ToolPaletteItemProvider.prototype.addToToolGroup = function (toolGroupID, toolItem, nodeFactoryMethod, icon) {
            var tool = {};
            tool.nodeFactoryMethod = nodeFactoryMethod;
            tool.meta = {
                connectorName: toolItem.getName(),
                connectorPackageName: ''
            };
            //TODO : use a generic icon
            tool.icon = icon;
            tool.title = toolItem.getName();
            tool.id = toolItem.getName();
            this._toolPalette.addNewToolToGroup(toolGroupID, tool);
        };

        /**
         * updates tool item with given new values
         * @param {string} toolGroupID - Id of the tool group
         * @param {Object} toolItem - tool object
         * @param {Object} newValue - new value for the tool
         */
        ToolPaletteItemProvider.prototype.updateToolItem = function (toolGroupID, toolItem, newValue) {
            this._toolPalette.updateToolPaletteItem(toolGroupID, toolItem, newValue);
        };

        return ToolPaletteItemProvider;
    });