/**
 * Base class for the plugins
 */
class Plugin {

    /**
     * 
     * @param {Object} args 
     * @param args.commandChannel - {CommandChannel} 
     * 
     */
    init(args) {
    }

    activate() {
    }

    /**
     * Returns the list of command definitions
     * @return {[Object]}
     *  
     */
    getCommands() {
        return [];
    }
}

export default Plugin;