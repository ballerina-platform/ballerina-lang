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
var expect = require('chai').expect;
var webDriverIO = require('webdriverio');
var fs = require('fs');
var temp = require('temp');
var path = require('path');
var utils = require('./utils/ui-test-common-utils.js');

console.log('running Test Suit');
describe("Ballerina UI Tests", function () {
    this.timeout(30000);
    var driver = {};

    /**
     * Before the test suit start setup necessary configurations.
     * @return {Object} webDriverIO instance
     * */
    before(function () {
        driver = webDriverIO.remote(utils.getWebDriverConfigurations());
        return driver.init();
    });

    /**
     * Service creation test.
     * */
    it('Create Service', function (done) {
        // navigate to the web page.
        driver.url(utils.getComposerBaseUrl())// navigate to the web page
            .click('.new-welcome-button')
            .pause(3000)
            .saveScreenshot('./serviceActual.png') // Save the screenshot to disk
            .getTitle().then(function (title) {
            console.log('Title was: ' + title);
            var callback = function (equal) {
                expect(equal).to.be.true;
                done();
            };
            utils.compareImages(utils.getScreenShotBaselinePath() + 'serviceExpected.png', 'serviceActual.png', 'serviceDiff', callback);
        });
    });

    it('Drag and Drop', function (done) {
        done();
    });

    /**
     * After running the test suit stop services that need to be stopped.
     * @return {Object} WebDriverIO
     * */
    after(function () {
        // utils.killChildProcess();
        return driver.end();
    })
});
