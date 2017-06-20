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
import _ from 'lodash';
import EventChannel from 'event_channel';
import Package from './../env/package';
import ToolGroup from './../tool-palette/tool-group';
import InitialTools from './initial-definitions';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';

const iconFunction = require('./../../../images/tool-icons/function.svg');
const iconConnector = require('./../../../images/tool-icons/connector.svg');
const iconAction = require('./../../../images/tool-icons/action.svg');

/**
 * constructs ToolPaletteItemProvider
 * @param {Object} args - data to create ToolPaletteItemProvider
 * @constructor
 */
class ToolPaletteItemProvider extends EventChannel {
    constructor(args) {
        super();
        this._ballerinaFileEditor = _.get(args, 'editor');
        // array which contains initial tool groups
        this._initialToolGroups = _.get(args, 'initialToolGroups', []);
        // array which contains all the tool groups for a particular tool palette item provider
        this._toolGroups = _.get(args, 'toolGroups', []);
        // array which contains tool groups that are added on the fly
        this._dynamicToolGroups = _.get(args, 'dynamicToolGroups', []);

        // Packages to be added to the tool palette by default in order.
        this._defaultImportedPackages = [];

        // views added to tool palette for each imported package keyed by package name
        this._importedPackagesViews = {};

        this.iconSrcs = {
            function: iconFunction,
            connector: iconConnector,
            action: iconAction,
        };

        this.cssClass = {
            function: 'icon fw fw-function',
            connector: 'icon fw fw-connector',
            action: 'icon fw fw-dgm-action',
        };

        this.icons = {};
        Object.keys(this.iconSrcs).forEach((iconName) => {
            const icon = document.createElement('img');
            icon.setAttribute('src', this.iconSrcs[iconName]);
            this.icons[iconName] = icon;
        });

        this.init();
    }

    /**
     * init function
     */
    init() {
        this._initialToolGroups = _.slice(InitialTools);
        this._toolGroups = _.merge(this._initialToolGroups, this._dynamicToolGroups);

        // Adding default packages
        const sortedPackages = _.sortBy(this._defaultImportedPackages, [function (pckg) {
            return pckg.getName();
        }]);
        _.forEach(sortedPackages, (packageToImport) => {
            this.addImport(packageToImport);
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
            const group = this.getToolGroup(pckg);

            const exisitingPackageWithName = _.filter(this._dynamicToolGroups, (pkg) => {
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
        const options = _.isUndefined(opts) ? {} : opts;
        if (pckg instanceof Package) {
            const isADefaultPackage = _.includes(this._defaultImportedPackages, pckg);
            if (!isADefaultPackage) { // Removing existing package
                // Re-adding the package
                const group = this.getToolGroup(pckg);

                // Need to remove any old group models added for this package and add the new one
                _.remove(this._toolGroups, (toolGroup) => {
                    return toolGroup.get('toolGroupName') === pckg.getName();
                });

                // Similarly need to remove old views
                const existingView = this._importedPackagesViews[pckg.getName()];

                if (!_.isUndefined(existingView)) {
                    const isCollapsed = existingView.$el.find('.tool-group-header')
                                                .hasClass('tool-group-header-collapse');
                    // If 'tool-group-header-collapse' class is present the view was collapsed.
                    // So add the new view also in collapsed state
                    options.collapsed = isCollapsed;
                    existingView.remove();
                }

                this._toolGroups.push(group);
                const groupView = this._toolPalette.addVerticallyFormattedToolGroup({ group, options });
                this._importedPackagesViews[pckg.getName()] = groupView;
            }
        }
    }

    /**
     * Removes a tool group view from the tool palette for a given package name
     * @param packageName - name of the package to be removed
     */
    removeImportToolGroup(packageName) {
        const defaultPackageIndex = _.findIndex(this._defaultImportedPackages, (p) => {
            return p.getName() === packageName;
        });

        if (defaultPackageIndex !== -1) {
            // The package is a default package.
            // Default packages should not be removed from the view.
            return;
        }

        const removingView = this._importedPackagesViews[packageName];
        if (!_.isNil(removingView)) {
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
        const definitions = [];

        // Sort the connector package by name
        const connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
            return connectorPackage.getName();
        }]);

        const functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
            return functionDef.getName();
        }]);

