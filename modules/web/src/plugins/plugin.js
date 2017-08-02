/**
 * Base class for the plugins
 */
class Plugin {

    /**
     * This is the starting point of any given plugin.
     *
     * @param {Object} args Arguments for the plugin
     * @param {EventChannel} args.commandChannel  Command channel instance
     */
    init(args) {
        this.commandChannel = args.commandChannel;
    }

    /**
     * Plugin Activate Hook.
     * This method will be called when app is finished
     * initializing all the plugins.
     */
    activate() {
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
