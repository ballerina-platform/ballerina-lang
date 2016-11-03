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
define(['require', 'logger', 'jquery', 'lodash', 'diagram', 'tool_palette', 'main_elements', 'processors', 'command', 'workspace', 'jquery_ui'],

    function (require, log, $, _, Diagrams, Tools, MainElements, Processors, CommandManager, WorkspaceManager) {

    var app = {};
    // Setting the default service parameters
    var serviceProduces = "MediaType.APPLICATION_JSON",
        serviceBasePath = "/stock",
        servicePackageName = "com.sample",
        serviceTags = "stock_info,stock_update",
        serviceDescription = "Rest api for get stocks details";

    var lifeLineOptions = {};
        lifeLineOptions.class = "lifeline";
        // Lifeline rectangle options
        lifeLineOptions.rect = {};
        lifeLineOptions.rect.width = 100;
        lifeLineOptions.rect.height = 30;
        lifeLineOptions.rect.roundX = 20;
        lifeLineOptions.rect.roundY = 20;
        lifeLineOptions.rect.class = "lifeline-rect";
        // Lifeline middle-rect options
        lifeLineOptions.middleRect = {};
        lifeLineOptions.middleRect.width = 100;
        lifeLineOptions.middleRect.height = 300;
        lifeLineOptions.middleRect.roundX = 1;
        lifeLineOptions.middleRect.roundY = 1;
        lifeLineOptions.middleRect.class = "lifeline-middleRect";
        // Lifeline options
        lifeLineOptions.line = {};
        lifeLineOptions.line.height = 300;
        lifeLineOptions.line.class = "lifeline-line";
        // Lifeline text options
        lifeLineOptions.text = {};
        lifeLineOptions.text.class = "lifeline-title";

    app.init = function(){

        app.workspaceServiceURL = "http://localhost:8289/service/workspace";
        app.eventManager = new Diagrams.Models.EventManager({});
        app.commandManager = new CommandManager(app);
        app.workspaceManager = new WorkspaceManager(app);

        //  TODO refactor and move to proper backbone classes
        $(document).ready(function () {
            var scrWidth = $("#page-content").width();
            var treeContainer = $("#tree-container");
            var rightContainer = $("#right-container");
            //TODO: remove
            treeContainer.resizable({
                ghost: false,
                minWidth: scrWidth / 16,
                maxWidth: scrWidth / 2,
                resize: function (event, el) {
                    // rightContainer.css("width", scrWidth - el.size.width);
                }
            });

            var toolContainer = $("#tool-palette");
            var editorContainer = $("#editor-container");
            var propertyContainer = $(".property-container");
            //toolContainer.width(scrWidth / 8);
            toolContainer.resizable({
                ghost: false,
                minWidth: 170,
                maxWidth: rightContainer.width() / 3,
                resize: function (event, el) {
                    editorContainer.css("width", rightContainer.innerWidth() - toolContainer.outerWidth(true) - propertyContainer.outerWidth(true));
                }
            });
        });

        // Create the model for the diagram
        app.diagram = new Diagrams.Models.Diagram({});
        app.definedConstants = [];
    };

    app.createPoint = function (x, y) {
        return new GeoCore.Models.Point({'x': x, 'y': y});
    };

    app.createLifeLine = function (title, center, cssClass, utils, parameters, textModel, type) {
        return new SequenceD.Models.LifeLine({
            title: title,
            centerPoint: center,
            cssClass: cssClass,
            utils: utils,
            parameters: parameters,
            textModel: textModel,
            type: type
        });
    };

    app.createToolPalette = function(){
        // Create main tool group
        var mainToolGroup = new Tools.Models.ToolGroup({
            toolGroupName: "Main Elements",
            toolGroupID: "main-tool-group"
        });

        for (var lifeline in MainElements.lifelines) {
            var tool = new Tools.Models.Tool(MainElements.lifelines[lifeline]);
            mainToolGroup.toolCollection.add(tool);
        }

        // Create mediators tool group
        var mediatorsToolGroup = new Tools.Models.ToolGroup({
            toolGroupName: "Mediators",
            toolGroupID: "mediators-tool-group"
        });

        for (var manipulator in Processors.manipulators) {
            var tool = new Tools.Models.Tool(Processors.manipulators[manipulator]);
            mediatorsToolGroup.toolCollection.add(tool);
        }
        for (var flowController in Processors.flowControllers) {
            var tool = new Tools.Models.Tool(Processors.flowControllers[flowController]);
            mediatorsToolGroup.toolCollection.add(tool);
        }

        // Create tool palette
        var toolPalette = new Tools.Models.ToolPalette();
        toolPalette.add(mainToolGroup);
        toolPalette.add(mediatorsToolGroup);

        var paletteView = new Tools.Views.ToolPaletteView({collection: toolPalette});
        paletteView.render();
    };


    app.initTabs = function initTabs(){

        // Configuring dynamic  tab support
        var tab = new Diagrams.Models.Tab({
            resourceId: "seq_1",
            hrefId: "#seq_1",
            resourceTitle: "Resource",
            createdTab: false
        });

        var tabListView = new Diagrams.Views.TabListView({model: tab});
        tabListView.render(tab);
        var diagramObj1 = new Diagrams.Models.Diagram({});
        tab.addDiagramForTab(diagramObj1);
        var tabId1 = tab.get("resourceId");
        var linkId1 = tab.get("hrefId");
        //Enabling tab activation at page load
        $('.tabList a[href="#' + tabId1 + '"]').tab('show');
        var dgModel1 = tab.getDiagramOfTab(tab.attributes.diagramForTab.models[0].cid);
        dgModel1.CurrentDiagram(dgModel1);
        var svgUId1 = tabId1 + "4";
        var options = {selector: linkId1, wrapperId: svgUId1};
        // get the current diagram view for the tab
        var currentView1 = dgModel1.createDiagramView(dgModel1, options);
        // set current tab's diagram view as default view
        currentView1.currentDiagramView(currentView1);
        tab.setDiagramViewForTab(currentView1);
        // mark tab as visited
        tab.setSelectedTab();
        var preview = new Diagrams.Views.DiagramOutlineView({mainView: currentView1});
        preview.render();
        tab.preview(preview);

        defaultView.renderMainElement("Source", 1, MainElements.lifelines.SourceLifeline);
        defaultView.model.sourceLifeLineCounter(1);
        defaultView.renderMainElement("Resource", 1, MainElements.lifelines.ResourceLifeline);
        defaultView.model.resourceLifeLineCounter(1);
        //create initial arrow between source and resource
        var currentSource = defaultView.model.diagramSourceElements().models[0];
        var currentResource = defaultView.model.diagramResourceElements().models[0];
        tabListView.drawInitArrow(currentSource,currentResource,defaultView);

    };

    $(document).ready(function(){
        $("#empty-workspace-wrapper").show();
        $("#resource-tabs-wrapper").hide();
        $("#breadcrumbRow").hide();
        $("#serviceAndSourceButtonsRow").hide();
    });

    var formatter;
    define('formatter', ['beautify/beautify'],
        function(beautify) {
            var beautify = beautify.js_beautify;
            formatter = beautify;

        }
    );
    require(['formatter']);

    var mainEditor;
    define('testace', ['ace/ace','ace/ext/language_tools'],
        function(ace,langTools, res) {
            console.log("source view lookup");
            var editor = ace.edit("ace-editor");
            mainEditor = editor;
            //Avoiding ace warning
            mainEditor.$blockScrolling = Infinity;
            mainEditor.setTheme("ace/theme/twilight");
            mainEditor.session.setMode("ace/mode/nel");
            var langTools = ace.require("ace/ext/language_tools");
            mainEditor.setOptions({
                enableBasicAutocompletion:true


            });
            mainEditor.setBehavioursEnabled(true);
            //bind auto complete to key press
            mainEditor.commands.on("afterExec", function(e){
                if (e.command.name == "insertstring"&&/^[\w.]$/.test(e.args)) {
                    mainEditor.execCommand("startAutocomplete");
                }
            });

        }
    );
    require(['testace']);

    app.alertSuccess = function alertSuccess(msg){
        var alertArea = $('#alertArea');
        alertArea.text(msg);
        alertArea.removeClass("alert-danger");
        alertArea.addClass("alert-success");
        alertArea.fadeTo(4000, 500).slideUp(500, function () {
            alertArea.slideUp(500);
        });
    };

    app.alertError = function alertError(msg){
        var alertArea = $('#alertArea');
        alertArea.text(msg);
        alertArea.addClass("alert-danger");
        alertArea.removeClass("alert-success");
        alertArea.fadeTo(4000, 500).slideUp(500, function () {
            alertArea.slideUp(500);
        });
    };

    app.setBreadcrumb = function setBreadcrumb(path, file){
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

    app.loadServiceProperties = function loadServiceProperties() {
        $("#produces").val(serviceProduces);
        $("#basePath").val(serviceBasePath);
        $("#packageName").val(servicePackageName);
        $("#tags").val(serviceTags);
        $("#description").val(serviceDescription);
        $('#servicePropertiesModal').modal('show');
    };

    app.saveServiceProperties = function saveServiceProperties() {
        serviceProduces = $("#produces").val();
        serviceBasePath = $("#basePath").val();
        servicePackageName = $("#packageName").val();
        serviceTags = $("#tags").val();
        serviceDescription = $("#description").val();
        $('#servicePropertiesModal').modal('hide');
    };

    app.loadGlobalVariables = function(){
        window.$ = jquery;
        window._ = _;
        window.Backbone = backbone;
        window.log4javascript = log4javascript;
    };

    return app;
});