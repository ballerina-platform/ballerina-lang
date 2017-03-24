module.exports = install;

var async = require('async');
var crypto = require('crypto');
var fs = require('fs');
var merge = require('lodash').merge;
var mapValues = require('lodash').mapValues;
var mkdirp = require('mkdirp');
var path = require('path');
var request = require('request');
var tarStream = require('tar-stream');

var computeDownloadUrls = require('./compute-download-urls');
var computeFsPaths = require('./compute-fs-paths');
var defaultConfig = require('./default-config');
var noop = require('./noop');

function install(opts, cb) {
  var total = 0;
  var progress = 0;
  var startedRequests = 0;
  var expectedRequests;
  var requestOpts = {followAllRedirects: true};

  if (typeof opts === 'function') {
    cb = opts;
    opts = {};
  }

  var logger = opts.logger || noop;

  if (!opts.baseURL) {
    opts.baseURL = defaultConfig.baseURL;
  }

  if (!opts.version) {
    opts.version = defaultConfig.version;
  }

  if (opts.drivers) {
    // Merge in missing driver options for those specified
    opts.drivers = mapValues(opts.drivers, function(config, name) {
      return merge({}, defaultConfig.drivers[name], config);
    });
  } else {
    opts.drivers = defaultConfig.drivers;
  }

  if (process.platform !== 'win32') {
    delete opts.drivers.ie;
  }
  expectedRequests = Object.keys(opts.drivers).length + 1;

  if (opts.proxy) {
    requestOpts.proxy = opts.proxy;
  }

  opts.progressCb = opts.progressCb || noop;

  logger('----------');
  logger('selenium-standalone installation starting');
  logger('----------');
  logger('');

  var fsPaths = computeFsPaths({
    seleniumVersion: opts.version,
    drivers: opts.drivers,
    basePath: opts.basePath
  });

  var urls = computeDownloadUrls({
    seleniumVersion: opts.version,
    seleniumBaseURL: opts.baseURL,
    drivers: opts.drivers
  });

  logInstallSummary(logger, fsPaths, urls);

  var tasks = [
    createDirs.bind(null, fsPaths),
    download.bind(null, {
      urls: urls,
      fsPaths: fsPaths
    }),
    asyncLogEnd.bind(null, logger)
  ];

  if (fsPaths.chrome) {
    tasks.push(chmodChromeDr.bind(null, fsPaths.chrome.installPath));
  }

  if (fsPaths.firefox) {
    tasks.push(chmodChromeDr.bind(null, fsPaths.firefox.installPath));
  }

  async.series(tasks, function(err) {
    cb(err, fsPaths);
  });

  function onlyInstallMissingFiles(opts, cb) {
    async.waterfall([
      checksum.bind(null, opts.to),
      isUpToDate.bind(null, opts.from, requestOpts)
    ], function (error, isLatest) {
      if (error) {
        return cb(error);
      }

      // File already exists. Prevent download/installation.
      if (isLatest) {
        logger('---');
        logger('File from ' + opts.from + ' has already been downloaded');
        expectedRequests -= 1;
        return cb();
      }

      opts.installer.call(null, {
        to: opts.to,
        from: opts.from
      }, cb);
    });
  }

  function download(opts, cb) {
    var installers = [{
      installer: installSelenium,
      from: opts.urls.selenium,
      to: opts.fsPaths.selenium.downloadPath
    }];

    if (opts.fsPaths.chrome) {
      installers.push({
        installer: installChromeDr,
        from: opts.urls.chrome,
        to: opts.fsPaths.chrome.downloadPath
      });
    }

    if (process.platform === 'win32' && opts.fsPaths.ie) {
      installers.push({
        installer: installIeDr,
        from: opts.urls.ie,
        to: opts.fsPaths.ie.downloadPath
      });
    }

    if (opts.fsPaths.firefox) {
      installers.push({
        installer: installFirefoxDr,
        from: opts.urls.firefox,
        to: opts.fsPaths.firefox.downloadPath
      })
    }

    var steps = installers.map(function (opts) {
      return onlyInstallMissingFiles.bind(null, opts);
    });

    async.parallel(steps, cb);
  }

  function installSelenium(opts, cb) {
    getDownloadStream(opts.from, function(err, stream) {
      if (err) {
        return cb(err);
      }

      stream
        .pipe(fs.createWriteStream(opts.to))
        .once('error', cb.bind(null, new Error('Could not write to ' + opts.to)))
        .once('finish', cb);
    });
  }

  function installChromeDr(opts, cb) {
    installZippedFile(opts.from, opts.to, cb);
  }

  function installIeDr(opts, cb) {
    installZippedFile(opts.from, opts.to, cb);
  }

  function installFirefoxDr(opts, cb) {
    // only windows build is a zip
    if (path.extname(opts.from) === '.zip') {
      installZippedFile(opts.from, opts.to, cb);
    } else {
      installGzippedFile(opts.from, opts.to, cb);
    }
  }

  function installGzippedFile(from, to, cb) {
    getDownloadStream(from, function(err, stream) {
      if (err) {
        return cb(err);
      }
      // Store downloaded compressed file
      var gzipWriteStream = fs.createWriteStream(to)
        .once('error', cb.bind(null, new Error('Could not write to ' + to)));
      stream.pipe(gzipWriteStream);

      gzipWriteStream.once('finish', uncompressGzippedFile.bind(null, from, to, cb));
    });
  }

  function uncompressGzippedFile(from, gzipFilePath, cb) {
    var gunzip = require('zlib').createGunzip();
    var extractPath = path.join(path.dirname(gzipFilePath), path.basename(gzipFilePath, '.gz'));
    var writeStream = fs.createWriteStream(extractPath).once('error',
      function(error) {
        cb.bind(null, new Error('Could not write to ' + extractPath));
      }
    );
    var gunzippedContent = fs.createReadStream(gzipFilePath).pipe(gunzip)
        .once('error', cb.bind(null, new Error('Could not read ' + gzipFilePath)));

    if (from.substr(-7) === '.tar.gz') {
      var extractor = tarStream.extract();
      var fileAlreadyUnarchived = false;
      var cbCalled = false;

      extractor
        .on('entry', function(header, stream, callback) {
          if (fileAlreadyUnarchived) {
            if (!cbCalled) {
              cb(new Error('Tar archive contains more than one file'));
              cbCalled = true;
            }
            fileAlreadyUnarchived = true;
          }
          stream.pipe(writeStream);
          stream.on('end', function() {
            callback();
          })
          stream.resume();
        })
        .on('finish', function() {
          if (!cbCalled) {
            cb();
            cbCalled = true;
          }
        });
      gunzippedContent.pipe(extractor);
    } else {
      gunzippedContent.pipe(writeStream).on('finish', function() { cb(); });
    }
  }

  function installZippedFile(from, to, cb) {
    getDownloadStream(from, function(err, stream) {
      if (err) {
        return cb(err);
      }

      // Store downloaded compressed file
      var zipWriteStream = fs.createWriteStream(to)
        .once('error', cb.bind(null, new Error('Could not write to ' + to)));
      stream.pipe(zipWriteStream);

      // Uncompress downloaded file
      zipWriteStream.once('finish',
        uncompressDownloadedFile.bind(null, to, cb)
      );
    });
  }

  function getDownloadStream(downloadUrl, cb) {
    var r = request(downloadUrl, requestOpts)
      .on('response', function(res) {
        startedRequests += 1;

        if (res.statusCode !== 200) {
          return cb(new Error('Could not download ' + downloadUrl));
        }

        res.on('data', function(chunk) {
          progress += chunk.length;
          updateProgressPercentage(chunk.length);
        });

        total += parseInt(res.headers['content-length'], 10);

        cb(null, res);
      })
      .once('error', function(error) {
        cb(new Error('Could not download ' + downloadUrl + ': ' + error));
      });

    // initiate request
    r.end();
  }

  function uncompressDownloadedFile(zipFilePath, cb) {
    var yauzl = require('yauzl');
    var extractPath = path.join(path.dirname(zipFilePath), path.basename(zipFilePath, '.zip'));

    yauzl.open(zipFilePath, {lazyEntries: true}, function onOpenZipFile(err, zipFile) {
      if (err) {
        cb(err);
        return;
      }
      zipFile.readEntry();
      zipFile.once('entry', function (entry) {
        zipFile.openReadStream(entry, function onOpenZipFileEntryReadStream(err, readStream) {
          if (err) {
            cb(err);
            return;
          }
          var extractWriteStream = fs.createWriteStream(extractPath)
            .once('error', cb.bind(null, new Error('Could not write to ' + extractPath)));
          readStream
            .pipe(extractWriteStream)
            .once('error', cb.bind(null, new Error('Could not read ' + zipFilePath)))
            .once('finish', function onExtracted() {
              zipFile.close();
              cb();
            });
        });
      });
    })
  }

  function updateProgressPercentage(chunk) {
    if (expectedRequests === startedRequests) {
      opts.progressCb(total, progress, chunk);
    }
  }
}

