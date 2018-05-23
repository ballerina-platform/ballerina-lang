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

import _ from 'lodash';
import Plugin from './plugin/plugin';
import { ACTIVATION_POLICIES, CONTRIBUTIONS, EVENTS } from './plugin/constants';
import { CONTEXT_NAMESPACES } from './constants';

import AlertPlugin from './alert/plugin';
import CommandPlugin from './command/plugin';
import LayoutPlugin from './layout/plugin';
import { COMMANDS as LAYOUT_COMMANDS } from './layout/constants';
import MenuPlugin from './menu/plugin';
import ToolBarPlugin from './toolbar/plugin';
import WorkspacePlugin from './workspace/plugin';
import EditorPlugin from './editor/plugin';
import PreferencesPlugin from './preferences/plugin';
import { makeImutable } from './utils/object-utils';
import { isOnElectron } from './utils/client-info';

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
            services: config.services,
            // TODO avoid hardcoding, provide a config contributing mechanism from BE
            balHome: config.balHome,
            debuggerPath: config.debuggerPath,
        };

        // Initialize core plugins first
        this.commandPlugin = new CommandPlugin();
        this.layoutPlugin = new LayoutPlugin();
        this.menuPlugin = new MenuPlugin();
        this.toolBarPlugin = new ToolBarPlugin();
        this.workspacePlugin = new WorkspacePlugin();
        this.editorPlugin = new EditorPlugin();
        this.preferencesPlugin = new PreferencesPlugin();
        this.alertPlugin = new AlertPlugin();

        // Unless the necessities to keep direct references within app
        // & to be the very first set of plugins to be init/activate,
        // core plugins will be same as other plugins
        this.loadPlugin(this.commandPlugin);
        this.loadPlugin(this.layoutPlugin);
        this.loadPlugin(this.menuPlugin);
        this.loadPlugin(this.toolBarPlugin);
        this.loadPlugin(this.workspacePlugin);
        this.loadPlugin(this.editorPlugin);
        this.loadPlugin(this.preferencesPlugin);
        this.loadPlugin(this.alertPlugin);

        // load plugins contributed via config
        this.loadOtherPlugins();

        // make contexts from core plugins
        // available via shorter namespaces
        this.appContext[CONTEXT_NAMESPACES.COMMAND] = this.appContext
                    .pluginContexts[this.commandPlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.LAYOUT] = this.appContext
                    .pluginContexts[this.layoutPlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.MENU] = this.appContext
                    .pluginContexts[this.menuPlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.WORKSPACE] = this.appContext
                    .pluginContexts[this.workspacePlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.EDITOR] = this.appContext
                    .pluginContexts[this.editorPlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.PREFERENCES] = this.appContext
                    .pluginContexts[this.preferencesPlugin.getID()];
        this.appContext[CONTEXT_NAMESPACES.ALERT] = this.appContext
                    .pluginContexts[this.alertPlugin.getID()];

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
            plugins.forEach((ExternalPlugin) => {
                this.loadPlugin(new ExternalPlugin());
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
        const pluginID = plugin.getID();
        const contributions = plugin.getContributions();

        const commands = _.get(contributions, CONTRIBUTIONS.COMMANDS, []);
        const handlers = _.get(contributions, CONTRIBUTIONS.HANDLERS, []);
        const views = _.get(contributions, CONTRIBUTIONS.VIEWS, []);
        const dialogs = _.get(contributions, CONTRIBUTIONS.DIALOGS, []);
        const menus = _.get(contributions, CONTRIBUTIONS.MENUS, []);
        const tools = _.get(contributions, CONTRIBUTIONS.TOOLS, []);
        const editors = _.get(contributions, CONTRIBUTIONS.EDITORS, []);

        commands.forEach((commandDef) => {
            this.commandPlugin.registerCommand(commandDef);
        });

        handlers.forEach((handlerDef) => {
            const { cmdID, handler, context } = handlerDef;
            this.commandPlugin.registerHandler(cmdID, handler, context);
        });

        views.forEach((viewDef) => {
            viewDef.pluginID = pluginID;
            this.layoutPlugin.addViewToLayout(viewDef);
        });

        dialogs.forEach((dialogDef) => {
            this.layoutPlugin.registerDialog(dialogDef);
        });

        menus.forEach((menuDef) => {
            this.menuPlugin.addMenu(menuDef);
        });

        tools.forEach((toolDef) => {
            this.toolBarPlugin.addTool(toolDef);
        });

        editors.forEach((editorDef) => {
            this.editorPlugin.registerEditor(editorDef);
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
            plugin.on(EVENTS.RE_RENDER, () => {
                this.commandPlugin.dispatch(LAYOUT_COMMANDS.RE_RENDER_PLUGIN, { id: plugin.getID() });
            });
        });
        this.layoutPlugin.render();
        // Finished Activating all the plugins.
        // Now it's time to hide pre-loader.
        this.hidePreLoader();
        this.plugins.forEach((plugin) => {
            plugin.onAfterInitialRender();
        });
        if (isOnElectron()) {
            setTimeout(() => {
                const { ipcRenderer } = require('electron');
                ipcRenderer.send('composer-rendered');
            }, 200);
        }
    }

    /**
     * Hide the preloader. This will be called once the application rendering is completed.
     *
     * @memberof Application
     */
    hidePreLoader() {
        const body = document.getElementsByTagName('body')[0];
        body.classList.remove('loading');
        document.getElementsByClassName('loading-animation')[0].remove();
        document.getElementsByClassName('loading-bg')[0].remove();
        body.removeAttribute('data-toggle');
        body.removeAttribute('data-loading-style');
    }
}

export default Application;
