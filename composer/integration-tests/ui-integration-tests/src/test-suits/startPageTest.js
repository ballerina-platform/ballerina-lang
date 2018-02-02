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
var models = require('./utils/syntax-models.js');

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
            .click('#btn-welcome-new')
            .pause(3000)
            .then(function () {
                var argument = models.getArgumentDeclarationModel(3, "m", "message");
                var resource = models.getResourceModel(2, "resource1", [argument]);
                var service = models.getServiceModel(1, "service1", [resource]);
                var packages = models.getPackageModel(".");
                var root = models.getRootModel();
                root.root.push(packages);
                root.root.push(service);

                utils.renderSyntax(root, driver);
            })
            .pause(3000)
            .saveScreenshot('./' + utils.getActualImagePath() + 'serviceActual.png') // Save the screenshot to disk
            .getTitle().then(function (title) {
            console.log('Title was: ' + title);
            var callback = function (equal) {
                expect(equal).to.be.true;
                done();
            };
            // Compare the images with given expected screenshot
            utils.compareImages(utils.getScreenShotBaselinePath() + 'serviceExpected.png', utils.getActualImagePath() + 'serviceActual.png', 'createServiceDiff', callback);
        });
    });

    it('Drag and Drop A Service Container', function (done) {
        driver.click('.fw-delete.delete-icon')
            .pause(2000)
            .then(function () {
                driver.execute(utils.dragAndDropContainer("#service", ".canvas-container", {
                    dropTargetClass: "design-view-hover"
                }));
            })
            .pause(2000)
            .then(function () {
                done();
            });
    });

    it('Drag and Drop A Assignment', function (done) {
        driver.pause(2000)
            .then(function () {

            })
            .pause(2000)
            .then(function () {
                done();
            });
    });

    /**
     * After running the test suit stop services that need to be stopped.
     * @return {Object} WebDriverIO
     * */
    after(function () {
        utils.killChildProcess();
        return driver.end();
    })
});
