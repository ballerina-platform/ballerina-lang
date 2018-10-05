/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

 /* eslint-disable */
const path = require('path');
const fs = require('fs');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const UnusedFilesWebpackPlugin = require('unused-files-webpack-plugin').UnusedFilesWebpackPlugin;
const CircularDependencyPlugin = require('circular-dependency-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const WebfontPlugin = require('webpack-webfont').default;
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const WriteFilePlugin = require('write-file-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

const { env: { NODE_ENV }} = process;
const isElectronBuild = NODE_ENV === 'electron' || NODE_ENV === 'electron-dev';
const isProductionBuild = NODE_ENV === 'production' || NODE_ENV === 'electron';
const target = isElectronBuild ? 'electron-renderer' : 'web';
const outputPath = isElectronBuild && isProductionBuild ? path.resolve(__dirname, 'dist-electron')
                        : path.resolve(__dirname, 'dist');

const extractThemes = new ExtractTextPlugin({ filename: './[name].css', allChunks: true });
const extractCSSBundle = new ExtractTextPlugin({ filename: './bundle-[name]-[hash].css', allChunks: true });
const extractLessBundle = new ExtractTextPlugin({ filename: './[name]-less-[hash].css', allChunks: true});
let exportConfig = {};

// Keeps unicode codepoints of font-ballerina for each icon name
const codepoints = {}

const config = [{
    target,
    entry: {
        tree: './src/plugins/ballerina/model/tree-builder.js',
        bundle: './src/index.js',
    },
    output: {
        filename: '[name]-[hash].js',
        path: outputPath,
    },
    module: {
        rules: [
        {
            test: require.resolve('monaco-editor/esm/vs/editor/common/services/editorSimpleWorker'),
            use: [
                {
                    loader: 'babel-loader',
                    options: {
                        plugins: [
                            replaceSelfRequireWithGlobalRequire()
                        ]
                    }
                }
            ],
        },    
        {
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
                        sourceMap: !isProductionBuild,
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
        {
            test: /\.less$/,
            exclude: /node_modules/,
            use: extractLessBundle.extract({
                fallback: 'style-loader',
                use: [{
                    loader: 'css-loader', 
                    options: {
                        hmr: !isProductionBuild,
                        sourceMap: !isProductionBuild,
                    },
                }, {
                    loader: 'less-loader',
                    options: {
                        hmr: !isProductionBuild,
                        sourceMap: !isProductionBuild,
                    },
                }],
            }),
        },
        ],
    },
    plugins: [
        new ProgressBarPlugin(),
        new CleanWebpackPlugin([outputPath], {watch: true, exclude:['themes', 'workers']}),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'tree',
            chunks: ['bundle', 'tree'],
            minChunks: Infinity,
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            chunks: ['bundle', 'tree'],
            minChunks(module) {
                const context = module.context;
                return context && context.indexOf('node_modules') >= 0;
            },
        }),
        extractLessBundle,
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
            cssTemplateFontPath: '../fonts/',
            fontName: 'font-ballerina',
            fontHeight: 1000,
            normalize: true,
            cssTemplateClassName: 'fw', // TODO: map with proper class name
            template: path.resolve(__dirname, './font/font-ballerina/template.css.njk'),
            glyphTransformFn: (obj) => {
                codepoints[obj.name] = obj.unicode;
            },
            dest: {
                fontsDir: path.resolve(__dirname, './font/dist/font-ballerina/fonts'),
                stylesDir: path.resolve(__dirname, './font/dist/font-ballerina/css'),
                outputFilename: 'font-ballerina.css',
            },
            hash: new Date().getTime(),
        }), {
            apply: function(compiler) {
                compiler.plugin('compile', function (compilation, callback = () => { }) {
                    fs.writeFile(
                        path.resolve(__dirname, './font/dist/font-ballerina/codepoints.json'),
                        JSON.stringify(codepoints),
                        'utf8',
                        callback
                    );
                });
            }
        },
        new WriteFilePlugin(),
        new CopyWebpackPlugin([
            {
                from: 'public',
            },
        ]),
        new HtmlWebpackPlugin({
            template: 'src/index.ejs',
            inject: false,
        }),
        new webpack.ExtendedAPIPlugin(),
        /*
        new CircularDependencyPlugin({
            exclude: /a\.css|node_modules/,
            failOnError: true,
        }),
        */
    ],
    devServer: {
        contentBase: path.join(__dirname, "dist"),
    },
    externals: {
        jsdom: 'window',
        'react-addons-test-utils': true,
        'react/addons': true,
        'react/lib/ExecutionEnvironment': true,
        'react/lib/ReactContext': true,
    },
    devtool: 'source-map',
    resolve: {
        extensions: ['.js', '.json', '.jsx'],
        modules: ['src', 'public/lib', 'font/dist', 'node_modules', path.resolve(__dirname)],
        alias: {
            // ///////////////////////
            // third party modules //
            // //////////////////////
            theme_wso2: 'theme-wso2-2.0.0/js/theme-wso2',
            // /////////////////////
            // custom modules ////
            // ////////////////////
            log: 'core/log/log',
            event_channel: 'core/event/channel',
            plugins: 'plugins',
            images: 'public/images',
            '../../theme.config$': path.join(__dirname, 'src/ballerina-theme/theme.config')
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
        path: path.resolve(outputPath, 'themes'),
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
                            sourceMap: !isProductionBuild,
                        },
                    }, {
                        loader: 'sass-loader',
                        options: {
                            sourceMap: !isProductionBuild,
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
}, 
{
    name: 'monaco-workers',
    target: 'webworker',
    entry: {
        'editor': 'monaco-editor/esm/vs/editor/editor.worker.js',
        'json': 'monaco-editor/esm/vs/language/json/json.worker.js',
        'css': 'monaco-editor/esm/vs/language/css/css.worker.js',
        'html': 'monaco-editor/esm/vs/language/html/html.worker.js',
        'ts': 'monaco-editor/esm/vs/language/typescript/ts.worker.js'
    },
    output: {
        filename: '[name].worker.bundle.js',
        path: path.resolve(outputPath, 'workers'),
    },
    module: {
		rules: [
			{
				test: /\.css$/,
				use: [ 'style-loader', 'css-loader' ]
			},
			{
				test: require.resolve('monaco-editor/esm/vs/editor/common/services/editorSimpleWorker'),
				use: [
					{
						loader: 'babel-loader',
						options: {
							plugins: [
								replaceSelfRequireWithGlobalRequire()
							]
						}
					}
				],
            },
            {
                test: /\.js$/,
                use: [
                    {
                        loader: 'babel-loader',
                        query: {
                            presets: ['es2015', 'react'],
                        },
                    },
                ],
            },
		]
	},
	node: {
		fs: 'empty',
		module: 'empty',
		net: 'empty'
	},
	plugins: [
		new webpack.IgnorePlugin(/^((fs)|(path)|(os)|(crypto)|(source-map-support))$/, /vs\/language\/typescript\/lib/),
		new webpack.ContextReplacementPlugin(
			new RegExp('^' + path.dirname(require.resolve('monaco-editor/esm/vs/editor/common/services/editorSimpleWorker')) + '$'),
			'',
			{
				'vs/language/css/cssWorker': require.resolve('monaco-editor/esm/vs/language/css/cssWorker'),
				'vs/language/html/htmlWorker': require.resolve('monaco-editor/esm/vs/language/html/htmlWorker'),
				'vs/language/json/jsonWorker': require.resolve('monaco-editor/esm/vs/language/json/jsonWorker'),
				'vs/language/typescript/tsWorker': require.resolve('monaco-editor/esm/vs/language/typescript/tsWorker')
			}
		)
	],
}];
exportConfig = config;
if (target === 'web') {
    config[0].node = { module: 'empty', net: 'empty', fs: 'empty' };
    config[0].plugins.push(new webpack.IgnorePlugin(/electron/));
}
if (isProductionBuild) {
    config[0].plugins.push(new webpack.DefinePlugin({
        PRODUCTION: JSON.stringify(true),

        // React does some optimizations to it if NODE_ENV is set to 'production'
        'process.env': {
            NODE_ENV: JSON.stringify(NODE_ENV),
        },
    }));

    // Add UglifyJsPlugin only when we build for production.
    // uglyfying slows down webpack build so we avoid in when in development
   config[0].plugins.push(new UglifyJsPlugin({
       sourceMap: !isProductionBuild,
       parallel: true,
       uglifyOptions: {
           mangle: {
               keep_fnames: true,
           },
       }
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
} else if (process.env.NODE_ENV === 'test-source-gen-dev') {
    const testConfig = config[0];
    testConfig.target = 'node';
    testConfig.entry = './src/plugins/ballerina/tests/js/spec/ballerina-test.js';
    testConfig.output = {
        path: path.resolve(__dirname, 'target'),
        filename: 'ballerina-test.js',
    };
    testConfig.plugins = [
        new webpack.DefinePlugin({
            PRODUCTION: JSON.stringify(false),
        }),
    ];
    exportConfig = testConfig;
}

function replaceSelfRequireWithGlobalRequire() {
	return (babel) => {
		const { types: t } = babel;
		return {
			visitor: {
				CallExpression(path) {
					const { node } = path;
					const isSelfRequireExpression = (
						t.isMemberExpression(node.callee)
						&& t.isIdentifier(node.callee.object, { name: 'self' })
						&& t.isIdentifier(node.callee.property, { name: 'require' })
					);
					if (!isSelfRequireExpression) { return; }
					path.get('callee').replaceWith(t.identifier('require'));
				}
			}
		};
	};
}

/* eslint-enable */

module.exports = exportConfig;
