/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';
import $ from 'jquery';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
const ace = global.ace;

/**
 * The annotation editor shown in the design view
 */
class AnnotationView {
    /**
     * Initializes the annotation view.
     * @param {object} args The arguments object to create the annotation view.
     * @param {DiagramRenderContext} args.diagramRenderingContext The diagram rending context.
     * @param {Element} args.viewPrependElement The element to which the annotation view should be appended to.
     * @param {Element} args.positionElement The element to which the annotation view should be positions to.
     */
    constructor (args) {
        this._astNode = _.get(args, 'astNode');
        this._diagramRenderingContext = _.get(args, 'diagramRenderingContext');
        this._viewPrependElement = _.get(args, 'viewPrependElement');
        this._postitioningElement = _.get(args, 'positioningElement');
    }

    /**
     * Renders the annotation view. By default the view is hidden.
     */
    render() {
        let ballerinaFileEditor = this._diagramRenderingContext.ballerinaFileEditor;
        let annotationEditorDiv;
        if (_.isUndefined(this._postitionArgs)) {
            annotationEditorDiv = $('<div/>').prependTo(this._viewPrependElement);
        } else {
            annotationEditorDiv = $('<div/>').appendTo(this._viewPrependElement);
        }

        this._editor = ace.edit(annotationEditorDiv.get(0));
        this._editor.getSession().setMode('ace/mode/ballerina');
        this._editor.$blockScrolling = Infinity;
        let editorThemeName = 'ace/theme/chrome';
        //ballerinaFileEditor.getFile()._storage.get('pref:sourceViewTheme') !== null || _.get(ballerinaFileEditor.getViewOptions().source_view, 'theme');
        let editorFontSize = ballerinaFileEditor.getFile()._storage.get('pref:sourceViewFontSize') !== null || _.get(ballerinaFileEditor.getViewOptions().source_view, 'font_size');

        let editorTheme = ace.acequire(editorThemeName);

        this._editor.setTheme(editorTheme);
        this._editor.setFontSize(editorFontSize);
        this._editor.setOptions({
            enableBasicAutocompletion:true
        });
        this._editor.setBehavioursEnabled(true);

        // Hiding line numbers
        this._editor.renderer.setOption('showLineNumbers', false);

        this.hideEditor();
    }

