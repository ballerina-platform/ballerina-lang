const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const ExtractVSCodeThemeCSS = new ExtractTextPlugin({
    filename: (getPath) => {
        return getPath('themes/ballerina-vscode.css').replace('themes/js', 'css');
    },
    allChunks: true
});

module.exports = {
    output: {
        path: path.resolve(__dirname, 'build'),
        filename: 'composer.js',
        library: 'ballerinaComposer',
        libraryTarget: 'umd'
    },
    resolve: {
        alias: {
            '../../theme.config$': path.join(
                __dirname, 'node_modules/@ballerina/theme/src/themes/vscode/theme.config')
        },
        extensions: ['.tsx', '.ts', '.js', '.json']
    },
    module: {
        rules: [{
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                use: ExtractVSCodeThemeCSS.extract({
                    use: ['css-loader', 'less-loader']
                }),
                test: /(themes).vscode.*\.less$/,
            },
            {
                exclude: /(themes).*\.less/,
                test: /\.less$/,
                use: ['style-loader', 'css-loader', 'less-loader']
            },
            {
                test: /\.scss$/,
                use: ['style-loader', 'css-loader', 'sass-loader']
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
        ExtractVSCodeThemeCSS
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