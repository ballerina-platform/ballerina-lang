var path = require('path');
var webpack = require("webpack");

module.exports = {
    entry: './modules/web/js/main.js',
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist')
    },
    module: {
        rules: [
            //'{test: /\.(js|jsx)$/, use: 'babel-loader'}
        ]
    },
    plugins: [
        new webpack.optimize.UglifyJsPlugin()
    ],

    resolve: {
        modules: ['./modules/web/lib', './modules/web/js', 'node_modules'],
        alias: {
            /////////////////////////
            // third party modules from npm//
            ////////////////////////
            jquery: "jquery/jquery-1.9.1.min",
            jquery_ui: "jquery-ui/jquery-ui",
            bootstrap: "bootstrap/dist/js/bootstrap",
            d3: "d3_v4.1.1/d3",
            dagre: "dagre-0.7.4/dagre.min",
            log4javascript: "log4javascript-1.4.13/log4javascript",
            lodash: "lodash_v4.13.1/lodash",
            backbone: "backbone_v1.3.3/backbone",
            mousetrap: "mousetrap_v1.6.0/mousetrap.min",
            ace: "ace_v1.2.6",
            /////////////////////////
            // third party modules //
            ////////////////////////
            jquery: "jquery_v1.9.1/jquery-1.9.1.min",
            jquery_ui: "jquery-ui_v1.12.1.custom/jquery-ui",
            bootstrap: "bootstrap_v3.3.6/js/bootstrap",
            d3: "d3_v4.1.1/d3",
            dagre: "dagre-0.7.4/dagre.min",
            log4javascript: "log4javascript-1.4.13/log4javascript",
            lodash: "lodash_v4.13.1/lodash",
            backbone: "backbone_v1.3.3/backbone",
            mousetrap: "mousetrap_v1.6.0/mousetrap.min",
            ace: "ace_v1.2.6",
            svg_pan_zoom: "svg-panNZoom/jquery.svg.pan.zoom",
            js_tree: "js-tree-v3.3.2/jstree",
            theme_wso2: "theme-wso2-2.0.0/js/theme-wso2",
            beautify: "beautify/beautify",
            mcustom_scroller: "mCustomScrollbar_v3.1.5/js/jquery.mCustomScrollbar",
            jquery_mousewheel: "jquery-mousewheel_v3.1.13/jquery.mousewheel",
            jquery_context_menu: "context-menu_v2.4.2/jquery.contextMenu.min",
            html5_shiv: "html5shiv_3.7.2/html5shiv.min",
            respond: "respond_1.4.2/respond.min",
            jsPlumb: "jsPlumb-2.2.8/jsPlumb-2.2.8-min",
            yaml: "js-yaml-v3.7.0/dist/js-yaml.min",
            typeahead: "typeahead_v0.11.1/typeahead.bundle.min",
            select2: "select2-4.0.3/dist/js/select2.full.min",
            underscore: "lodash_v4.13.1/lodash",
            ///////////////////////
            // custom modules ////
            //////////////////////
            log: "log/log",
            d3utils: "utils/d3-utils",
            diagram_core: "diagram-core/module",
            command: "command/command",
            event_channel: "event/channel",
            drag_drop_manager: "drag-drop/manager",
            main_elements: "main-elements/module",
            processors: "processors/module",
            file_browser: "file-browser/file-browser",
            menu_bar: "menu-bar/menu-bar",
            context_menu: "context-menu/context-menu",
            tool_bar: "tool-bar/tool-bar",
            alerts: "utils/alerts",
            breadcrumbs: "breadcrumbs/breadcrumbs",
            property_pane_utils: "property-pane/property-pane-utils",
            expression_editor_utils: "expression-editor/expression-editor-utils",
            constants: 'constants/constants',
            typeMapper: 'type-mapper/type-mapper-renderer',
            environment_content: "ballerina/env/environment-content",
            bal_utils: "ballerina/utils",
            bal_configs: "ballerina/configs",
            console: "launcher/console",
            workspace: "workspace/module"
        }
    }

};