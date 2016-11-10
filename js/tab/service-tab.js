/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
define(['require', 'jquery', 'lodash', './tab'], function (require, jquery, _, Tab) {
    var  resourceTab;

    resourceTab = Tab.extend({
        initialize: function (options) {

        },
        render: function () {
        }
    });

    return resourceTab;
});

//app.createToolPalette = function(){
//    // Create main tool group
//    var mainToolGroup = new Tools.Models.ToolGroup({
//        toolGroupName: "Main Elements",
//        toolGroupID: "main-tool-group"
//    });
//
//    for (var lifeline in MainElements.lifelines) {
//        var tool = new Tools.Models.Tool(MainElements.lifelines[lifeline]);
//        mainToolGroup.toolCollection.add(tool);
//    }
//
//    // Create mediators tool group
//    var mediatorsToolGroup = new Tools.Models.ToolGroup({
//        toolGroupName: "Mediators",
//        toolGroupID: "mediators-tool-group"
//    });
//
//    for (var manipulator in Processors.manipulators) {
//        var tool = new Tools.Models.Tool(Processors.manipulators[manipulator]);
//        mediatorsToolGroup.toolCollection.add(tool);
//    }
//    for (var flowController in Processors.flowControllers) {
//        var tool = new Tools.Models.Tool(Processors.flowControllers[flowController]);
//        mediatorsToolGroup.toolCollection.add(tool);
//    }
//
//    // Create tool palette
//    var toolPalette = new Tools.Models.ToolPalette();
//    toolPalette.add(mainToolGroup);
//    toolPalette.add(mediatorsToolGroup);
//
//    var paletteView = new Tools.Views.ToolPaletteView({collection: toolPalette});
//    paletteView.render();
//};
//
//
//
//var formatter;
//define('formatter', ['beautify/beautify'],
//    function(beautify) {
//        var beautify = beautify.js_beautify;
//        formatter = beautify;
//
//    }
//);
//require(['formatter']);
//
//var mainEditor;
//define('testace', ['ace/ace','ace/ext/language_tools'],
//    function(ace,langTools, res) {
//        console.log("source view lookup");
//        var editor = ace.edit("ace-editor");
//        mainEditor = editor;
//        //Avoiding ace warning
//        mainEditor.$blockScrolling = Infinity;
//        mainEditor.setTheme("ace/theme/twilight");
//        mainEditor.session.setMode("ace/mode/nel");
//        var langTools = ace.require("ace/ext/language_tools");
//        mainEditor.setOptions({
//            enableBasicAutocompletion:true
//
//
//        });
//        mainEditor.setBehavioursEnabled(true);
//        //bind auto complete to key press
//        mainEditor.commands.on("afterExec", function(e){
//            if (e.command.name == "insertstring"&&/^[\w.]$/.test(e.args)) {
//                mainEditor.execCommand("startAutocomplete");
//            }
//        });
//
//    }
//);
//require(['testace']);
//
//
//// Setting the default service parameters
//var serviceProduces = "MediaType.APPLICATION_JSON",
//    serviceBasePath = "/stock",
//    servicePackageName = "com.sample",
//    serviceTags = "stock_info,stock_update",
//    serviceDescription = "Rest api for get stocks details";
//
//app.loadServiceProperties = function loadServiceProperties() {
//    $("#produces").val(serviceProduces);
//    $("#basePath").val(serviceBasePath);
//    $("#packageName").val(servicePackageName);
//    $("#tags").val(serviceTags);
//    $("#description").val(serviceDescription);
//    $('#servicePropertiesModal').modal('show');
//};
//
//app.saveServiceProperties = function saveServiceProperties() {
//    serviceProduces = $("#produces").val();
//    serviceBasePath = $("#basePath").val();
//    servicePackageName = $("#packageName").val();
//    serviceTags = $("#tags").val();
//    serviceDescription = $("#description").val();
//    $('#servicePropertiesModal').modal('hide');
//};