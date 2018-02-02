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
var childProcess = require('child_process');
var shell = require('shelljs');
var spawn = require('child_process').spawn;

// Export functionality with the module.
module.exports = {
    /**
     * Start a Child Process for Composer Web App to be run on.
     * @return null
     * */
    startComposerProcess: function () {
        console.log("Starting Composer...");
        childProcess.spawn('node', ['src/test-setup-scripts/start-composer.js'], {
            stdio: 'inherit',
            detached: true
        }).unref();
    },

    /**
     * Start a Child Process for Selenium Standalone Server to be run on.
     * @return null
     * */
    startSeleniumProcess: function () {
        console.log("Starting Selenium...");
        spawn('node', ['src/test-setup-scripts/start-selenium.js'], {
            stdio: 'inherit',
            detached: true
        }).unref();
    },

    /**
     * Kill All Child Processes started to support UI tests.
     * @return null
     * */
    killChildProcess: function () {
        console.log("Terminating UI test related processes...");
        spawn('node', ['src/test-setup-scripts/stop-composer.js'], {
            stdio: 'inherit',
            detached: true
        }).unref();
        
        // spawn('node', ['src/test-setup-scripts/stop-selenium.js'], {
        //     stdio: 'ignore',
        //     detached: true
        // }).unref();
    }
};