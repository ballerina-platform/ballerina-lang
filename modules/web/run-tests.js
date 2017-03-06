var argv = require('yargs').argv;
var shell = require('shelljs');
if (argv.skipTests === "true") {
    console.log('Skipping Tests');
    return;
} else {
    console.log('Running Tests');
    shell.exec("NODE_ENV=test mocha-webpack --require ./js/tests/js/spec/setup.js --webpack-config ./webpack.config.js ./js/tests/js/spec/BallerinaTest.js", function(code) {
        shell.exit(code);
    });
}