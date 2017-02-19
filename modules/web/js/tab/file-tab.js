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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'workspace/file', 'ballerina/diagram-render/diagram-render-context',
        'ballerina/views/backend', 'ballerina/ast/ballerina-ast-deserializer', '../debugger/debug-manager', 'alerts'],
    function (require, log, $, _, Tab, Ballerina, File, DiagramRenderContext, Backend, BallerinaASTDeserializer, DebugManager, alerts) {
    var FileTab;

    FileTab = Tab.extend({
        initialize: function (options) {
            Tab.prototype.initialize.call(this, options);
            if (!_.has(options, 'file')) {
                this._file = new File({isTemp: true, isDirty: false}, {storage: this.getParent().getBrowserStorage()});
            } else {
                this._file = _.get(options, 'file');
            }
            if (_.has(options, 'astRoot')) {
                this._astRoot = _.get(options, 'astRoot');
            }
            //TODO convert Backend to a singleton
            this.app = options.application;
            this.parseBackend = new Backend({"url" : this.app.config.services.parser.endpoint});
            this.validateBackend = new Backend({"url" : this.app.config.services.validator.endpoint});
            this.deserializer = BallerinaASTDeserializer;
        },

        getTitle: function(){
            return _.isNil(this._file) ? "untitled" :  this._file.getName();
        },

        getFile: function () {
            return this._file;
        },

        render: function () {
            Tab.prototype.render.call(this);
            // if file already has content
            if(!_.isNil(this._file.getContent()) && !_.isEmpty(this._file.getContent().trim())){
                if(!_.isEmpty(this._file.getContent().trim())){
                    var validateResponse = this.validateBackend.parse(this._file.getContent());
                    if (validateResponse.errors != undefined && !_.isEmpty(validateResponse.errors)) {
                        this.renderBallerinaEditor(this._astRoot, true);
                        return;
                    }
                }
                var parseResponse = this.parseBackend.parse(this._file.getContent());
                //if no errors display the design.
                var root = this.deserializer.getASTModel(parseResponse);
                this.renderBallerinaEditor(root);
            } else if(!_.isNil(this._astRoot)) {
                this.renderBallerinaEditor(this._astRoot, false);
                var updatedContent = this.getBallerinaFileEditor().generateSource();
                this._file.setContent(updatedContent);
                this._file.setDirty(true);
                this._file.save();
            } else {
                this.renderBallerinaEditor(this.createEmptyBallerinaRoot(), false);
                var updatedContent = this.getBallerinaFileEditor().generateSource();
                this._file.setContent(updatedContent);
                this._file.save();
            }
        },

        renderBallerinaEditor: function(astRoot, parseFailed){
            var self = this;
            var ballerinaEditorOptions = _.get(this.options, 'ballerina_editor');
            var backendEndpointsOptions = _.get(this.options, 'application.config.services');
            var diagramRenderingContext = new DiagramRenderContext();

            var fileEditor = new Ballerina.views.BallerinaFileEditor({
                model: astRoot,
                parseFailed: parseFailed,
                file: self._file,
                container: this.$el.get(0),
                viewOptions: ballerinaEditorOptions,
                backendEndpointsOptions: backendEndpointsOptions,
                debugger: DebugManager
            });

            // change tab header class to match look and feel of source view
            fileEditor.on('source-view-activated swagger-view-activated', function(){
                this.getHeader().addClass('inverse');
                this.app.workspaceManager.updateMenuItems();
            }, this);
            fileEditor.on('design-view-activated', function(){
                this.getHeader().removeClass('inverse');
                this.app.workspaceManager.updateMenuItems();
            }, this);

            fileEditor.on('add-breakpoint', function(row){
                DebugManager.addBreakPoint(row, this._file.getName());
            }, this);

            fileEditor.on('remove-breakpoint', function(row){
                DebugManager.removeBreakPoint(row, this._file.getName());
            }, this);

            this.on('tab-removed', function() {
                this.removeAllBreakpoints();
            });

            var breakPointChangeCallback = function(debugPointChangedFileName) {
                var fileName = self._file.getName();
                
                if(debugPointChangedFileName === fileName) {
                    var newBreakpoints = DebugManager.getDebugPoints(fileName);
                    fileEditor.trigger('reset-breakpoints', newBreakpoints);
                }
            };
            DebugManager.on('breakpoint-added', breakPointChangeCallback);
            DebugManager.on('breakpoint-removed', breakPointChangeCallback);

            DebugManager.on('debug-hit', function(message){
                var position = message.location;
                if(position.fileName == this._file.getName()){
                    fileEditor.debugHit(DebugManager.createDebugPoint(position.lineNumber, position.fileName));
                }
            }, this);

            this._fileEditor = fileEditor;
            fileEditor.render(diagramRenderingContext);

            fileEditor.on("content-modified", function(){
                var updatedContent = fileEditor.getContent();
                this._file.setContent(updatedContent);
                this._file.setDirty(true);
                this._file.save();
                this.app.workspaceManager.updateMenuItems();
                this.trigger("tab-content-modified");
            }, this);

            this._file.on("dirty-state-change", function () {
                this.app.workspaceManager.updateSaveMenuItem();
                this.updateHeader();
            }, this);

            fileEditor.on("dispatch-command", function (id) {
                this.app.commandManager.dispatch(id);
            }, this);

            // bind app commands to source editor commands
            this.app.commandManager.getCommands().forEach(function(command){
                fileEditor.getSourceView().bindCommand(command);
            });
        },

        updateHeader: function(){
            if (this._file.isDirty()) {
                this.getHeader().setText('* ' + this.getTitle());
            } else {
                this.getHeader().setText(this.getTitle());
            }
        },

        createEmptyBallerinaRoot: function() {

            var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
            var ballerinaAstRoot = BallerinaASTFactory.createBallerinaAstRoot();

            //package definition
            var packageDefinition = BallerinaASTFactory.createPackageDefinition();
            packageDefinition.setPackageName("");
            //packageDefinition.setPackageName("samples.echo");
            ballerinaAstRoot.addChild(packageDefinition);
            ballerinaAstRoot.setPackageDefinition(packageDefinition);

            return ballerinaAstRoot;
        },

        getBallerinaFileEditor: function () {
            return this._fileEditor;
        },

        removeAllBreakpoints: function() {
            DebugManager.removeAllBreakpoints(this._file.getName());
        }

    });

    return FileTab;
});
