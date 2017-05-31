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
import log from 'log';
import $ from 'jquery';
import _ from 'lodash';
import Tab from './tab';
import BallerinaFileEditor from 'ballerina/views/ballerina-file-editor';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import File from '../workspace/file';
import DiagramRenderContext from 'ballerina/diagram-render/diagram-render-context';
import Backend from 'ballerina/views/backend';
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import DebugManager from '../debugger/debug-manager';
import alerts from 'alerts';
import BallerinaEnvFactory from '../ballerina/env/ballerina-env-factory';
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
        if (_.has(options, 'parseResponse')) {
            this._parseResponse = _.get(options, 'parseResponse');
        }
        if(_.has(options, 'programPackages')){
            this._programPackages = _.get(options, 'programPackages');
        }else{
            this._programPackages = [];
        }
        //TODO convert Backend to a singleton
        this.app = options.application;
        this.parseBackend = new Backend({"url" : this.app.config.services.parser.endpoint});
        this.validateBackend = new Backend({"url" : this.app.config.services.validator.endpoint});
        this.deserializer = BallerinaASTDeserializer;
        this._file.setLangserverCallbacks({
            documentDidSaveNotification:  (options) => {
                this.app.langseverClientController.documentDidSaveNotification(options);
            }
        });
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
                var validateResponse = this.validateBackend.parse({name: this._file.getName(), path: this._file.getPath(), package: this._astRoot, content: this._file.getContent()});
                if (validateResponse.errors != undefined && !_.isEmpty(validateResponse.errors)) {
                    this.renderBallerinaEditor(this._parseResponse, true);
                    return;
                }
            }
            var parseResponse = this.parseBackend.parse({name: this._file.getName(), path: this._file.getPath(), package: this._astRoot, content: this._file.getContent()});
            //if no errors display the design.
            this.renderBallerinaEditor(parseResponse);
        } else if(!_.isNil(this._parseResponse)) {
            this.renderBallerinaEditor(this._parseResponse, false);
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
        // Send document open notification to the language server
        const docUri = this._file.isPersisted() ? this._file.getPath() : ('/temp/' + this._file.id);
        let documentOptions = {
            textDocument: {
                documentUri: docUri,
                languageId: 'ballerina',
                version: 1,
                text: this._file.getContent()
            }
        };
        this.app.langseverClientController.documentDidOpenNotification(documentOptions);
        $(this.app.config.tab_controller.tabs.tab.ballerina_editor.design_view.container).scrollTop(0);
    },

    renderBallerinaEditor: function(parseResponse, parseFailed){
        var self = this;
        var ballerinaEditorOptions = _.get(this.options, 'ballerina_editor');
        var backendEndpointsOptions = _.get(this.options, 'application.config.services');
        let renderingContextOpts = {application: this.options.application};
        var diagramRenderingContext = new DiagramRenderContext(renderingContextOpts);


        var astRoot = this.deserializer.getASTModel(parseResponse);

        var packages = parseResponse.packages;
        _.each(packages, (packageNode) => {
            var pckg = BallerinaEnvFactory.createPackage();
            pckg.initFromJson(packageNode);
            this._programPackages.push(pckg);
        });

        var fileEditor = new BallerinaFileEditor({
            model: astRoot,
            parseFailed: parseFailed,
            file: self._file,
            container: this.$el.get(0),
            viewOptions: ballerinaEditorOptions,
            backendEndpointsOptions: backendEndpointsOptions,
            debugger: DebugManager,
            programPackages: self._programPackages
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

        fileEditor.on('design-view-activated', () => {
            const breakpoints = fileEditor._sourceView.getBreakpoints() || [];
            fileEditor._showDesignViewBreakpoints(breakpoints);
        }, this);

        fileEditor.on('source-view-activated', () => {
            fileEditor._showSourceViewBreakPoints();
        });

        fileEditor.on('add-breakpoint', function(row){
            DebugManager.addBreakPoint(row, this._file.getName());
        }, this);

        fileEditor.on('remove-breakpoint', function(row){
            DebugManager.removeBreakPoint(row, this._file.getName());
        }, this);

        this.on('tab-removed', function() {
            const docUri = this._file.isPersisted() ? this._file.getPath() : ('/temp/' + this._file.id);
            // Send document closed notification to the language server
            let documentOptions = {
                textDocument: {
                    documentUri: docUri,
                    documentId: this._file.id
                }
            };
            this.app.langseverClientController.documentDidCloseNotification(documentOptions);
            this.removeAllBreakpoints();
        });

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

    /**
     * Re-render current ballerina file.
     */
    reRender: function(){
        if(this._fileEditor){
            this._fileEditor.reRender();
        }
    },

    createEmptyBallerinaRoot: function() {

        var ballerinaAstRoot = BallerinaASTFactory.createBallerinaAstRoot();

        // package definition
        var packageDefinition = BallerinaASTFactory.createPackageDefinition();
        packageDefinition.setPackageName("");
        // packageDefinition.setPackageName("samples.echo");
        ballerinaAstRoot.addChild(packageDefinition);
        ballerinaAstRoot.setPackageDefinition(packageDefinition);

        return ballerinaAstRoot;
    },

    getBallerinaFileEditor: function () {
        return this._fileEditor;
    },

    removeAllBreakpoints: function() {
        DebugManager.removeAllBreakpoints(this._file.getName());
    },

    getBreakPoints: function() {
        return DebugManager.getDebugPoints(this._file.getName());
    }

});

export default FileTab;