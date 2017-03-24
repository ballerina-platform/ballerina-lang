import path from 'path';
import fs from 'fs';
import interpret from 'interpret';

function sortExtensions(ext1, ext2) {
  if (ext1 === '.js') {
    return -1;
  }
  if (ext2 === '.js') {
    return 1;
  }
  return ext1.length - ext2.length;
}

const extensions = Object.keys(interpret.extensions).sort(sortExtensions);

function fileExists(filePath) {
  try {
    return fs.lstatSync(filePath).isFile();
  } catch (e) {
    return false;
  }
}

function findConfigFile(dirPath, baseName) {
  for (let i = 0; i < extensions.length; i++) {
    const filePath = path.resolve(dirPath, `${baseName}${extensions[i]}`);
    if (fileExists(filePath)) {
      return filePath;
    }
  }
  return null;
}

function getConfigExtension(configPath) {
  for (let i = extensions.length - 1; i >= 0; i--) {
    const extension = extensions[i];
    if (configPath.indexOf(extension, configPath.length - extension.length) > -1) {
      return extension;
    }
  }
  return path.extname(configPath);
}

function registerCompiler(moduleDescriptor) {
  if (!moduleDescriptor) {
    return;
  }

  if (typeof moduleDescriptor === 'string') {
    require(moduleDescriptor); // eslint-disable-line global-require
  } else if (!Array.isArray(moduleDescriptor)) {
    const module = require(moduleDescriptor.module); // eslint-disable-line global-require
    moduleDescriptor.register(module);
  } else {
    for (let i = 0; i < moduleDescriptor.length; i++) {
      try {
        registerCompiler(moduleDescriptor[i]);
        break;
      } catch (e) {
        // do nothing
      }
    }
  }
}

export default function requireWebpackConfig(webpackConfig) {
  if (!webpackConfig) {
    return {};
  }

  let configPath = path.resolve(webpackConfig);
  let configExtension = getConfigExtension(configPath);

  if (!fileExists(configPath)) {
    if (configExtension !== '.js') {
      return {};
    }

    const configDirPath = path.dirname(configPath);
    const configBaseName = path.basename(configPath, configExtension);

    configPath = findConfigFile(configDirPath, configBaseName);
    if (configPath === null) {
      return {};
    }

    configExtension = getConfigExtension(configPath);
  }

  registerCompiler(interpret.extensions[configExtension]);
  const config = require(configPath); // eslint-disable-line global-require

  return config.default || config;
}
