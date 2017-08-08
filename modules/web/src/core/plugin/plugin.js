import PropTypes from 'prop-types';

import { ACTIVATION_POLICIES } from './constants';

/**
 * Base class for the plugins
 */
class Plugin {

    /**
     * Method to get the unique ID of the plugin.
     * This unique ID will be useful when providing configs for
     * the plugin & in future for handling preferences specific to
     * this plugin.
     *
     * @returns {String} A unique ID for the plugin
     */
    getID() {
        return 'composer.plugin.generic';
    }

    /**
     * Method to get the activation policy of the plugin.
     * By default the plugin will be activated during app startup.
     *
     * @returns {Object} Activation Policy
     */
    getActivationPolicy() {
        return {
            type: ACTIVATION_POLICIES.APP_STARTUP,
            args: {
            },
        };
    }

    /**
     * This is the starting point of any given plugin.
     *
     * @param {Object} config Specic configurations for the plugin
     *                        This will be derived using unique plugin ID
     *                        @see Plugin#getID
     */
    init(config) {
        // validate configs using config types
        PropTypes.checkPropTypes(this.constructor.configTypes,
                config, 'config', this.constructor.name);
        this.config = config;
    }

    /**
     * Plugin Activate Hook.
     * This method will be called when the app is finished
     * initializing all the plugins.
     *
     * @param {Object} appContext Application Context
     * @param {CommandChannel} appContext.commandChannel Command Channel
     */
    activate(appContext) {
        this.appContext = appContext;
    }

    /**
     * Plugin Deactivate Hook.
     * This method will be called when the plugin is unloaded
     */
    deactivate() {
    }

    /**
     * Returns the list of command definitions
     * that this particular plugin is contributing.
     *
     * @return {Object[]} Command definitions
     */
    getCommandDefinitions() {
        return [];
    }

    /**
     * Returns the list of command handlers
     * that this particular plugin is contributing.
     *
     * @return {Object[]} Command Handler Definitionns
     */
    getCommandHandlerDefinitions() {
        return [];
    }

    /**
     * Returns the list of menu definitions
     * that this particular plugin is contributing.
     *
     * @return {Object[]} Menu definitions
     */
    getMenuDefinitions() {
        return [];
    }

    /**
     * Returns the list of view definitions
     * that this particular plugin is contributing.
     *
     * @return {Object[]} View definitions
     */
    getViews() {
        return [];
    }
}

Plugin.configTypes = {
};

export default Plugin;