        _.each(connectorsOrdered, (connector) => {
            const packageName = _.last(_.split(pckg.getName(), '.'));
            const args = {
                pkgName: packageName,
                connectorName: connector.getName(),
            };
            connector.nodeFactoryMethod = DefaultBallerinaASTFactory.createConnectorDeclaration;
            connector.meta = args;
            // TODO : use a generic icon
            connector.icon = this.icons.connector;
            connector.title = connector.getName();
            connector.id = connector.getName();
            definitions.push(connector);

            const actionsOrdered = _.sortBy(connector.getActions(), [function (action) {
                return action.getName();
            }]);
            _.each(actionsOrdered, (action, index, collection) => {
                /* We need to add a special class to actions to indent them in tool palette. */
                action.setId(`${connector.getName()}-${action.getName()}`);
                action.classNames = 'tool-connector-action';
                if ((index + 1) === collection.length) {
                    action.classNames = 'tool-connector-action tool-connector-last-action';
                }
                action.meta = {
                    action: action.getName(),
                    actionConnectorName: connector.getName(),
                    actionPackageName: packageName,
                    fullPackageName: pckg.getName(),
                };
                action.icon = this.icons.action;

                action.title = action.getName();

                action.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0) {
                    action.nodeFactoryMethod =
                             DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement;
                }

                action.id = action.getId();
                definitions.push(action);
            });
        });

        _.each(functionsOrdered, (functionDef) => {
            if (functionDef.getName() === 'main') {
                // do not add main function to tool palette
                return;
            }

            const packageName = _.last(_.split(pckg.getName(), '.'));
            functionDef.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
            functionDef.meta = {
                functionDef,
                packageName,
                fullPackageName: pckg.getName(),
            };
            functionDef.icon = this.icons.function;

            functionDef.title = functionDef.getName();
            functionDef.id = functionDef.getName();
            definitions.push(functionDef);
        });

        const group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: `${pckg.getName()}-tool-group`,
            toolOrder: 'vertical',
            toolDefinitions: definitions,
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
        const tool = {};
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
        let packageNames = [];
        packageNames = packageNames.concat(_.map(this._defaultImportedPackages, '_name'));
        _.forEach(this._importedPackagesViews, (key) => {
            packageNames.push(key);
        });
        packageNames = _.sortBy(packageNames);
        return packageNames[_.sortedIndex(packageNames, newImportName)];
    }

    /**
     * Get the package to import, via environment
     * @param packageString
     */
    getPackageToImport(packageString) {
        return this._ballerinaFileEditor.getEnvironment().searchPackage(packageString);
    }

    /**
     * Create and return a ToolGroup object for a given package
     * @param pckg {Package} Package Object.
     */
    getCombinedToolGroup(pckg) {
        const definitions = [];

        // Sort the connector package by name
        const connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
            return connectorPackage.getName();
        }]);

        const functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
            return functionDef.getName();
        }]);

        _.each(connectorsOrdered, (connector) => {
            const packageName = _.last(_.split(pckg.getName(), '.'));
            const args = {
                pkgName: packageName,
                connectorName: connector.getName(),
            };
            connector.nodeFactoryMethod = DefaultBallerinaASTFactory.createConnectorDeclaration;
            connector.meta = args;
            // TODO : use a generic icon
            connector.icon = this.icons.connector;
            connector.title = connector.getName();
            connector.id = connector.getName();
            connector.cssClass = 'icon fw fw-connector';
            definitions.push(connector);

            const actionsOrdered = _.sortBy(connector.getActions(), [function (action) {
                return action.getName();
            }]);
            _.each(actionsOrdered, (action, index, collection) => {
                /* We need to add a special class to actions to indent them in tool palette. */
                action.setId(`${connector.getName()}-${action.getName()}`);
                action.classNames = 'tool-connector-action';
                if ((index + 1) === collection.length) {
                    action.classNames = 'tool-connector-action tool-connector-last-action';
                }
                action.meta = {
                    action: action.getName(),
                    actionConnectorName: connector.getName(),
                    actionPackageName: packageName,
                    fullPackageName: pckg.getName(),
                };
                action.icon = this.icons.action;

                action.title = action.getName();
                action.cssClass = 'icon fw fw-dgm-action';
                action.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0) {
                    action.nodeFactoryMethod = DefaultBallerinaASTFactory
                                                  .createAggregatedActionInvocationAssignmentStatement;
                }

                action.id = action.getId();
                definitions.push(action);
            });
        });

        _.each(functionsOrdered, (functionDef) => {
            if (functionDef.getName() === 'main') {
                // do not add main function to tool palette
                return;
            }

            const packageName = _.last(_.split(pckg.getName(), '.'));
            functionDef.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
            functionDef.meta = {
                functionDef,
                packageName,
                fullPackageName: pckg.getName(),
            };
            functionDef.icon = this.icons.function;

            functionDef.title = functionDef.getName();
            functionDef.id = functionDef.getName();
            functionDef.cssClass = 'icon fw fw-function';
            definitions.push(functionDef);
        });

        const group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: `${pckg.getName()}-tool-group`,
            toolOrder: 'vertical',
            toolDefinitions: definitions,
        });

        return group;
    }


    /**
     * Create and return a connector ToolGroup object for a given package
     * @param pckg {Package} Package Object.
     */
    getConnectorToolGroup(pckg) {
        const definitions = [];

        // Sort the connector package by name
        const connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
            return connectorPackage.getName();
        }]);

        _.each(connectorsOrdered, (connector) => {
            const packageName = _.last(_.split(pckg.getName(), '.'));
            const args = {
                pkgName: packageName,
                connectorName: connector.getName(),
                fullPackageName: pckg.getName(),
            };
            connector.nodeFactoryMethod = DefaultBallerinaASTFactory.createConnectorDeclaration;
            connector.meta = args;
            // TODO : use a generic icon
            connector.icon = this.icons.connector;
            connector.title = connector.getName();
            connector.id = connector.getName();
            connector.cssClass = 'icon fw fw-connector';
            definitions.push(connector);

            const actionsOrdered = _.sortBy(connector.getActions(), [function (action) {
                return action.getName();
            }]);
            _.each(actionsOrdered, (action, index, collection) => {
                /* We need to add a special class to actions to indent them in tool palette. */
                action.setId(`${connector.getName()}-${action.getName()}`);
                action.classNames = 'tool-connector-action';
                if ((index + 1) === collection.length) {
                    action.classNames = 'tool-connector-action tool-connector-last-action';
                }
                action.meta = {
                    action: action.getName(),
                    actionConnectorName: connector.getName(),
                    actionPackageName: packageName,
                    
                    fullPackageName: pckg.getName(),
                };
                action.icon = this.icons.action;
                action.cssClass = 'icon fw fw-dgm-action';
                action.title = action.getName();

                action.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement;
                if (action.getReturnParams().length > 0) {
                    action.nodeFactoryMethod = DefaultBallerinaASTFactory
                                                  .createAggregatedActionInvocationAssignmentStatement;
                }

                action.id = action.getId();
                definitions.push(action);
            });
        });

        const group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: `${pckg.getName()}-tool-group`,
            toolOrder: 'vertical',
            toolDefinitions: definitions,
        });

        return group;
    }



    /**
     * Create a tool group consisting only of functions of a package.
     *
     * @param {Package} pckg Package Object.
     * @returns {ToolGroup} returns tool group.
     *
     * @memberof ToolPaletteItemProvider
     */
    getLibraryToolGroup(pckg) {
        const definitions = [];

        const functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
            return functionDef.getName();
        }]);

        _.each(functionsOrdered, (functionDef) => {
            if (functionDef.getName() === 'main') {
                // do not add main function to tool palette
                return;
            }

            const packageName = _.last(_.split(pckg.getName(), '.'));
            functionDef.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
            functionDef.meta = {
                functionDef,
                packageName,
                fullPackageName: pckg.getName(),
            };
            functionDef.icon = this.icons.function;

            functionDef.title = functionDef.getName();
            functionDef.id = functionDef.getName();
            functionDef.cssClass = 'icon fw fw-function';
            definitions.push(functionDef);
        });

        const group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: `${pckg.getName()}-tool-group`,
            toolOrder: 'vertical',
            toolDefinitions: definitions,
        });

        return group;
    }
}

export default ToolPaletteItemProvider;
