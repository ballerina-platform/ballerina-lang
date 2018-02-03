/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/*eslint-disable */
const argv = require('yargs').argv;
const shell = require('shelljs');

if (argv.skipTests === 'true') {
    console.log('Skipping Tests');
} else {
    console.log('Running Tests');
    shell.exec('NODE_ENV=test mocha-webpack --require ./src/plugins/ballerina/tests/js/spec/setup.js ' +
                    '--webpack-config ./webpack.config.js ./src/plugins/ballerina/tests/js/spec/BallerinaTest.js', (code) => {
        shell.exit(code);
    });

    //TODO - Running multiple files(./js/tests/js/spec/BallerinaTest.js and ./js/tests/js/spec/LanguageServerTest.js)
    // with * didn't work. Hence running the command again for ./js/tests/js/spec/LanguageServerTest.js. 
    shell.exec('NODE_ENV=test mocha-webpack --require ./src/plugins/ballerina/tests/js/spec/setup.js ' +
                    '--webpack-config ./webpack.config.js ./src/plugins/ballerina/tests/js/spec/LanguageServerTest.js', (code) => {
        shell.exit(code);
    });
}

/*eslint-enable */
