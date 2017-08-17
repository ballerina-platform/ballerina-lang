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
import Plugin from './plugin/plugin';
import { ACTIVATION_POLICIES, CONTRIBUTIONS } from './plugin/constants';
import { CONTEXT_NAMESPACES } from './constants';

import CommandManager from './command/manager';
import LayoutManager from './layout/manager';
import MenuManager from './menu/manager';
import WorkspaceManager from './workspace/manager';
import { makeImutable } from './utils/object-utils';

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
        // Make config object read only.
        // This is to prevent any subsequent changes
        // to config object from plugins, etc.
        makeImutable(config);
        this.config = config;

        // init plugins collection
        this.plugins = [];

        // init the application context
        this.appContext = {
            pluginContexts: {
            },
        };

        // Initialize core plugins first
        this.commandManager = new CommandManager();
        this.layoutManager = new LayoutManager();
        this.menuManager = new MenuManager();
        this.workspaceManager = new WorkspaceManager();

        // Unless the necessities to keep direct references within app
        // & to be the very first set of plugins to be init/activate,
        // core plugins will be same as other plugins
        this.loadPlugin(this.commandManager);
        this.loadPlugin(this.layoutManager);
        this.loadPlugin(this.menuManager);
        this.loadPlugin(this.workspaceManager);

        // load plugins contributed via config
        this.loadOtherPlugins();

        // make contexts from core plugins
        // available via shorter namespaces
        this.appContext[CONTEXT_NAMESPACES.COMMAND] = this.appContext
                    .pluginContexts[this.commandManager.getID()];
        this.appContext[CONTEXT_NAMESPACES.LAYOUT] = this.appContext
                    .pluginContexts[this.layoutManager.getID()];
        this.appContext[CONTEXT_NAMESPACES.MENU] = this.appContext
                    .pluginContexts[this.menuManager.getID()];
        this.appContext[CONTEXT_NAMESPACES.WORKSPACE] = this.appContext
                    .pluginContexts[this.workspaceManager.getID()];

        // Since now we have loaded all the plugins
        // Make appContext object read only.
        // This is to prevent any subsequent changes
        // to it from plugins, etc.
        makeImutable(this.appContext);
    }

    /**
     * Load other configured plugins
     */
    loadOtherPlugins() {
        const { app: { plugins } } = this.config;
        if (_.isArray(plugins)) {
            plugins.forEach((plugin) => {
                this.loadPlugin(plugin);
            });
        }
    }

    /**
     * Load a plugin
     *
     * @param {Plugin} plugin plugin instance
     */
    loadPlugin(plugin) {
        if (!(plugin instanceof Plugin)) {
            throw new Error('Invalid prototype for a plugin.');
        }
        this.plugins.push(plugin);
        this._initPlugin(plugin);
        this._discoverContributions(plugin);
    }

    /**
     * Initialize plugin
     *
     * @param {Plugin} plugin plugin instance
     */
    _initPlugin(plugin) {
        const { pluginConfigs } = this.config;
        // if any plugin specific configs are found
        // provide them to the plugin.
        // (This is to let devs override plugin configs externally)
        const pluginContext = plugin.init(_.get(pluginConfigs, plugin.getID(), {}));
        // set public plugin context
        this.appContext.pluginContexts[plugin.getID()] = pluginContext;
    }

    /**
     * Discover contributions from plugins
     *
     * @param {Plugin} plugin plugin instance
     */
    _discoverContributions(plugin) {
        const contributions = plugin.getContributions();

        const commands = _.get(contributions, CONTRIBUTIONS.COMMANDS, []);
        const handlers = _.get(contributions, CONTRIBUTIONS.HANDLERS, []);
        const views = _.get(contributions, CONTRIBUTIONS.VIEWS, []);
        const menus = _.get(contributions, CONTRIBUTIONS.MENUS, []);

        commands.forEach((commandDef) => {
            this.commandManager.registerCommand(commandDef);
        });

        handlers.forEach((handlerDef) => {
            const { cmdID, handler, context } = handlerDef;
            this.commandManager.registerHandler(cmdID, handler, context);
        });

        views.forEach((viewDef) => {
            this.layoutManager.addViewToLayout(viewDef);
        });

        menus.forEach((menuDef) => {
            this.menuManager.addMenu(menuDef);
        });
    }

    /**
     * Render Composer.
     *
     * @memberof Application
     */
    render() {
        this.plugins.forEach((plugin) => {
            if (plugin.getActivationPolicy().type === ACTIVATION_POLICIES.APP_STARTUP) {
                plugin.activate(this.appContext);
            }
        });
        this.layoutManager.render();
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
