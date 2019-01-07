const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

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
        extensions: ['.tsx', '.ts', '.js', '.json']
    },
    module: {
        rules: [{
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
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
    watchOptions: {
        ignored: [
            /node_modules([\\]+|\/)+(?!@ballerina)/,
            /build/
        ]
    },
    devServer: {
        contentBase: path.join(__dirname, 'build'),
        port: 9000
    },
    plugins: [
        new CopyWebpackPlugin([
            { context: path.resolve(__dirname, 'node_modules', '@ballerina', 'theme'), from: 'lib', to: 'themes' }
        ])
    ],
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
