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
var looksSame = require('looks-same');
var childProcessManager = require('./../../test-setup-scripts/utils/child-process-manager.js');
var fs = require('fs');
var temp = require('temp');
var path = require('path');
var PNG = require('pngjs').PNG;
var pixelmatch = require('pixelmatch');

module.exports = {
    /**
     * Compare Two given images and fire the callback.
     * @param {string} expectedImage expected image path.
     * @param {string} actualImage actual image path.
     * @param {string} diffName diff image name
     * @param {function} callback callback function.
     * @return null.
     * */
    compareImages: function (expectedImage, actualImage, diffName, callback) {
        var expected = fs.readFileSync(expectedImage),
            actual = fs.readFileSync(actualImage);

        looksSame(expected, actual, function (error, equal) {
            console.log('equal: ' + equal);
            if (!equal) {
                var img1 = fs.createReadStream(expectedImage).pipe(new PNG()).on('parsed', doneReading),
                    img2 = fs.createReadStream(actualImage).pipe(new PNG()).on('parsed', doneReading),
                    filesRead = 0;

                function doneReading() {
                    if (++filesRead < 2) {
                        return;
                    }
                    var diff = new PNG({width: img1.width, height: img1.height});

                    pixelmatch(img1.data, img2.data, diff.data, img1.width, img1.height, {threshold: 0.1});

                    diff.pack().pipe(fs.createWriteStream('src/resources/differences/'+diffName+'.png'));
                }
            }
            callback(equal);
        });
    },

    /**
     * Kill All the child processes that needs to be kill after test run.
     * @return null.
     * */
    killChildProcess: function () {
        childProcessManager.killChildProcess();
    },

    /**
     * Get the web driver configurations.
     * @return null.
     * */
    getWebDriverConfigurations: function () {
        return {
            desiredCapabilities: {
                browserName: 'firefox'
            }
        };
    },

    /**
     * Get the base path for baseline screenshots.
     * @return {string} base path.
     * */
    getScreenShotBaselinePath: function () {
        return "src/resources/screenshots/baselines/";
    },

    /**
     * Get the Base URL of the Composer.
     * @return {string} composer base url.
     * */
    getComposerBaseUrl: function () {
        return "http://localhost:9091/";
    }
};
