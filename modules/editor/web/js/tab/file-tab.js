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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'workspace', 'ballerina/diagram-render/diagram-render-context'],
    function (require, log, jquery, _, Tab, Ballerina, Workspace, DiagramRenderContext) {
    var FileTab;

    FileTab = Tab.extend({
        initialize: function (options) {
            Tab.prototype.initialize.call(this, options);
            if (!_.has(options, 'file')) {
                this._file = new Workspace.File({isTemp: true}, {storage: this.getParent().getBrowserStorage()});
            } else {
                this._file = _.get(options, 'file');
            }
            if (_.has(options, 'astRoot')) {
                this._astRoot = _.get(options, 'astRoot');
            }
        },

        getFile: function () {
            return this._file;
        },

        render: function () {
            Tab.prototype.render.call(this);
            // if file already has content
            if(!_.isNil(this._file.getContent())){
                var workspaceManager = _.get(this, 'options.application.workspaceManager'),
                    self = this;
                workspaceManager.getParsedTree(this._file, function(astRoot){
                    self.renderAST(astRoot);
                })
            } else if(!_.isNil(this._astRoot)) {
                this.renderAST(this._astRoot);
                var updatedContent = this.getBallerinaFileEditor().generateSource();
                this._file.setContent(updatedContent);
                this._file.save();
            } else {
                this.renderAST(this.createEmptyBallerinaRoot());
                var updatedContent = this.getBallerinaFileEditor().generateSource();
                this._file.setContent(updatedContent);
                this._file.save();
            }
        },

        renderAST: function(astRoot){
            var ballerinaEditorOptions = _.get(this.options, 'ballerina_editor');
            var diagramRenderingContext = new DiagramRenderContext();

            var fileEditor = new Ballerina.views.BallerinaFileEditor({
                model: astRoot,
                container: this.$el.get(0),
                viewOptions: ballerinaEditorOptions
            });

            // change tab header class to match look and feel of source view
            fileEditor.on('source-view-activated', function(){
                this.getHeader().toggleClass('inverse');
            }, this);
            fileEditor.on('design-view-activated', function(){
                this.getHeader().toggleClass('inverse');
            }, this);

            this._fileEditor = fileEditor;
            fileEditor.render(diagramRenderingContext);

            fileEditor.on("content-modified", function(){
                this.trigger("tab-content-modified");
                var updatedContent = this.getBallerinaFileEditor().generateSource();
                this._file.setContent(updatedContent);
                this._file.save();
            }, this);
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

            //import declarations
            var importDeclaration_langSystem = BallerinaASTFactory.createImportDeclaration();
            importDeclaration_langSystem.setPackageName("ballerina.lang.system");
            importDeclaration_langSystem.setParent(ballerinaAstRoot);
            ballerinaAstRoot.addImport(importDeclaration_langSystem);

            return ballerinaAstRoot;
        },

        getBallerinaFileEditor: function () {
            return this._fileEditor;
        }

    });

    return FileTab;
});
