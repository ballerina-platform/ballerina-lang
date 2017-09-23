import PropTypes from 'prop-types';
import EventChannel from 'event_channel';

import { ACTIVATION_POLICIES, EVENTS } from './constants';

/**
 * Base class for the plugins
 * @extends {EventChannel}
 */
class Plugin extends EventChannel {

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
     * If there are overridden configs provided for the plugin,
     * those will be provided here.
     *
     * @param {Object} config Specic configurations for the plugin
     *                        This will be derived using unique plugin ID
     *                        @see Plugin#getID
     * @returns {Object} pluginContext The API which is accessible to public
     *
     */
    init(config) {
        // validate configs using config types
        PropTypes.checkPropTypes(this.constructor.configTypes,
                config, 'config', this.constructor.name);
        this.config = config;
        return {};
    }

    /**
     * Plugin Activate Hook.
     * This method will be called when the app is finished
     * initializing all the plugins and appContext is filled with all
     * public APIs available from plugins. However, app is still not rendered.
     *
     * @param {Object} appContext Application Context
     * @param {CommandChannel} appContext.commandChannel Command Channel
     */
    activate(appContext) {
        this.appContext = appContext;
    }

    /**
     * This hook will be invoked once the app is finished doing initial rendering.
     * Here, you know for sure that dom is rendered and you can use UI dependant APIs.
     */
    onAfterInitialRender() {

    }

    /**
     * Plugin Deactivate Hook.
     * This method will be called when the plugin is unloaded
     */
    deactivate() {
    }

    /**
     * Provides the contributions from this plugin.
     * IMPORTANT: This method will be called before the plugin is activated.
     *
     * @return {Object} Contributions
     *
     */
    getContributions() {
        return {};
    }

    /**
     * Invoking re-render will make all views contributed via this plugin re-render.
     */
    reRender() {
        this.trigger(EVENTS.RE_RENDER);
    }
}

Plugin.configTypes = {
};

export default Plugin;
