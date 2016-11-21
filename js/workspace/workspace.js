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
define(['jquery', 'lodash', 'backbone', 'file_browser', 'logger', 'bootstrap'], function ($, _, Backbone, FileBrowser, log) {

    // workspace manager constructor
    /**
     * Arg: application instance
     */
    return function(app) {
        if(_.isUndefined(app.commandManager)){
            var error = "CommandManager is not initialized.";
            log.error(error);
            throw error;
        }
        var configLocation,
            configFileName,
            saveServiceURL = workspaceServiceURL + "/write";

        this.openNewConfigWizard = function openNewConfigWizard(){

            var newWizardModal =  $('#newConfigModal'),
                newWizardError = $('#newWizardError'),
                selectFolderBtn = newWizardModal.find("#openFileBrowserBtn"),
                createBtn = newWizardModal.find("#createBtn"),
                locationInput = newWizardModal.find("#location"),
                fileNameInput = newWizardModal.find("#configName"),
                fileBrowserWizard = $('#fileBrowserModel'),
                fbSelectBtn = fileBrowserWizard.find('#selectBtn'),
                fbCancelBtn = fileBrowserWizard.find('#cancelBtn'),
                fileBrowser;

            newWizardError.hide();

            selectFolderBtn.click(function(){
                newWizardModal.modal('hide');
                fileBrowserWizard.modal('show');
                fileBrowser = new FileBrowser({container:"#fileTree"});
            });

            fbCancelBtn.click(function(){
                fileBrowserWizard.modal('hide');
                newWizardModal.modal('show');
            });

            fbSelectBtn.click(function(){
                var selected = fileBrowser.selected;
                if(selected){
                    fileBrowserWizard.modal('hide');
                    newWizardModal.modal('show');
                    locationInput.val(selected);
                }
            });

            createBtn.click(function(){
                if(_.isEmpty(locationInput.val())){
                    newWizardError.text("Invalid Value for Location." );
                    newWizardError.show();
                    return;
                }
                if(_.isEmpty(fileNameInput.val())){
                    newWizardError.text("Invalid Value for File Name." );
                    newWizardError.show();
                    return;
                }
                var config = '@Path ("/stock")\n' +
                    '@Source (interface="default")\n' +
                    '@Service (tags = {"stock_info","stock_update"}, description = "Rest api for do operations on admin", produces = MediaType.APPLICATION_JSON)\n' +
                    'package com.sample;\n\n';

                var payload = "location=" + (btoa(locationInput.val() + "/" + fileNameInput.val())) + "&config=" + (btoa(config));

                $.ajax({
                    url: saveServiceURL,
                    type: "POST",
                    data: payload,
                    contentType: "text/plain; charset=utf-8",
                    async: false,
                    success: function (data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            app.alertSuccess("File " + locationInput.val() + "/" + fileNameInput.val()  + " created successfully.");
                            newWizardModal.modal('hide');
                            $("#breadcrumbRow").show();
                            app.setBreadcrumb(locationInput.value, fileNameInput.val());
                            $("#empty-workspace-wrapper").hide();
                            $("#resource-tabs-wrapper").show();
                            $("#serviceAndSourceButtonsRow").show();
                            app.initTabs();
                        } else {
                            newWizardError.text("Something went wrong. " + textStatus);
                            newWizardError.show();
                        }
                    },
                    error: function(res, errorCode, error){
                        newWizardError.text("Something went wrong. " + errorCode + " " + error + " " + res.responseText);
                        newWizardError.show();
                    }
                });

            });
            newWizardModal.modal('show');
        };

       this.saveConfiguration = function saveConfiguration() {
            var tags = serviceTags.split(",");
            var tagString = "{";
            tags.forEach(function (tag) {
                tagString += ('"' + tag + '",');
            });
            tagString += '}';
            var config = '@Path ("' + serviceBasePath + '")\n' +
                '@Source (interface="default")\n' +
                '@Service (tags =' + tagString + ', description = "' + serviceDescription + '", produces = ' + serviceProduces + ')\n' +
                'package ' + servicePackageName + ';\n\n';
            config += diagram.parseTree();
            var payload = "location=" + (btoa(configLocation.value + "/" + configFileName.value)) + "&config=" + (btoa(config));

            $.ajax({
                url: saveServiceURL,
                type: "POST",
                data: payload,
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (data, textStatus, xhr) {
                    if (xhr.status == 200) {
                        alertSuccess("Configuration was saved successfully.");
                    } else {
                        alertError("Something went wrong. " + textStatus);
                    }
                },
                error: function(res, errorCode, error){
                    alertError("Something went wrong. " + errorCode + " " + error + " " + res.responseText);
                }
            });
        };

        this.toggleSource = function toggleSource() {
            var editorMain = document.getElementById("sourceViewTab");
            var tabContainer = document.getElementById("tab-container");
            var editorContainer = document.getElementById("editor-view");

            //Reset ace editor content
            mainEditor.setValue("");
            //Hard coded sourceConfig :TODO
            var tags = serviceTags.split(",");
            var tagString = "{";
            tags.forEach(function (tag) {
                tagString += ('"' + tag + '",');
            });
            tagString = tagString.substring(0, tagString.length - 1);
            tagString += '}';
            var sourceConfig = '@Path ("' + serviceBasePath + '")\n' +
                '@Source (interface="default")\n' +
                '@Service (tags =' + tagString + ', description = "' + serviceDescription + '", produces = ' + serviceProduces + ')\n' +
                'package ' + servicePackageName + ';\n\n';

            //Source not loaded yet
            if (editorMain.style.display == "none") {
                // hide design editor area
                editorContainer.style.display = "none";
                // show tab area
                tabContainer.style.display = "block";
                //show ace editor
                editorMain.style.display = "block";

                var treeResult = diagram.parseTree();
                var formattedValue = formatter(treeResult, {indent_size: 3});
                sourceConfig += formattedValue;
                mainEditor.session.setValue(sourceConfig);
            }
            //source already loaded, hence need to load the design view
            else {
                // Hide source view
                editorMain.style.display = "none";

                //show design editor area
                editorContainer.style.display = "block";
            }
        };

        this.setBreadcrumb = function(path, file){
            var path = _.replace(path, /\\/gi, "/")
            var pathArr = _.split(path, "/");

            var breadCumbList = $("#breadcrumbList");
            breadCumbList.empty();
            pathArr.forEach(function(segment){
                if(!_.isEmpty(segment)){
                    var li = $("<li>"+segment+"</li>");
                    li.addClass("breadcrumb-item");
                    breadCumbList.append(li);
                }
            });
            var fileLi = $("<li>"+file+"</li>");
            fileLi.addClass("breadcrumb-item");
            fileLi.addClass("active");
            breadCumbList.append(fileLi);
        };

        app.commandManager.registerCommand("open-new-config-wizard", {key:""});
        app.commandManager.registerHandler("open-new-config-wizard", workspaceManager.openNewConfigWizard);
        app.commandManager.registerCommand("toggle-source", {key:""});
        app.commandManager.registerHandler("toggle-source", workspaceManager.toggleSource);

    }
});

