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
var selenium = require('selenium-standalone');

console.log('Installing Selenium Standalone Server...');
selenium.install({
    logger: function (message) {
        console.log(message);
    }
}, function (err) {
    if (err) console.log(err);

    console.log('Starting Selenium Standalone Server...');
    selenium.start(function (err, child) {
        if (err) return done(err);
        selenium.child = child;
    });
});