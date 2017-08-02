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
        return 'composer.plugins.generic';
    }

    /**
     * This is the starting point of any given plugin.
     *
     * @param {Object} config Specic configurations for the plugin
     *                        This will be derived using unique plugin ID
     *                        @see Plugin#getID
     */
    init(config) {
        this.config = config;
    }

    /**
     * Plugin Activate Hook.
     * This method will be called when the app is finished
     * initializing all the plugins.
     *
     * @param {Object} appContext Application Context
     * @param {EventChannel} appContext.commandChannel Command channel instance
     */
    activate(appContext) {
        this.appContext = appContext;
    }

    /**
     * Returns the list of command definitions
     * that this particular plugin is contributing.
     *
     * @return {[Object]} Command definitions
     */
    getCommands() {
        return [];
    }
}

export default Plugin;
