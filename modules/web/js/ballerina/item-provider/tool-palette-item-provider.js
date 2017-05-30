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
import log from 'log';
import _ from 'lodash';
import Package from './../env/package';
import ToolPalette from './../tool-palette/tool-palette';
import ToolGroup from './../tool-palette/tool-group';
import Environment from './../env/environment';
import InitialTools from './initial-definitions';
import EventChannel from 'event_channel';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';

/**
 * constructs ToolPaletteItemProvider
 * @param {Object} args - data to create ToolPaletteItemProvider
 * @constructor
 */
class ToolPaletteItemProvider extends EventChannel {
    constructor(args) {
        super();
        // array which contains initial tool groups
        this._initialToolGroups = _.get(args, 'initialToolGroups', []);
        // array which contains all the tool groups for a particular tool palette item provider
        this._toolGroups = _.get(args, 'toolGroups', []);
        // array which contains tool groups that are added on the fly
        this._dynamicToolGroups = _.get(args, 'dynamicToolGroups', []);

        var self = this;
        // Packages to be added to the tool palette by default in order.
        this._defaultImportedPackages = [];
        _.forEach(["ballerina.net.http", "ballerina.lang.*"],
            function (defaultPackageString) {
                var packagesToImport = Environment.searchPackage(defaultPackageString);
                _.forEach(packagesToImport, function (packageToImport) {
                    self._defaultImportedPackages.push(packageToImport);
                });
            });

        // views added to tool palette for each imported package keyed by package name
        this._importedPackagesViews = {};

        this.iconSrcs = {
            function: require('./../../../images/tool-icons/function.svg'),
            connector: require('./../../../images/tool-icons/connector.svg'),
            action: require('./../../../images/tool-icons/action.svg'),
        };

        this.cssClass = {
            function: "icon fw fw-function",
            connector: "icon fw fw-connector",
            action: "icon fw fw-dgm-action",
        };

        this.icons = {};
        Object.keys(this.iconSrcs).forEach((iconName) => {
            var icon = document.createElement('img');
            icon.setAttribute('src', this.iconSrcs[iconName]);
            this.icons[iconName] = icon;
        });

        this.init();
    }

    /**
     * init function
     */
    init() {
        var self = this;

        this._initialToolGroups = _.slice(InitialTools);
        this._toolGroups = _.merge(this._initialToolGroups, this._dynamicToolGroups);

        // Adding default packages
        var sortedPackages = _.sortBy(self._defaultImportedPackages, [function (pckg) {
            return pckg.getName();
        }]);
        _.forEach(sortedPackages, function (packageToImport) {
            self.addImport(packageToImport);
        });
    }

    /**
     * returns initial tool groups
     * @returns {Object[]}
     */
    getInitialToolGroups() {
        return this._initialToolGroups;
    }

    /**
     * returns all the tool groups
     * @returns {Object[]}
     */
    getToolGroups() {
        return this._toolGroups;
    }

    /**
     * sets tool palette
     * @param toolPalette - tool palette
     */
    setToolPalette(toolPalette) {
        this._toolPalette = toolPalette;
    }

    /**
     * function to add imports. Packages will be converted to a ToolGroup and added to relevant arrays
     * @param pckg - package to be imported
     * @param index -  index to add
     * @returns {number} -1 if exisiting package and 1 otherwise
     */
    addImport(pckg, index) {
        if (pckg instanceof Package) {
            var group = this.getToolGroup(pckg);

            let exisitingPackageWithName = _.filter(this._dynamicToolGroups, function(pkg) {
                return pkg.attributes.toolGroupName === pckg.getName();
            });

            /**
             * Add the new package to the arrays, only if it doesn't exists already.
             */
            if (_.isEmpty(exisitingPackageWithName)) {
                if (_.isNil(index)) {
                    this._dynamicToolGroups.push(group);
                    this._toolGroups.push(group);
                } else {
                    this._dynamicToolGroups.splice(index, 0, group);
                    this._toolGroups.splice(index, 0, group);
                }
                return 1;
            }
        }

        return -1;
    }

