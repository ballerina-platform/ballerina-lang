const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const UnusedFilesWebpackPlugin = require('unused-files-webpack-plugin').UnusedFilesWebpackPlugin;
const CircularDependencyPlugin = require('circular-dependency-plugin');

const extractThemes = new ExtractTextPlugin('./[name].css');
const extractCSSBundle = new ExtractTextPlugin('./bundle.css');
let exportConfig = {};
const config = [{
    entry: {
        bundle: './index.js',
        'worker-ballerina': './js/ballerina/utils/ace-worker.js',
    },
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'dist'),
    },
    module: {
        rules: [{
            test: /\.js$/,
            exclude: /(node_modules|modules\/web\/lib\/scss)/,
            use: [
                {
                    loader: 'babel-loader',
                    query: {
                        presets: ['es2015', 'react'],
                    },
                },
            ],
        },
        {
            test: /\.html$/,
            use: [{
                loader: 'html-loader',
            }],
        },
        {
            test: /\.css$/,
            use: extractCSSBundle.extract({
                fallback: 'style-loader',
                use: [{
                    loader: 'css-loader',
                    options: {
                        sourceMap: true,
                    },
                }],
            }),
        },
        {
            test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
            use: ['url-loader'],
        },
        {
            test: /\.jsx$/,
            exclude: /(node_modules|modules\/web\/lib\/scss)/,
            use: [
                {
                    loader: 'babel-loader',
                    query: {
                        presets: ['es2015', 'react'],
                    },
                },
            ],
        },
        ],
    },
    plugins: [
        extractCSSBundle,
        new UnusedFilesWebpackPlugin({
            pattern: 'js/**/*.*',
            globOptions: {
                ignore: 'js/tests/**/*.*',
            },
        }),
        // https://github.com/fronteed/icheck/issues/322
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
        }),
        /*
        new CircularDependencyPlugin({
            exclude: /a\.css|node_modules/,
            failOnError: true,
        }),
        */
    ],
    devServer: {
        publicPath: '/dist/',
    },
    externals: {
        'jsdom': 'window',
        'react-addons-test-utils': true,
        'react/addons': true,
        'react/lib/ExecutionEnvironment': true,
        'react/lib/ReactContext': true,
    },
    node: { module: 'empty', net: 'empty', fs: 'empty' },
    devtool: 'source-map',
    resolve: {
        extensions: ['.js', '.json', '.jsx'],
        modules: [path.resolve('./lib'), path.resolve('./js'), path.resolve('./node_modules'), path.resolve(__dirname)],
        alias: {
            // ///////////////////////
            // third party modules //
            // //////////////////////
            svg_pan_zoom: 'svg-panNZoom/jquery.svg.pan.zoom',
            theme_wso2: 'theme-wso2-2.0.0/js/theme-wso2',
            mcustom_scroller: 'malihu-custom-scrollbar-plugin',
            respond: 'respond_1.4.2/respond.min',
            select2: 'select2-4.0.3/dist/js/select2.full.min',
            underscore: 'lodash',
            ace: 'ace-builds/src-noconflict',
            // dagre uses an older version of lodash which conflicts
            // with the lodash version used by the composer.
            // hence dagre is added to libs
            // https://github.com/cpettitt/graphlib/issues/58
            dagre: 'dagre-0.7.4/dagre.min.js',

            // /////////////////////
            // custom modules ////
            // ////////////////////
            log: 'log/log',
            d3utils: 'utils/d3-utils',
            diagram_core: 'diagram-core/module',
            command: 'command/command',
            event_channel: 'event/channel',
            drag_drop_manager: 'drag-drop/manager',
            main_elements: 'main-elements/module',
            processors: 'processors/module',
            file_browser: 'file-browser/file-browser',
            menu_bar: 'menu-bar/menu-bar',
            context_menu: 'context-menu/context-menu',
            tool_bar: 'tool-bar/tool-bar',
            alerts: 'utils/alerts',
            breadcrumbs: 'breadcrumbs/breadcrumbs',
            property_pane_utils: 'property-pane/property-pane-utils',
            expression_editor_utils: 'expression-editor/expression-editor-utils',
            constants: 'constants/constants',
            typeMapper: 'type-mapper/type-mapper-renderer',
            environment_content: 'ballerina/env/environment-content',
            bal_utils: 'ballerina/utils',
            bal_configs: 'ballerina/configs',
            console: 'launcher/console',
            workspace$: 'workspace/module',
            ballerina$: 'ballerina/module',
            'welcome-page$': 'welcome-page/module',
        },
    },

}, {
    entry: {
        default: './scss/themes/default.scss',
        light: './scss/themes/light.scss',
        dark: './scss/themes/dark.scss',
    },
    output: {
        filename: '[name].css',
        path: path.resolve(__dirname, 'dist/themes/'),
    },
    module: {
        rules: [
            {
                test: /\.scss$/,
                use: extractThemes.extract({
                    fallback: 'style-loader',
                    use: [{
                        loader: 'css-loader',
                        options: {
                            sourceMap: true,
                        },
                    }, {
                        loader: 'sass-loader',
                        options: {
                            sourceMap: true,
                        },
                    }],
                }),
            },
        ],
    },
    plugins: [
        extractThemes,
    ],
    devtool: 'source-map',
}];
exportConfig = config;
if (process.env.NODE_ENV === 'production') {
    config[0].plugins.push(new webpack.DefinePlugin({
        PRODUCTION: JSON.stringify(true),

      // React does some optimizations to it if NODE_ENV is set to 'production'
        'process.env': {
            NODE_ENV: JSON.stringify('production'),
        },
    }));

  // Add UglifyJsPlugin only when we build for production.
  // uglyfying slows down webpack build so we avoid in when in development
    config[0].plugins.push(new webpack.optimize.UglifyJsPlugin({
        sourceMap: true,
        mangle: {
            keep_fnames: true
        },
    }));
} else {
    config[0].plugins.push(new webpack.DefinePlugin({
        PRODUCTION: JSON.stringify(false),
    }));
}

if (process.env.NODE_ENV === 'test') {
  // we run tests on nodejs. So compile for nodejs
    config[0].target = 'node';
    exportConfig = config[0];
}

if (process.env.NODE_ENV === 'electron-dev' || process.env.NODE_ENV === 'electron') {
  // we run tests on nodejs. So compile for nodejs
    config[0].target = 'electron-renderer';

  // reassign entry so it uses the entry point for the electron app
    config[0].entry = {
        bundle: './electron-index.js',
    };
}

module.exports = exportConfig;
