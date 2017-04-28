// you can use this file to add your custom webpack plugins, loaders and anything you like.
// This is just the basic way to add addional webpack configurations.
// For more information refer the docs: https://getstorybook.io/docs/configurations/custom-webpack-config

// IMPORTANT
// When you add this file, we won't add the default configurations which is similar
// to "React Create App". This only has babel loader to load JavaScript.
const path = require('path');
const composerConfig = require('../webpack.config');

// Export a function. Accept the base config as the only param.
module.exports = function(storybookBaseConfig, configType) {
  // configType has a value of 'DEVELOPMENT' or 'PRODUCTION'
  // You can change the configuration based on that.
  // 'PRODUCTION' is used when building the static version of storybook.

  // Make whatever fine-grained changes you need
  storybookBaseConfig.module.loaders = storybookBaseConfig.module.loaders.concat([{
        test: /\.css?$/,
        loaders: [ 'style', 'raw' ],
        include: path.resolve(__dirname, '../')
      },
      {
        test: /\.(png|jpg|svg|cur|gif)$/,
        loaders: [ 'file-loader' ]
      }
  ]);

  storybookBaseConfig.resolve.fallback.push(path.resolve(__dirname, '../'));
  storybookBaseConfig.resolve.fallback.push(path.resolve(__dirname, '../js/'));

  storybookBaseConfig.resolve.alias = composerConfig.resolve.alias;

  // Return the altered config
  return storybookBaseConfig;
};
