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
import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import debuggerHoc from 'plugins/debugger/views/DebuggerHoc';
import File from 'core/workspace/model/file';
import { COMMANDS, EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import { CONTENT_MODIFIED } from 'plugins/ballerina/constants/events';
import { GO_TO_POSITION } from 'plugins/ballerina/constants/commands';
import MonacoEditor from 'react-monaco-editor';
import SourceViewCompleterFactory from './../../ballerina/utils/source-view-completer-factory';
import { CHANGE_EVT_TYPES } from './constants';
import { REDO_EVENT, UNDO_EVENT } from './../constants/events';
import Grammar from './../utils/monarch-grammar';
import { getMonacoKeyBinding } from '../utils/monaco-key-utils';
import BAL_LANG_CONFIG from '../utils/monaco-lang-config';

const BAL_LANGUAGE = 'ballerina-lang';

const webpackHash = process.env.NODE_ENV === 'production'
            || process.env.NODE_ENV === 'electron'
            ? __webpack_hash__ : __webpack_hash__();

self.MonacoEnvironment = {
    getWorkerUrl (moduleId, label) {
      if (label === 'json') {
        return `./workers/json.worker.bundle.js`;
      }
      if (label === 'css') {
        return `./workers/css.worker.bundle.js`;
      }
      if (label === 'html') {
        return `./workers/html.worker.bundle.js`;
      }
      if (label === 'typescript' || label === 'javascript') {
        return `./workers/ts.worker.bundle.js`;
      }
      return `./workers/editor.worker.bundle.js`;
    },
};

/**
 * Source editor component which wraps monaco editor
 */
class SourceEditor extends React.Component {

    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.monaco = undefined;
        this.editorInstance = undefined;
        this.inSilentMode = false;
        this.sourceViewCompleterFactory = new SourceViewCompleterFactory();
        this.goToCursorPosition = this.goToCursorPosition.bind(this);
        this.lastUpdatedTimestamp = props.file.lastUpdated;
        this.editorDidMount = this.editorDidMount.bind(this);
        this.onWorkspaceFileClose = this.onWorkspaceFileClose.bind(this);
        this.handleGoToPosition = this.handleGoToPosition.bind(this);
        this.preventChangeEvt = false;
    }

    /**
     * @inheritDoc
     */
    componentWillReceiveProps(nextProps) {
        const { debugHit, breakpoints } = nextProps;
        if (this.monaco && this.editorInstance) {
            const breakpointDecorations = [];
            breakpoints.forEach((breakpoint) => {
                breakpointDecorations.push({
                    range: new this.monaco.Range(breakpoint, 1, breakpoint, 1),
                    options: {
                        isWholeLine: false,
                        glyphMarginClassName: 'breakpoint',
                    },
                });
            });
            this.breakpointDecorations = this.editorInstance.deltaDecorations(this.breakpointDecorations || [],
                breakpointDecorations);

            if (debugHit) {
                this.debugHitDecorations = this.editorInstance.deltaDecorations(this.debugHitDecorations || [], [
                    {
                        range: new this.monaco.Range(debugHit, 1, debugHit, 1),
                        options: {
                            isWholeLine: true,
                            className: 'debug-hit',
                        },
                    },
                ]);
            } else {
                // clear existing debug hit
                this.debugHitDecorations = this.editorInstance.deltaDecorations(this.debugHitDecorations || [], []);
            }
        }
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        if (this.didChangeHandler) {
            this.didChangeHandler.dispose();
        }
        this.props.commandProxy.off(WORKSPACE_EVENTS.FILE_CLOSED, this.onWorkspaceFileClose);
        this.props.commandProxy.off(GO_TO_POSITION, this.handleGoToPosition);
    }

     /**
     * On File Close in workspce
     */
    onWorkspaceFileClose({ file }) {
        if (this.monaco) {
            const uri = this.monaco.Uri.parse(file.toURI());
            const modelForFile = this.monaco.editor.getModel(uri);
            if (modelForFile) {
                modelForFile.dispose();
            }
        }
    }

    shouldIgnoreChangeEvt() {
        return this.ignoreChangeEvt;
    }

    setContent(newContent, ignoreChangeEvt = false) {
        this.preventChangeEvt = ignoreChangeEvt;
        this.getCurrentModel()
            .pushEditOperations([],
            [{
                    range: this.getCurrentModel().getFullModelRange(),
                    text: newContent,
                    forceMoveMarkers: true,
                }],
                () => []);
        this.getCurrentModel().pushStackElement();
        this.preventChangeEvt = false;
    }

    getCurrentModel() {
        const uri = this.monaco.Uri.parse(this.props.file.toURI());
        return this.monaco.editor.getModel(uri);
    }

    /**
     * Life-cycle hook for editor did mount
     *
     * @param {IEditor} editorInstance Current editor instance
     * @param {Object} monaco Monaco API
     */
    editorDidMount(editorInstance, monaco) {
        this.context.setSourceEditorRef(this);
        this.monaco = monaco;
        this.editorInstance = editorInstance;
        monaco.languages.register({ id: BAL_LANGUAGE });
        monaco.languages.setMonarchTokensProvider(BAL_LANGUAGE, Grammar);
        monaco.languages.setLanguageConfiguration(BAL_LANGUAGE, BAL_LANG_CONFIG);
        const updateEditorModel = () => {
            const uri = monaco.Uri.parse(this.props.file.toURI());
            let modelForFile = monaco.editor.getModel(uri);
            if (!modelForFile) {
                modelForFile = monaco.editor.createModel(this.props.file.content, BAL_LANGUAGE, uri);
            }
            const debouncedSetContent = _.debounce(() => {
                const changeEvent = {
                    type: CHANGE_EVT_TYPES.SOURCE_MODIFIED,
                    title: 'Modify source',
                    data: {
                        sourceEditor: this,
                    },
                };
                this.props.file
                    .setContent(editorInstance.getValue(), changeEvent);
            },
            400);
            this.didChangeHandler = modelForFile.onDidChangeContent(({ changes, isRedoing, isUndoing }) => {
                if (this.shouldIgnoreChangeEvt()) {
                    return;
                }
                debouncedSetContent();
            });
            editorInstance.setModel(modelForFile);
            return modelForFile;
        };
        let modelForFile = updateEditorModel();
        this.props.file.on(WORKSPACE_EVENTS.FILE_PATH_CHANGED, () => {
            const newModelForFile = updateEditorModel();
            modelForFile.dispose();
            modelForFile = newModelForFile;
        });
        this.props.commandProxy.on(WORKSPACE_EVENTS.FILE_CLOSED, this.onWorkspaceFileClose);
        this.props.commandProxy.on(GO_TO_POSITION, this.handleGoToPosition);
    
        const getLangServerConnection = this.props.ballerinaPlugin.getLangServerConnection();
        getLangServerConnection
            .then((connection) => {
                this.props.ballerinaPlugin.lsCommands.forEach((cmd) => {
                    editorInstance._commandService.addCommand({
                        id: cmd.command,
                        handler: (_accessor, ...args) => cmd.callback(...args),
                    });
                });
                editorInstance.onMouseDown((e) => {
                    if (e.event.ctrlKey) {
                        e.event.preventDefault();
                        connection.sendRequest('textDocument/definition', {
                            textDocument: {
                                uri: this.props.file.toURI().toString(),
                            },
                            position: {
                                line: e.target.position.lineNumber - 1,
                                character: e.target.position.column - 1,
                            },
                        })
                        .then((result) => {
                            if (result[0] && result[0].uri && result[0].range) {
                                this.props.commandProxy.dispatch(COMMANDS.OPEN_FILE, {
                                    filePath: monaco.Uri.parse(result[0].uri).path,
                                });

                                this.props.commandProxy.dispatch(COMMANDS.GO_TO_POSITION, {
                                    row: result[0].range.start.line,
                                    column: result[0].range.start.character,
                                });
                            }
                        });
                    }
                });
            });
        this.bindCommands();
        editorInstance.onMouseDown((e) => {
            if (e.target.type === monaco.editor.MouseTargetType.GUTTER_GLYPH_MARGIN) {
                if (e.target.element.classList.contains('breakpoint')) {
                    this.props.removeBreakpoint(e.target.position.lineNumber);
                } else {
                    this.props.addBreakpoint(e.target.position.lineNumber);
                }
            }
        });
        editorInstance.onMouseMove((e) => {
            // clean up previous decorations
            this.breakpointHoverDecorations = this.editorInstance.deltaDecorations(
                this.breakpointHoverDecorations || [], []);

            if (e.target.type === monaco.editor.MouseTargetType.GUTTER_GLYPH_MARGIN) {
                if (!e.target.element.classList.contains('breakpoint')) {
                    this.breakpointHoverDecorations = this.editorInstance.deltaDecorations([], [
                        {
                            range: new this.monaco.Range(
                                e.target.position.lineNumber, 1, e.target.position.lineNumber, 1),
                            options: {
                                isWholeLine: false,
                                glyphMarginClassName: 'breakpoint-mouse-over',
                            },
                        },
                    ]);
                }
            }
        });

        editorInstance.onMouseLeave((e) => {
            // clean up previous decorations
            this.breakpointHoverDecorations = this.editorInstance.deltaDecorations(
                this.breakpointHoverDecorations || [], []);
        });

    }

    /**
     * Go to given position command handler.
     *
     * @param {Object} args
     * @param {File} args.file File
     * @param {number} args.row Line number
     * @param {number} args.column Offset
     * @param {boolean} args.isPreviewViewEnabled indicates whether the privew is on
     */
    handleGoToPosition(args) {
        if (this.props.file.toURI() === args.file.toURI()
            && ((this.props.isUsedAsPreviewComponent && args.isPreviewViewEnabled)
                || !args.isPreviewViewEnabled)) {
            this.goToCursorPosition(args.row + 1, args.column + 1);
        }
    }

    /**
     * Set cursor of the source editor to a
     * specific position.
     *
     * @param {number} lineNumber Line Number (1 based)
     * @param {number} column Offset (1 based)
     */
    goToCursorPosition(lineNumber, column) {
        const position = {
            lineNumber,
            column,
        };
        this.editorInstance.focus();
        this.editorInstance.setPosition(position);
        this.editorInstance.revealPositionInCenter(position);
    }

    /**
     * Binds available commands to monaco editor
     */
    bindCommands() {
        const commands = this.props.commandProxy.getCommands();
        commands.forEach((command) => {
            if (command.shortcut) {
                const shortcut = command.shortcut.derived.key;
                const monacoKeyBinding = getMonacoKeyBinding(shortcut);
                this.editorInstance.addCommand(monacoKeyBinding, () => {
                    this.props.commandProxy.dispatch(command.id, {});
                });
            }
        });
    }

    undo(title) {
        this.preventChangeEvt = true;
        this.editorInstance.trigger(title, 'undo');
        this.preventChangeEvt = false;
    }

    redo(title) {
        this.preventChangeEvt = true;
        this.editorInstance.trigger(title, 'redo');
        this.preventChangeEvt = false;
    }

    /**
     * @inheritDoc
     */
    render() {
        const { width, height } = this.props;
        return (
            <div className='text-editor bal-source-editor'>
                <MonacoEditor
                    language='ballerinalang'
                    theme='vs-dark'
                    editorDidMount={this.editorDidMount}
                    options={{
                        autoIndent: true,
                        fontSize: 14,
                        contextmenu: true,
                        renderIndentGuides: true,
                        autoClosingBrackets: true,
                        matchBrackets: true,
                        automaticLayout: true,
                        glyphMargin: true,
                        folding: true,
                        lineNumbersMinChars: 2,
                        scrollBeyondLastLine: false,
                        scrollbar: {
                            useShadows: false,
                            verticalScrollbarSize: 6,
                            horizontalScrollbarSize: 6,
                        },
                    }}
                    width={width}
                    height={height}
                />
            </div>
        );
    }
}

SourceEditor.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
        off: PropTypes.func.isRequired,
    }).isRequired,
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
    breakpoints: PropTypes.arrayOf(Number).isRequired,
    addBreakpoint: PropTypes.func.isRequired,
    removeBreakpoint: PropTypes.func.isRequired,
    debugHit: PropTypes.number,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    isUsedAsPreviewComponent: PropTypes.bool,
};

SourceEditor.contextTypes = {
    setSourceEditorRef: PropTypes.func.isRequired,
};

SourceEditor.defaultProps = {
    debugHit: null,
    onLintErrors: () => {},
    isUsedAsPreviewComponent: false,
};

export default debuggerHoc(SourceEditor);