function asyncLogEnd(logger, cb) {
  setImmediate(function() {
    logger('');
    logger('');
    logger('-----');
    logger('selenium-standalone installation finished');
    logger('-----');
    cb();
  });
}

function createDirs(paths, cb) {
  var installDirectories =
    Object
      .keys(paths)
      .map(function(name) {
        return paths[name].installPath;
      });

  async.eachSeries(
    installDirectories.map(basePath),
    mkdirp,
    cb
  );
}

function basePath(fullPath) {
  return path.dirname(fullPath);
}

function chmodChromeDr(where, cb) {
  var chmod = function () {
    fs.chmod(where, '0755', cb);
  };

  // node.js 0.10.x does not support fs.access
  if (fs.access) {
    fs.access(where, fs.R_OK | fs.X_OK, function(err) {
      if (err) {
        chmod();
      } else {
        return cb();
      }
    }.bind(this));
  } else {
    chmod();
  }
}

function logInstallSummary(logger, paths, urls) {
  ['selenium', 'chrome', 'ie', 'firefox'].forEach(function log(name) {
    if (!paths[name]) {
      return;
    }

    logger('---');
    logger(name + ' install:');
    logger('from: ' + urls[name]);
    logger('to: ' + paths[name].installPath);
  });
}

function checksum (filepath, cb) {
  if (!fs.existsSync(filepath)) {
    return cb(null, null);
  }

  var hash = crypto.createHash('md5');
  var stream = fs.createReadStream(filepath);

  stream.on('data', function (data) {
    hash.update(data, 'utf8');
  }).on('end', function () {
    cb(null, hash.digest('hex'));
  }).once('error', cb);
}

function unquote (str, quoteChar) {
  quoteChar = quoteChar || '"';

  if (str[0] === quoteChar && str[str.length - 1] === quoteChar) {
    return str.slice(1, str.length - 1);
  }

  return str;
}

function isUpToDate (url, requestOpts, hash, cb) {
  if (!hash) {
    return cb(null, false);
  }

  var query = merge({}, requestOpts, {
    url: url,
    headers: {
      'If-None-Match': '"' + hash + '"'
    }
  });

  var req = request.get(query);
  req.on('response', function (res) {
    req.abort();

    if (res.statusCode === 304) {
      return cb(null, true);
    }

    if (res.statusCode !== 200) {
      return cb(new Error('Could not request headers from ' + url + ': ', res.statusCode));
    }

    cb(null, false);
  }).once('error', function (err) {
    cb(new Error('Could not request headers from ' + url + ': ' + err));
  });
}