    /**
     * Sets the annotation values to the editor.
     */
    setEditorValue() {
        let sourcePrefix = '';
        let annotationSource = '';
        let sourceSuffix = '';
        let annotationASTs = this._astNode.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let ballerinaFileEditor = this._diagramRenderingContext.ballerinaFileEditor;
        let generatedSource = ballerinaFileEditor.generateSource();
        let response = ballerinaFileEditor.getModelFromSource(generatedSource);
        let sourceArray = generatedSource.split('\n');
        if (annotationASTs.length > 0) {
            let firstAnnotation = _.first(annotationASTs);
            let lastAnnotation = _.last(annotationASTs);

            let lineNumberOfFirstAnnotation = 0;
            let lineNumberOfLastAnnotation = 0;
            if (this._astNode.getFactory().isBallerinaAstRoot(response)) {
                let pathVector = [];
                ballerinaFileEditor.getModel().getPathToNode(firstAnnotation, pathVector);
                let parsedFirstAnnotationNode = ballerinaFileEditor.getModel().getNodeByVector(response, pathVector);
                lineNumberOfFirstAnnotation = parsedFirstAnnotationNode.getLineNumber();

                pathVector = [];
                ballerinaFileEditor.getModel().getPathToNode(lastAnnotation, pathVector);
                let parsedLastAnnotationNode = ballerinaFileEditor.getModel().getNodeByVector(response, pathVector);
                lineNumberOfLastAnnotation = parsedLastAnnotationNode.getLineNumber();
            }

            for (let i = 0; i < sourceArray.length; i++) {
                if (i < lineNumberOfFirstAnnotation - 1) {
                    if (i === 0) {
                        sourcePrefix += sourceArray[i];
                    } else {
                        sourcePrefix += '\n' + sourceArray[i];
                    }
                } else if (i < lineNumberOfLastAnnotation) {
                    if (i === lineNumberOfFirstAnnotation - 1) {
                        annotationSource += sourceArray[i];
                    } else {
                        annotationSource += '\n' + sourceArray[i];
                    }
                } else {
                    if (i === lineNumberOfLastAnnotation) {
                        sourceSuffix += sourceArray[i];
                    } else {
                        sourceSuffix += '\n' + sourceArray[i];
                    }
                }
            }
        } else {
            let pathVector = [];
            ballerinaFileEditor.getModel().getPathToNode(this._astNode, pathVector);
            let parsedAstNode = ballerinaFileEditor.getModel().getNodeByVector(response, pathVector);
            for (let i = 0; i < sourceArray.length; i++) {
                if (i < parsedAstNode.getLineNumber() - 1) {
                    if (i === 0) {
                        sourcePrefix += sourceArray[i];
                    } else {
                        sourcePrefix += '\n' + sourceArray[i];
                    }
                } else {
                    if (i === parsedAstNode.getLineNumber()) {
                        sourceSuffix += sourceArray[i];
                    } else {
                        sourceSuffix += '\n' + sourceArray[i];
                    }
                }
            }
        }

        this._editor.setValue(annotationSource);
        this._editor.getSession().getUndoManager().markClean();
        this._editor.clearSelection();
        this._editor.navigateFileEnd();

        this._updateEditorHeight();

        let self = this;

        this._editor.getSession().on('change', () => {
            let newSource = sourcePrefix + self._editor.getValue() + sourceSuffix;
            let response = ballerinaFileEditor.getModelFromSource(newSource);
            if (!_.isUndefined(response) && self._astNode.getFactory().isBallerinaAstRoot(response)) {
                // Updating AST
                if (!self._editor.getSession().getUndoManager().isClean() && !_.isUndefined(response)) {
                    let pathVector = [];

                    ballerinaFileEditor.getModel().getPathToNode(self._astNode, pathVector);
                    let newAstNode = response.getNodeByVector(response, pathVector);

                    if (!_.isUndefined(newAstNode)) { // Removing old annotations.
                        let oldAnnotationASTs = self._astNode.getChildrenOfType(BallerinaASTFactory.isAnnotation);
                        _.forEach(oldAnnotationASTs, (annotationAST) => {
                            annotationAST.remove();
                        });

                        // Adding new annotations.
                        let newAnnotationASTs = newAstNode.getChildrenOfType(BallerinaASTFactory.isAnnotation);
                        _.forEach(newAnnotationASTs, (annotationAST, index) => {
                            self._astNode.addChild(annotationAST, index);
                        });
                    }
                }
            }

            // Updating height.
            this._updateEditorHeight();
        });
    }

    /**
     * Update the height of the editor depending on the content inside.
     * @private
     */
    _updateEditorHeight() {
        if (_.isUndefined(this._postitioningElement)) {
            // Updating height.
            let newHeight = (this._editor.getSession().getScreenLength() * this._editor.renderer.lineHeight)
                + this._editor.renderer.scrollBar.getWidth();
            $(this._editor.container).height(newHeight.toString() + 'px');
            // This call is required for the editor to fix all of
            // its inner structure for adapting to a change in size
            this._editor.resize();
        } else {
            // Updating height.
            let newHeight = (this._editor.getSession().getScreenLength() * this._editor.renderer.lineHeight)
                + this._editor.renderer.scrollBar.getWidth();
            $(this._editor.container).height(newHeight.toString() + 'px');
            // This call is required for the editor to fix all of
            // its inner structure for adapting to a change in size
            this._editor.resize();

            $(this._editor.container).css('top', this._postitioningElement.attr('y') - newHeight - 26);
        }

    }

    /**
     * Positions the editor with x, y and width.
     */
    positionEditor() {
        if (!_.isUndefined(this._postitioningElement)) {
            // Positioning the editor
            let editorContainer = $(this._editor.container);
            editorContainer.removeClass('annotation-editor-view').addClass('annotation-editor-view');
            editorContainer.css('left', parseFloat(this._postitioningElement.attr('x')) - 1);
            editorContainer.css('top', parseFloat(this._postitioningElement.attr('y')));
            editorContainer.css('width', parseFloat(this._postitioningElement.attr('width')) + 2);
        }
    }

    /**
     * Shows the editor.
     */
    showEditor() {
        this.positionEditor();
        $(this._editor.container).show();
        this.setEditorValue();
    }

    /**
     * Hides the editor.
     */
    hideEditor() {
        $(this._editor.container).hide();
    }
}

export default AnnotationView;
