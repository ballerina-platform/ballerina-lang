import path from 'path';
import _ from 'lodash';

import parseArgv from './parseArgv';
import prepareWebpack from './prepareWebpack';
import { run, watch } from './runner';
import { existsFileSync } from '../util/exists';
import parseConfig from './parseConfig';
import requireWebpackConfig from './requireWebpackConfig';


function resolve(mod) {
  const absolute = existsFileSync(mod) || existsFileSync(`${mod}.js`);
  const file = absolute ? path.resolve(mod) : mod;
  return file;
}


const cliOptions = parseArgv(process.argv.slice(2), true);
const configOptions = parseConfig(cliOptions.opts);
const defaultOptions = parseArgv([]);

const options = _.defaults({}, cliOptions, configOptions, defaultOptions);

options.require.forEach((mod) => {
  require(resolve(mod)); // eslint-disable-line global-require
});

options.include = options.include.map(resolve);

options.webpackConfig = requireWebpackConfig(options.webpackConfig);

prepareWebpack(options, (err, webpackConfig) => {
  if (err) {
    throw err;
  } else if (options.watch) {
    watch(options, webpackConfig);
  } else {
    run(options, webpackConfig);
  }
});
