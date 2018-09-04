const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const htmlWebpackPlugin = new HtmlWebpackPlugin({
    template: path.join(__dirname, 'examples/src/index.html'),
    filename: './index.html',
});

module.exports = {
    entry: [
        path.join(__dirname, 'examples/src/index.js'),
        path.join(__dirname, 'src/style/main.less'),
    ],
    module: {
        rules: [{
            test: /\.(js|jsx)$/,
            use: ['babel-loader', { loader: 'eslint-loader', options: { emitWarning: true, emitError: false, failonError: false } }],
            exclude: /node_modules/,
        }, {
            test: /\.(css)$/,
            use: ['style-loader', 'css-loader'],
        }, {
            test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
            use: ['url-loader'],
        }, {
            test: /\.(less)$/,
            use: [{
                loader: 'style-loader',
            }, {
                loader: 'css-loader',
            }, {
                loader: 'less-loader',
            }],
        }],
    },
    plugins: [htmlWebpackPlugin],
    resolve: {
        extensions: ['.js', '.jsx'],
    },
    devServer: {
        port: 3001,
    },
};
