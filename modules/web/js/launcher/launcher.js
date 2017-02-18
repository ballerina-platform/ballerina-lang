/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['log', 'jquery', 'backbone', 'lodash', 'context_menu', 'mcustom_scroller', './launch-manager', 'alerts'],

    function (log, $, Backbone, _, ContextMenu, mcustomScroller, LaunchManager, alerts) {

    var Launcher = Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(config, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file browser with selector: ' + _.get(config, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;

            if(!_.has(config, 'application')){
                log.error('Cannot init file browser. config: application not found.')
            }
            this.application = _.get(config, 'application');
            this._options = config;
            this._lastWidth = undefined;
            this._verticalSeparator = $(_.get(this._options, 'separator'));
            this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));
            this._items = [];

            // register command
            this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
            this.application.commandManager.registerHandler(config.command.id, this.toggleExplorer, this);

	        this.compiled = _.template('<div><% if (!active) { %>'
	            + '<div class="debug-panel-header">'
	            + '     <span class="tool-group-header-title">Run</span>' 
	            + '</div>' 
	            + '<div class="btn-group col-xs-12">' 
	            + '     <button type="button" id="run_application" class="btn btn-default text-left btn-debug-activate col-xs-12" title="Start Debug">Application</button>' 
	            + '     <button type="button" id="run_service" class="btn btn-default text-left btn-debug-activate col-xs-12" title="Start Debug">Service</button>' 
	            + '</div>'
	            + '<% } %>' 
	            + '<% if (active) { %>'
	            + '<div class="debug-panel-header">'
	            + '     <span class="tool-group-header-title">Program Running</span></span>' 
	            + '</div>'             
	            + '<div class="btn-group col-xs-12">'
	            + '<button type="button" class="btn btn-default btn-debug-activate" title="Stop Application" id="stop_program"><i class="fw fw-stop" /> Stop Application</button>' 
	            + '</div><% } %></div>');


	        //event bindings
	        this._$parent_el.on('click',"#run_application", _.bindKey(this,'runApplication'));
	        this._$parent_el.on('click',"#run_service", _.bindKey(this,'runService'));
            this._$parent_el.on('click',"#stop_program", _.bindKey(this,'stopProgram'));

            LaunchManager.on("execution-started",_.bindKey(this,'renderBody'));
            LaunchManager.on("execution-ended",_.bindKey(this,'renderBody'));



        },

        runService: function(){
        	var activeTab = this.application.tabController.getActiveTab();
        	if(this.isReadyToRun(activeTab)) {
        	    var file = activeTab.getFile();
       		    LaunchManager.runService(file);
        	} else {
                alerts.error("Save file before running service");
        	}
        },

        runApplication: function(){
        	var activeTab = this.application.tabController.getActiveTab();

        	// only file tabs can run application
        	if(this.isReadyToRun(activeTab)) {
        	    var file = activeTab.getFile();
        	    LaunchManager.runApplication(file);
        	} else {
        	    alerts.error("Save file before running application");
        	}
        },

        isReadyToRun: function(tab) {
            if (!typeof tab.getFile === "function") {
                return false;
            }

            var file = tab.getFile();
            // file is not saved give an error and avoid running
            if(file.isDirty()) {
                return false;
            }

            return true;
        },

        stopProgram: function(){
            LaunchManager.stopProgram();
        },        

        isActive: function(){
            return this._activateBtn.parent('li').hasClass('active');
        },

        toggleExplorer: function(){
            if(this.isActive()){
                this._$parent_el.parent().width('0px');
                this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._activateBtn.parent('li').removeClass('active');

            } else {
                this._activateBtn.tab('show');
                var width = this._lastWidth || _.get(this._options, 'defaultWidth');
                this._$parent_el.parent().width(width);
                this._containerToAdjust.css('padding-left', width);
                this._verticalSeparator.css('left',  width - _.get(this._options, 'separatorOffset'));
            }
        },

        render: function () {
            var self = this;
            var activateBtn = $(_.get(this._options, 'activateBtn'));
            this._activateBtn = activateBtn;

            var launcherContainer = $('<div role="tabpanel"></div>');
            launcherContainer.addClass(_.get(this._options, 'cssClass.container'));
            launcherContainer.attr('id', _.get(this._options, ('containerId')));
            this._$parent_el.append(launcherContainer);

            activateBtn.on('click', function(e){
                $(this).tooltip('hide');
                e.preventDefault();
                e.stopPropagation();
                self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
            });

            activateBtn.attr("data-placement", "bottom").attr("data-container", "body");

            if (this.application.isRunningOnMacOS()) {
                activateBtn.attr("title", "Run (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ").tooltip();
            } else {
                activateBtn.attr("title", "Run  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ").tooltip();
            }

            this._verticalSeparator.on('drag', function(event){
                if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                    && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
                    self._verticalSeparator.css('left', event.originalEvent.clientX);
                    self._verticalSeparator.css('cursor', 'ew-resize');
                    var newWidth = event.originalEvent.clientX;
                    self._$parent_el.parent().width(newWidth);
                    self._containerToAdjust.css('padding-left', event.originalEvent.clientX);
                    self._lastWidth = newWidth;
                    self._isActive = true;
                }
                event.preventDefault();
                event.stopPropagation();
            });
            this._launcherContainer = launcherContainer;

            launcherContainer.mCustomScrollbar({
                theme: "minimal",
                scrollInertia: 0,
                axis: "xy"
            });
            if(!_.isEmpty(this._openedFolders)){
                this._openedFolders.forEach(function(folder){
                    self.createExplorerItem(folder);
                });
            }
            this.renderBody();
            return this;
        },


        renderBody : function(){
        	this._launcherContainer.html(this.compiled(LaunchManager));
        },

        showConsole : function(){
        	$("#tab-content-wrapper").css("height:70%");
        	$("#console-container").css("height:30%");
        }
    });

    return Launcher;

});

