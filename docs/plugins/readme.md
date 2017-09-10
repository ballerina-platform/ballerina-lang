# Writing plugins for Ballerina Composer

A plugin should extend from [Plugin](https://github.com/ballerinalang/composer/blob/composer-plugins-dev/modules/web/src/core/plugin/plugin.js) class. Each plugin should provide a unique ID via `getID()` method. A plugin can contribute to various [extension points available](#available-contribution-points) in composer by
providing contributions via `getContributions()` method. Main components of Composer are also developed as plugins. However, they are treated differently while initializing, compared to other plugins. Here is an example for a plugin class [WorkspacePlugin](https://github.com/ballerinalang/composer/blob/composer-plugins-dev/modules/web/src/core/workspace/plugin.js). 

# Plugin Life-Cycle

1. init - `init(config)`
2. activate - `activate(appContext)`
3. onAfterInitialRender
4. deactivate

# Available contribution points

- commands
- handlers
- menus
- dialogs
- views
- editors

# App Context - Core plugins and their APIs

- command
- layout
- workspace
- menu
- editor
- preferences


