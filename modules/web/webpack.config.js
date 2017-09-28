const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const UnusedFilesWebpackPlugin = require('unused-files-webpack-plugin').UnusedFilesWebpackPlugin;
const CircularDependencyPlugin = require('circular-dependency-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const WebfontPlugin = require('webpack-webfont').default;

const extractThemes = new ExtractTextPlugin('./[name].css');
const extractCSSBundle = new ExtractTextPlugin('./bundle.css');
let exportConfig = {};
const config = [{
    entry: {
        bundle: './src/index.js',
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
            test: /\.scss$/,
            exclude: /node_modules/,
            loader: 'style-loader!css-loader!sass-loader',
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
        new ProgressBarPlugin(),
        extractCSSBundle,
        // new UnusedFilesWebpackPlugin({
        //    pattern: 'js/**/*.*',
        //    globOptions: {
        //        ignore: 'js/tests/**/*.*',
        //    },
        // }),
        // https://github.com/fronteed/icheck/issues/322
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
        }),
        new webpack.WatchIgnorePlugin([path.resolve(__dirname, './font/dist/')]),
        new WebfontPlugin({
            files: path.resolve(__dirname, './font/font-ballerina/icons/**/*.svg'),
            css: true,
            cssTemplateFontPath: '../fonts/',
            fontName: 'font-ballerina',
            fontHeight: 1000,
            normalize: true,
            cssTemplateClassName: 'fw', // TODO: map with proper class name
            template: path.resolve(__dirname, './font/font-ballerina/template.css.njk'),
            dest: {
                fontsDir: path.resolve(__dirname, './font/dist/font-ballerina/fonts'),
                stylesDir: path.resolve(__dirname, './font/dist/font-ballerina/css'),
                outputFilename: 'font-ballerina.css',
            },
            hash: new Date().getTime(),
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
        contentBase: './public',
    },
    externals: {
        jsdom: 'window',
        'react-addons-test-utils': true,
        'react/addons': true,
        'react/lib/ExecutionEnvironment': true,
        'react/lib/ReactContext': true,
    },
    node: { module: 'empty', net: 'empty', fs: 'empty' },
    devtool: 'source-map',
    resolve: {
        extensions: ['.js', '.json', '.jsx'],
        modules: ['src', 'public/lib', 'font/dist', 'js', 'node_modules', path.resolve(__dirname)],
        alias: {
            // ///////////////////////
            // third party modules //
            // //////////////////////
            theme_wso2: 'theme-wso2-2.0.0/js/theme-wso2',
            // /////////////////////
            // custom modules ////
            // ////////////////////
            log: 'log/log',
            event_channel: 'event/channel',
            ace: 'ace-builds/src-noconflict',
            alerts: 'utils/alerts',
            expression_editor_utils: 'expression-editor/expression-editor-utils',
            images: 'public/images',
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
            keep_fnames: true,
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
        bundle: './src-electron/electron-index.js',
    };
}

module.exports = exportConfig;
