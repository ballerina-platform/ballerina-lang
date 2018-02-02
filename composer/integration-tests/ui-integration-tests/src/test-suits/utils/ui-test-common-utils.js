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

                    diff.pack().pipe(fs.createWriteStream('target/' + diffName + '.png', {
                        defaultEncoding: 'utf8',
                        autoClose: true
                    }));
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
     * Get the diff image path.
     * @return {string} diff image path.
     * */
    getDiffImagePath: function () {
        return 'src/resources/differences/';
    },

    /**
     * Get the path for actual images which captured during the test.
     * @return {string} path string to actual images.
     * */
    getActualImagePath: function () {
        return 'src/resources/screenshots/capturedimages/';
    },

    /**
     * Get the Base URL of the Composer.
     * @return {string} composer base url.
     * */
    getComposerBaseUrl: function () {
        return "http://localhost:9091/";
    },

    /**
     * Drag and Drop a container to canvas.
     * @return {string} javascript query for drag and drop.
     * */
    dragAndDropContainer: function (dragTarget, dropLocation, options) {
        var query = "var dragTarget = $('" + dragTarget + "');" +
            "var dropLocation = $('" + dropLocation + "');" +
            "var dropLocationOffset = dropLocation.offset();" +
            "var dragTargetOffset = dragTarget.offset();";

        if (options.dropTargetClass) {
            query += "$('" + dropLocation + "').addClass('" + options.dropTargetClass + "');";
        }

        if (options.dx) {
            query += "var dx = " + options.dx + ";";
        } else {
            query += "var dx = dropLocationOffset.left - dragTargetOffset.left;";
        }

        if (options.dy) {
            query += "var dy = " + options.dy + ";";
        } else {
            query += "var dy = dropLocationOffset.top - dragTargetOffset.top;";
        }

        query += "dragTarget.simulate('drag', {" +
            "    dx: dx," +
            "    dy: dy," +
            "    mouseOver: function(){" +
            "       $('" + dropLocation + "').mouseenter();" +
            "    }" +
            "});";

        return query;
    },

    /**
     * Drag and Drop a tool to a container.
     * @param {string} dragTarget id of element to be dragged.
     * @param {string} dropLocation id or class of the drop location.
     * @param {object} options
     * @return {string} javascript query for drag and drop.
     * */
    dragAndDropTool: function (dragTarget, dropLocation, options) {
        return "var gadget = $('#service');" +
            "var target = $('.canvas-container');" +
            "var targetOffset = target.offset();" +
            "var gadgetOffset = gadget.offset();" +
            "$('.canvas-container').addClass('main-drop-zone-hover');" +
            "var dx = targetOffset.left - gadgetOffset.left;" +
            "var dy = targetOffset.top - gadgetOffset.top;" +
            "gadget.simulate('drag', {" +
            "    dx: dx," +
            "    dy: dy," +
            "mouseOver: function(){" +
            "$('.canvas-container').mouseenter();" +
            "}" +
            "});";
    },

    /**
     * Create a service on the canvas.
     * @return {string} javascript query.
     * */
    createService: function () {
        return "var serviceDef = window.composer.tabController.getActiveTab()" +
            ".getBallerinaFileEditor()._model.getFactory().createServiceDefinition();" +
            "var resourceDef = window.composer.tabController.getActiveTab()" +
            ".getBallerinaFileEditor()._model.getFactory().createResourceDefinition({});" +
            "var resourceArg = window.composer.tabController.getActiveTab()" +
            ".getBallerinaFileEditor()._model.getFactory().createResourceParameter();" +
            "resourceArg.setType('message');" +
            "resourceArg.setIdentifier('m');" +
            "resourceDef.addChild(resourceArg);" +
            "serviceDef.addChild(resourceDef);" +
            "window.composer.tabController.getActiveTab().getBallerinaFileEditor()._model.addChild(serviceDef);";
    },

    renderSyntax: function (model, driver) {
        var modelString = JSON.stringify(model);
        driver.execute("window.composer.tabController.getActiveTab().getBallerinaFileEditor()" +
            ".setModel(window.composer.tabController.getActiveTab().getBallerinaFileEditor()" +
            ".deserializer.getASTModel(JSON.parse('" + modelString + "')));" +
            "window.composer.tabController.getActiveTab().getBallerinaFileEditor().reDraw();");
    }
};
