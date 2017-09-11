# Writing plugins for Ballerina Composer

A plugin should extend from [Plugin](./../../modules/web/src/core/plugin/plugin.js) class. Each plugin should provide a unique ID via `getID()` method. A plugin can contribute to various [extension points available](#available-extension-points) in composer by
providing contributions via `getContributions()` method.

Main components of Composer are also developed as plugins. However, comapared to other plugins, they are treated differently while initializing. They are the first set of plugins to be initialized and their startup order is enforced by Composer.

A plugin will be able to expose their APIs to other plugins via pluginContext. This is the object which should be returned in [init](#plugin-init) method - [here](./../../modules/web/src/core/workspace/plugin.js#L143) is an example of exposing public api.

A plugin can access public APIs of other plugins via appContext object which is passed to [activate](#plugin-activate) method. By default, upon plugin activate, appContext will be assigined to this.appContext of plugin class.

To access the API of another plugin, use appContext.pluginContext[pluginID]. To make life easy, we have exposed [APIs of core plugins](#core-plugins-and-their-apis) via shorter namespaces in appContext object (such as appContext.command, appContext.pref, appContext.workspace, etc.)

[Here](./../../modules/web/src/core/workspace/plugin.js) is an example for a plugin.

## Plugin Life-Cycle

1. init - `init(config)`
2. activate - `activate(appContext)`
3. onAfterInitialRender
4. deactivate - Not

### plugin init
### plugin activate

## Available extension points

> Please note that, as of now, these extension points only allow contributing to higher level components of the Composer. In future we are planning to improve ballerina-plugin to accept contributions from other plugins. For example, contributing to ballerina tool pallete will become possible after making plugins capable of defining their own extension points. As a first step, we have made basic components of the Composer pluggable and we will keep on improving it to allow more flexibility. 

- [commands](#command)
- [handlers](#handlers)
- [menus](#menus)
- [dialogs](#dialogs)
- [views](#views)
- [editors](#editors)

### command
### handlers
### menus
### dialogs
### views
### editors

## Core plugins and their APIs

- command
- layout
- workspace
- menu
- editor
- preferences


