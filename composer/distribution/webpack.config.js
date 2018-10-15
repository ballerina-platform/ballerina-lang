const path = require('path');

module.exports = {
    output: {
      path: path.resolve(__dirname, 'build'),
      filename: 'main.js'
    },
    resolve: {
      alias: {
        '../../theme.config$': path.join(
          __dirname, 'node_modules/@ballerina/diagram/lib/ballerina-theme/theme.config')
      },
      extensions: [ '.tsx', '.ts', '.js', '.json' ]
    },
    module: {
      rules: [
        {
          test: /\.css$/,
          use: [ 'style-loader', 'css-loader' ]
        },
        {
          test: /\.less$/,
          use: [ 'style-loader', 'css-loader', 'less-loader' ]
        },
        {
          test: /\.scss$/,
          use: [ 'style-loader', 'css-loader', 'sass-loader' ]
        },
        {
          test: /\.(png|jpg|svg|cur|gif|eot|svg|ttf|woff|woff2)$/,
          use: ['url-loader'],
        },
        {
          test: /\.tsx?$/,
          use: 'ts-loader',
          exclude: /(node_modules|diagram)/
        }
      ]
    }
  }