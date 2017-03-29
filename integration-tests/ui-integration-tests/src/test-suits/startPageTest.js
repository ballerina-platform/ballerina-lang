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
var Mugshot = require('mugshot');
var WebdriverIOAdapter = require('mugshot-webdriverio');
var webdriverio = require('webdriverio');
var LooksSameAdapter = require('mugshot-looks-same');
var fs = require('fs');
var temp = require('temp');
var path = require('path');
var utils = require('./test-utils/ui-test-common-utils.js');


var options = {
    desiredCapabilities: {
        browserName: 'firefox'
    }
};
console.log('running Test Suit');
describe("Ballerina UI Test", function () {
    this.timeout(30000);
    var driver = {};

    before(function () {
        driver = webdriverio.remote(options);
        return driver.init();
    });

    it('Open New File', function (done) {
        // this.timeout(15000);
        driver.url('http://localhost:9091/')// navigate to the web page
            .click('.new-welcome-button')
            .saveScreenshot('./snapshot1.png')
            .pause(5000)
            .saveScreenshot('./snapshot3.png') // Save the screenshot to disk
            .getTitle().then(function (title) {
                console.log('Title was: ' + title);
                var callback = function(equal){
                    expect(equal).to.be.true;
                    done();
                };
                utils.compareImages('snapshot2.png','snapshot3.png', callback);
            });
    });

    it('Drag and Drop Service', function (done) {
        done();
    });

    after(function () {
        return driver.end();
    })
});
