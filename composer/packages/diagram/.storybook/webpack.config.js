const path = require("path");
const TSDocgenPlugin = require("react-docgen-typescript-webpack-plugin");
module.exports = (baseConfig, env, config) => {
  config.module.rules.push({
    test: /\.(ts|tsx)$/,
    loader: require.resolve("ts-loader")
  },
  {
    test: /\.less$/,
    use: [ 'style-loader', 'css-loader', 'less-loader' ]
  });
  config.plugins.push(new TSDocgenPlugin()); // optional
  config.resolve.extensions.push(".ts", ".tsx");
  config.resolve.alias = {
    '../../theme.config$': path.join(
      __dirname, '../node_modules/@ballerina/theme/src/default-theme/theme.config')
  };
  config.node = {};
  config.node.net = "mock";
  return config;
};