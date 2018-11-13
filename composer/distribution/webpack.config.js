const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');

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
          __dirname, 'node_modules/@ballerina/theme/src/default-theme/theme.config')
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
        },
        {
          test: /\.js$/,
          use: ["source-map-loader"],
          enforce: "pre"
        }
      ]
    },
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