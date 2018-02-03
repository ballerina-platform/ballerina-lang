/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var argv = require('yargs').argv;
var util = require('./../utils/child-process-manager.js');
var shell = require('shelljs');
if (argv.skipTests === "true") {
    console.log('Skipping Tests');
    return;
} else {
    console.log('Running Integration Tests');
    shell.exec("NODE_ENV=test env DIRECTORY=target mocha-webpack --require target/src/plugins/ballerina/tests/js/spec/setup.js --webpack-config webpack.config.js \"target/src/plugins/ballerina/tests/js/spec/*test.js\"", function (code) {
        console.log('Stop Composer Process.');
        util.killChildProcess();
        shell.exit(code);
    });
}
