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
import $ from 'jquery';
import _ from 'lodash';
import 'css/preloader.css';
import Plugin from 'plugin/plugin';
import { ACTIVATION_POLICIES } from './../plugin/constants';

import commandManager from './commands/manager';
import LayoutManagerPlugin from './plugins/layout-manager/plugin';
import ApplicationMenuPlugin from './plugins/application-menu/plugin';

/**
 * Composer Application
 */
class Application {

    /**
     * @constructs
     * @class Application wraps all the application logic and it is the main starting point.
     * @param {Object} config configuration options for the application
     */
    constructor(config) {
        // !!! IMPORTANT !!!
        // Make config object read only.
        // This is to prevent any subsequent changes
        // to config object from plugins, etc.
        Object.freeze(config);
        Object.preventExtensions(config);

        // init plugins collection
        this.plugins = [];

        // init the application context
        this.appContext = {
            commandChannel: commandManager.commandChannelProxy,
        };

        // Initialize special core plugins
        this.plugins.push(new LayoutManagerPlugin());
        this.plugins.push(new ApplicationMenuPlugin());

        // load other plugins
        this.loadPlugins(_.get(config, 'app.plugins', []));

        // if any plugin specific configs are found
        // provide them to the plugin.
        const { pluginConfigs } = config;
        this.plugins.forEach((plugin) => {
            plugin.init(_.get(pluginConfigs, plugin.getID(), {}));

            // Register command definitions
            const commandDefs = plugin.getCommandDefinitions();
            commandDefs.forEach((command) => {
                commandManager.registerCommand(command);
            });

            // Register command handlers
            const commandHandlerDefs = plugin.getCommandHandlerDefinitions();
            commandHandlerDefs.forEach((commandHandlerDef) => {
                const { cmdID, handler, context } = commandHandlerDef;
                commandManager.registerHandler(cmdID, handler, context);
            });
        });
    }

    /**
     * Load other configured plugins.
     *
     * @param {[Plugin]} pluginsFromConfig Pre configured plugins from config.
     *                                     (Similar to webpack.config)
     */
    loadPlugins(pluginsFromConfig) {
        if (_.isArray(pluginsFromConfig)) {
            pluginsFromConfig.forEach((plugin) => {
                if (plugin instanceof Plugin) {
                    this.plugins.push(plugin);
                }
            });
        }
    }

    /**
     * Render Composer.
     *
     * @memberof Application
     */
    render() {
        // activate plugins which needs to be activated during startup
        // while providing the application context
        this.plugins.forEach((plugin) => {
            if (plugin.getActivationPolicy().type ===
                     ACTIVATION_POLICIES.APP_STARTUP) {
                plugin.activate(this.appContext);
            }
        });
        // Finished Activating all the plugins.
        // Now it's time to hide pre-loader.
        this.hidePreLoader();
    }

    /**
     * Hide the preloader. This will be called once the application rendering is completed.
     *
     * @memberof Application
     */
    hidePreLoader() {
        $('body')
            .loading('hide')
            .removeAttr('data-toggle')
            .removeAttr('data-loading-style');
    }
}

export default Application;
