const fs = require('fs'),
    glob = require('glob'),
    decompress = require('decompress'),
    path = require('path'),
    webModuleBuild = path.join(__dirname, '..', 'web', 'target',
                  'ballerina-composer-web-*.zip'),
    serviceBuild = path.join(__dirname, '..', 'services',
                  'workspace-service', 'target', 'workspace-service-*.jar');

// copy resources from web module
function prepareWebModule() {
    // search for web module build file
    let foundFiles = glob.sync(webModuleBuild);

    if(foundFiles.length !== 1) {
        console.error('Error while searching for web module build file.');
    }
    // extract web module build to resources folder
    decompress(foundFiles[0], __dirname, {
        strip: 1
    });
}

// copy micro service
function prepareService() {
    // search for service jar
    let foundFiles = glob.sync(serviceBuild);

    if(foundFiles.length !== 1) {
        console.error('Error while searching for service build file.');
    }

    fs.createReadStream(foundFiles[0]).pipe(fs.createWriteStream(path.join(__dirname, 'workspace-service.jar')));
}

prepareWebModule();
prepareService();