    /**
     * Adds a tool group view to the tool palette for a given package
     * @param pckg - package to be added
     */
    addImportToolGroup(pckg, opts) {
        var options = _.isUndefined(opts) ? {} : opts;
        if (pckg instanceof Package) {
            var isADefaultPackage = _.includes(this._defaultImportedPackages, pckg);
            if (!isADefaultPackage) { // Removing existing package
                // Re-adding the package
                var group = this.getToolGroup(pckg);

                // Need to remove any old group models added for this package and add the new one
                _.remove(this._toolGroups, function(group){
                    return group.get('toolGroupName') === pckg.getName();
                });

                // Similarly need to remove old views
                var existingView = this._importedPackagesViews[pckg.getName()];

                if(!_.isUndefined(existingView)){
                    var isCollapsed = existingView.$el.find('.tool-group-header').hasClass('tool-group-header-collapse');
                    // if 'tool-group-header-collapse' class is present the view was collapsed. So add the new view also in
                    // collapsed state
                    options.collapsed = isCollapsed;
                    existingView.remove();
                }

                this._toolGroups.push(group);
                var groupView = this._toolPalette.addVerticallyFormattedToolGroup({group: group, options: options});
                this._importedPackagesViews[pckg.getName()] = groupView;
            }
        }
    }

    /**
     * Removes a tool group view from the tool palette for a given package name
     * @param packageName - name of the package to be removed
     */
    removeImportToolGroup(packageName) {
        var defaultPackageIndex = _.findIndex(this._defaultImportedPackages, function(p) {
            return p.getName() === packageName;
        });

        if(defaultPackageIndex !== -1) {
            // The package is a default package.
            // Default packages should not be removed from the view.
            return;
        }

        var removingView = this._importedPackagesViews[packageName];
        if(!_.isNil(removingView)){
            removingView.remove();
        }
    }

    /**
     * Keeps the tool palette view keyed by its package name
     * @param packageName - name of the package to be saved
     * @param view - Backbone view of the package in the tool palette
     */
    saveImportToolGroupView(packageName, view) {
        this._importedPackagesViews[packageName] = view;
    }

    /**
     * returns the array of dynamicToolGroups
     * @returns {Object[]}
     */
    getDynamicToolGroups() {
        return this._dynamicToolGroups;
    }

