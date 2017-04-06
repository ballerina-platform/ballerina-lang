var path = require('path');
var webpack = require("webpack");

var config = {
    entry: {
      bundle: './index.js',
      'worker-ballerina': './js/ballerina/utils/ace-worker.js'
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'dist')
    },
    module: {
        rules: [{
          test: /\.js$/,
          exclude: /(node_modules|modules\/web\/lib)/,
          use: [
            {
              loader: 'babel-loader',
              query: {
                  presets: ['es2015']
              }
            }
          ]
        },
        {
            test: /\.html$/,
            use: [ {
                loader: 'html-loader'
            }]
        },
        {
            test: /\.css$/,
            use: [ 'style-loader', 'css-loader' ]
        },
        {
            test: /\.(png|jpg|svg|cur|gif)$/,
            use: [ 'url-loader' ]
        }
      ]
    },
    plugins: [],
    devServer: {
      publicPath: '/dist/'
    },
    node: { module: "empty", net: "empty", fs: "empty" },
    devtool: 'source-map',
    resolve: {
        modules: [path.resolve('./lib'), path.resolve('./js'), path.resolve('./node_modules'), path.resolve(__dirname)],
        alias: {
            /////////////////////////
            // third party modules //
            ////////////////////////
            svg_pan_zoom: "svg-panNZoom/jquery.svg.pan.zoom",
            theme_wso2: "theme-wso2-2.0.0/js/theme-wso2",
            mcustom_scroller: "malihu-custom-scrollbar-plugin",
            respond: "respond_1.4.2/respond.min",
            select2: "select2-4.0.3/dist/js/select2.full.min",
            underscore: "lodash",
            ace: "ace-builds/src-noconflict",
            // dagre uses an older version of lodash which conflicts
            // with the lodash version used by the composer.
            // hence dagre is added to libs
            // https://github.com/cpettitt/graphlib/issues/58
            dagre : "dagre-0.7.4/dagre.min.js",
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
            workspace$: "workspace/module",
            ballerina$: "ballerina/module",
            "welcome-page$": "welcome-page/module",
            jstree : "js-tree-v3.3.2/jstree.js",
        }
    }

};

if (process.env.NODE_ENV === 'production') {
  // Add UglifyJsPlugin only when we build for production.
  // uglyfying slows down webpack build so we avoid in when in development
  config.plugins.push(new webpack.optimize.UglifyJsPlugin({
    sourceMap: true
  }));
}

if (process.env.NODE_ENV === 'test') {
  // we run tests on nodejs. So compile for nodejs
  config.target = 'node';
}

module.exports = config;
