const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const ExtractDefaultThemeCSS = new ExtractTextPlugin({
    filename: (getPath) => {
        return getPath('themes/ballerina-default.css').replace('themes/js', 'css');
    },
    allChunks: true
});

module.exports = {
    entry: {
        composer: './src/index.ts',
        apiEditor: './src/api-editor.ts',
    },
    output: {
        path: path.resolve(__dirname, 'build'),
        filename: '[name].js',
        library: 'ballerinaComposer',
        libraryTarget: 'umd'
    },
    resolve: {
        alias: {
            '../../theme.config$': path.join(
                __dirname, 'node_modules/@ballerina/theme/src/themes/default/theme.config')
        },
        extensions: ['.tsx', '.ts', '.js', '.json']
    },
    module: {
        rules: [{
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                use: ExtractDefaultThemeCSS.extract({
                    use: [
                        {
                            loader: 'css-loader', options: {
                            sourceMap: true
                            }
                        },
                        {
                            loader: 'less-loader', options: {
                            sourceMap: true
                            }
                        }
                    ]
                }),
                test: /(themes).default.*\.less$/,
            },
            {
                exclude: /(themes).*\.less/,
                test: /\.less$/,
                use: [
                        'style-loader', 
                        {
                            loader: 'css-loader', options: {
                            sourceMap: true
                            }
                        },
                        {
                            loader: 'less-loader', options: {
                            sourceMap: true
                            }
                        }
                    ]
            },
            {
                test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
                use: ['url-loader'],
            },
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /(node_modules|diagram)/
            },
            {
                test: /\.js$/,
                use: ["source-map-loader"],
                enforce: "pre"
            }
        ]
    },
    plugins: [
        ExtractDefaultThemeCSS
    ],
    watchOptions: {
        ignored: /(node_modules|build)/
    },
    devServer: {
        contentBase: path.join(__dirname, 'build'),
        port: 9000
    },
    devtool: 'source-map',
    optimization: {
        minimizer: [
            new UglifyJsPlugin({
                uglifyOptions: {
                    keep_fnames: true,
                }
            })
        ]
    }
}
