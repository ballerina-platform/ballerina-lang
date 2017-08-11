/*eslint-disable */
const argv = require('yargs').argv;
const shell = require('shelljs');

if (argv.skipTests === 'true') {
    console.log('Skipping Tests');
} else {
    console.log('Running Tests');
    shell.exec('NODE_ENV=test mocha-webpack --require ./js/tests/js/spec/setup.js ' +
                    '--webpack-config ./webpack.config.js ./js/tests/js/spec/BallerinaTest.js', (code) => {
        shell.exit(code);
    });

    //TODO - Running multiple files(./js/tests/js/spec/BallerinaTest.js and ./js/tests/js/spec/LanguageServerTest.js)
    // with * didn't work. Hence running the command again for ./js/tests/js/spec/LanguageServerTest.js. 
    shell.exec('NODE_ENV=test mocha-webpack --require ./js/tests/js/spec/setup.js ' +
                    '--webpack-config ./webpack.config.js ./js/tests/js/spec/LanguageServerTest.js', (code) => {
        shell.exit(code);
    });
}

/*eslint-enable */