    /**
     * Create and return a ToolGroup object for a given package
     * @param pckg {Package} Package Object.
     */
    getToolGroup(pckg) {
        var definitions = [];
        var self = this;

        // Sort the connector package by name
        var connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
            return connectorPackage.getName();
        }]);

        var functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
            return functionDef.getName();
        }]);

        _.each(connectorsOrdered, function (connector) {
            var packageName = _.last(_.split(pckg.getName(), '.'));
            connector.nodeFactoryMethod = BallerinaASTFactory.createConnectorDeclaration;
            var getParamString = function() {
                var params = _.map(connector.getParams(), function(p){return p.identifier;});
                return _.join(params, ', ');
            };
            connector.meta = {
                connectorName: connector.getName(),
                connectorPackageName: packageName,
                fullPackageName: pckg.getName(),
                params: getParamString()
            };
            //TODO : use a generic icon
            connector.icon = self.icons.connector;
            connector.title = connector.getName();
            connector.id = connector.getName();
            definitions.push(connector);

            var toolGroupID = pckg.getName() + "-tool-group";
            // registering connector name-modified event
            connector.on('name-modified', function(newName, oldName){
                self.updateToolItem(toolGroupID, connector, 'name', newName, 'connectorName');
                _.forEach(connector.getActions(), function (action) {
                    self.updateToolItem(toolGroupID, action, '', newName, 'actionConnectorName');
                });
            });

            connector.on('param-added', function (newName, oldName) {
                var paramString = getParamString();
                self.updateToolItem(toolGroupID, connector, 'params', paramString, 'params');
            });

            var actionsOrdered = _.sortBy(connector.getActions(), [function (action) {
                return action.getName();
            }]);
            _.each(actionsOrdered, function (action, index, collection) {
                /* We need to add a special class to actions to indent them in tool palette. */
                action.setId(connector.getName() + "-" + action.getName());
                action.classNames = "tool-connector-action";
                if ((index + 1 ) == collection.length) {
                    action.classNames = "tool-connector-action tool-connector-last-action";
                }
                action.meta = {
                    action: action.getName(),
                    actionConnectorName: connector.getName(),
                    actionPackageName: packageName,
                    fullPackageName: pckg.getName()
                };
                action.icon = self.icons.action;

                action.title = action.getName();

                action.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0){
                    action.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement;
                }

                action.id = action.getId();
                definitions.push(action);
                var toolGroupID = pckg.getName() + "-tool-group";
                // registering connector action name-modified event
                action.on('name-modified', function(newName, oldName){
                    self.updateToolItem(toolGroupID, action, 'name', newName, 'action');
                });
            });
            connector.on('connector-action-added', function (action) {
                var actionIcon = self.icons.action;
                var toolGroupID = pckg.getName() + "-tool-group";
                action.classNames = "tool-connector-action";

                action.meta = {
                    action: action.getName(),
                    actionConnectorName: connector.getName()
                };

                // registering connector action name-modified event
                action.on('name-modified', function(newName, oldName){
                    self.updateToolItem(toolGroupID, action, 'name', newName, 'action');
                });

                var actionNodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0){
                    actionNodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement;
                }
                self.addToToolGroup(toolGroupID, action, actionNodeFactoryMethod, actionIcon);
            });

            connector.on('connector-action-removed', function (action) {
                var toolGroupID = pckg.getName() + "-tool-group";
                var toolId = connector.getName() + '-' + action.getActionName();
                self._toolPalette.removeToolFromGroup(toolGroupID, toolId);
            });
        });

        _.each(functionsOrdered, function (functionDef) {
            if(functionDef.getName() === "main") {
                //do not add main function to tool palette
                return;
            }

            var packageName = _.last(_.split(pckg.getName(), '.'));
            if (functionDef.getReturnParams().length > 0){
                functionDef.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationExpression;
            } else {
                functionDef.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
            }
            functionDef.meta = {
                functionName: functionDef.getName(),
                packageName: packageName,
                params: getArgumentString(functionDef.getParameters()),
                returnParams: getReturnParamString(functionDef.getReturnParams()),
                operandType: getReturnParamString(functionDef.getReturnParams()),
                fullPackageName: pckg.getName()
            };
            functionDef.icon = self.icons.function;

            functionDef.title = functionDef.getName();
            functionDef.id = functionDef.getName();
            definitions.push(functionDef);

            var toolGroupID = pckg.getName() + "-tool-group";
            // registering function name-modified event
            functionDef.on('name-modified', function(newName, oldName){
                self.updateToolItem(toolGroupID, functionDef, 'name', newName, 'functionName');
            });
        });

        var group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: pckg.getName() + "-tool-group",
            toolOrder: "vertical",
            toolDefinitions: definitions
        });

        pckg.on('connector-defs-added', function (connector) {
            var packageName = _.last(_.split(pckg.getName(), '.'));
            var nodeFactoryMethod = BallerinaASTFactory.createConnectorDeclaration;
            var toolGroupID = pckg.getName() + "-tool-group";
            var icon = self.icons.connector;

            var getParamString = function() {
                var params = _.map(connector.getParams(), function(p){return p.identifier;});
                return _.join(params, ',');
            };

            connector.meta = {
                connectorName: connector.getName(),
                connectorPackageName: packageName,
                fullPackageName: pckg.getName(),
                params: getParamString()
            };

            this.addToToolGroup(toolGroupID, connector, nodeFactoryMethod, icon);

            connector.on('name-modified', function (newName, oldName) {
                self.updateToolItem(toolGroupID, connector, 'name', newName, 'connectorName');
                _.forEach(connector.getActions(), function (action) {
                    self.updateToolItem(toolGroupID, action, '', newName, 'actionConnectorName');
                });
            });

            connector.on('param-added', function (newName, oldName) {
                var paramString = getParamString();
                self.updateToolItem(toolGroupID, connector, 'params', paramString, 'params');
            });

            connector.on('connector-action-added', function (action) {
                var actionIcon = self.icons.action;
                action.classNames = "tool-connector-action";
                action.setId(action.getId());
                var actionNodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0){
                    actionNodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement;
                }

                // Setting the meta attributes to be passed as the action arguments
                action.meta = {
                    action: action.getName(),
                    arguments: action.getParameters(),
                    actionConnectorName: this.getName()
                };
                self.addToToolGroup(toolGroupID, action, actionNodeFactoryMethod, actionIcon);

                action.on('name-modified', function (newName, oldName) {
                    self.updateToolItem(toolGroupID, action, 'name', newName, 'action');
                });

            });

            connector.on('connector-action-removed', function (action) {
                var toolGroupID = pckg.getName() + "-tool-group";
                var toolId = connector.getName() + '-' + action.getActionName();
                self._toolPalette.removeToolFromGroup(toolGroupID, toolId);
            });

        }, this);

        pckg.on('function-defs-added', function (functionDef) {
            if(functionDef.getName() === "main") {
                //do not add main function to tool palette
                return;
            }

            var nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
            if (functionDef.getReturnParams().length > 0){
                nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationExpression;
            }

            // since functions are added to the current package, function name does not need
            // packageName:functionName format
            functionDef.meta = {
                functionName: functionDef.getName()
            };
            var toolGroupID = pckg.getName() + "-tool-group";
            var icon = self.icons.function;
            this.addToToolGroup(toolGroupID, functionDef, nodeFactoryMethod, icon);

            functionDef.on('name-modified', function(newName, oldName){
                self.updateToolItem(toolGroupID, functionDef, 'name', newName, 'functionName');
            });
        }, this);

        var self = this;
        pckg.on('function-def-removed', function (functionDef) {
            var toolGroupID = pckg.getName() + "-tool-group";
            var toolId = functionDef.getFunctionName();
            self._toolPalette.removeToolFromGroup(toolGroupID, toolId);
        });

        // registering event handler for 'connector-def-removed' event
        pckg.on('connector-def-removed', function (connectorDef) {
            var toolGroupID = pckg.getName() + "-tool-group";
            var toolId = connectorDef.getConnectorName();
            self._toolPalette.removeToolFromGroup(toolGroupID, toolId);

            // remove connector action definitions inside this connector
            var actions = connectorDef.getConnectorActionDefinitions();

            _.forEach(actions, function(action){
                var toolGroupID = pckg.getName() + "-tool-group";
                var toolId = connectorDef.getConnectorName() + '-' + action.getActionName();
                self._toolPalette.removeToolFromGroup(toolGroupID, toolId);
            });
        });

        return group;
    }

    /**
     * Adds the tool view to the given tool group
     * @param toolGroupID - tool group id
     * @param toolItem - tool item to be added
     * @param nodeFactoryMethod - factory method to create instance when the tool being dragged and dropped
     */
    addToToolGroup(toolGroupID, toolItem, nodeFactoryMethod, icon) {
        var tool = {};
        tool.nodeFactoryMethod = nodeFactoryMethod;
        tool.icon = icon;
        tool.title = toolItem.getName();
        tool.id = toolItem.getId();
        tool.classNames = toolItem.classNames;
        tool.meta = toolItem.meta;
        this._toolPalette.addNewToolToGroup(toolGroupID, tool);
    }

    /**
     * updates tool item with given new values
     * @param {string} toolGroupID - Id of the tool group
     * @param {Object} toolItem - tool object
     * @param {Object} newValue - new value for the tool
     */
    updateToolItem(toolGroupID, toolItem, attribute, newValue, metaAttr) {
        this._toolPalette.updateToolPaletteItem(toolGroupID, toolItem, attribute, newValue, metaAttr);
    }

    getNewImportPosition(newImportName) {
        var packageNames = [];
        packageNames = packageNames.concat(_.map(this._defaultImportedPackages, '_name'));
        for(var key in this._importedPackagesViews) {
            packageNames.push(key);
        }
        packageNames = _.sortBy(packageNames);
        return packageNames[_.sortedIndex(packageNames, newImportName)];
    }

    /**
     * Get the package to import, via environment
     * @param packageString
     */
    getPackageToImport(packageString) {
        return Environment.searchPackage(packageString);
    }
}

/**
 * Generate argument string from the argument array.
 * @param {Object} args argument array
 * @return {String} argument string
 * */
var getArgumentString = function(args){
    var argString = "";
    for (var itr = 0; itr < args.length; itr++) {
        argString += args[itr].name;
        if (args.length !== 1 && (args.length-1) !== itr) {
            argString += ",";
        }
    }
    return argString;
};

/**
 * Generate return parameter string.
 * @param {Object} args return parameter array.
 * @return {String} return argument string
 * */
var getReturnParamString = function(args){
    if(!_.isNil(args) && args.length !== 0) {
        return args[0].name;
    }
};

export default ToolPaletteItemProvider;
