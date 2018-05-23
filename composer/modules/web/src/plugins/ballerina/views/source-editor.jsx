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
import React from 'react';
import PropTypes from 'prop-types';
import {
    BaseLanguageClient, CloseAction, ErrorAction,
    createMonacoServices, createConnection,
} from 'monaco-languageclient';
import debuggerHoc from 'src/plugins/debugger/views/DebuggerHoc';
import File from 'core/workspace/model/file';
import { COMMANDS, EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import { CONTENT_MODIFIED } from 'plugins/ballerina/constants/events';
import { GO_TO_POSITION } from 'plugins/ballerina/constants/commands';
import MonacoEditor from 'react-monaco-editor';
import SourceViewCompleterFactory from './../../ballerina/utils/source-view-completer-factory';
import { CHANGE_EVT_TYPES } from './constants';
import Grammar from './../utils/monarch-grammar';
import { getMonacoKeyBinding } from '../utils/monaco-key-utils';
import BAL_LANG_CONFIG from '../utils/monaco-lang-config';

const BAL_LANGUAGE = 'ballerina-lang';

const webpackHash = process.env.NODE_ENV === 'production' ? __webpack_hash__ : __webpack_hash__();

self.MonacoEnvironment = {
    getWorkerUrl: function (moduleId, label) {
      if (label === 'json') {
        return `./json.worker-${webpackHash}.js`;
      }
      if (label === 'css') {
        return `./css.worker-${webpackHash}.js`;
      }
      if (label === 'html') {
        return `./html.worker-${webpackHash}.js`;
      }
      if (label === 'typescript' || label === 'javascript') {
        return `./ts.worker-${webpackHash}.js`;
      }
      return `./editor.worker-${webpackHash}.js`;
    }
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
        this.onFileContentChanged = this.onFileContentChanged.bind(this);
        this.lastUpdatedTimestamp = props.file.lastUpdated;
        this.editorDidMount = this.editorDidMount.bind(this);
        this.onWorkspaceFileClose = this.onWorkspaceFileClose.bind(this);
        this.handleGoToPosition = this.handleGoToPosition.bind(this);
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

        if (this.props.file.toURI() !== nextProps.file.toURI()) {
            if (this.monaco && this.editorInstance) {
                const uri = this.monaco.Uri.parse(nextProps.file.toURI());
                let modelForFile = this.monaco.editor.getModel(uri);
                const currentModel = this.editorInstance.getModel();
                if (!modelForFile) {
                    modelForFile = this.monaco.editor.createModel(nextProps.file.content, BAL_LANGUAGE, uri);
                }
                if (currentModel && modelForFile && currentModel.uri !== modelForFile.uri) {
                    this.editorInstance.setModel(modelForFile);
                }
            }
            // Removing the file content changed event of the previous file.
            this.props.file.off(CONTENT_MODIFIED, this.onFileContentChanged);
            // Adding the file content changed event to the new file.
            nextProps.file.on(CONTENT_MODIFIED, this.onFileContentChanged);
        }
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
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

    /**
     * Event handler when the content of the file object is changed.
     * @param {Object} evt The event object.
     * @memberof SourceEditor
     */
    onFileContentChanged(evt) {
        if (evt.originEvt.type !== CHANGE_EVT_TYPES.SOURCE_MODIFIED) {
            if (this.monaco && this.editorInstance && evt.file) {
                const uri = this.monaco.Uri.parse(evt.file.toURI());
                const modelForFile = this.monaco.editor.getModel(uri);
                if (modelForFile) {
                    modelForFile.setValue(evt.file.content);
                }
            }
        }
    }

    /**
     * Life-cycle hook for editor did mount
     *
     * @param {IEditor} editorInstance Current editor instance
     * @param {Object} monaco Monaco API
     */
    editorDidMount(editorInstance, monaco) {
        this.monaco = monaco;
        this.editorInstance = editorInstance;
        monaco.languages.register({ id: BAL_LANGUAGE });
        monaco.languages.setMonarchTokensProvider(BAL_LANGUAGE, Grammar);
        monaco.languages.setLanguageConfiguration(BAL_LANGUAGE, BAL_LANG_CONFIG);
        const uri = monaco.Uri.parse(this.props.file.toURI());
        let modelForFile = monaco.editor.getModel(uri);
        if (!modelForFile) {
            modelForFile = monaco.editor.createModel(this.props.file.content, BAL_LANGUAGE, uri);
        }
        editorInstance.setModel(modelForFile);
        this.props.commandProxy.on(WORKSPACE_EVENTS.FILE_CLOSED, this.onWorkspaceFileClose);
        this.props.commandProxy.on(GO_TO_POSITION, this.handleGoToPosition);
        const services = createMonacoServices(editorInstance);
        const getLangServerConnection = this.props.ballerinaPlugin.getLangServerConnection();
        getLangServerConnection
            .then((connection) => {
                // create and start the language client
                const languageClient = new BaseLanguageClient({
                    name: 'Ballerina Language Client',
                    clientOptions: {
                        // use a language id as a document selector
                        documentSelector: [BAL_LANGUAGE],
                        // disable the default error handler
                        errorHandler: {
                            error: () => ErrorAction.Continue,
                            closed: () => CloseAction.DoNotRestart,
                        },
                    },
                    services,
                    // create a language client connection from the JSON RPC connection on demand
                    connectionProvider: {
                        get: (errorHandler, closeHandler) => {
                            return Promise.resolve(createConnection(connection, errorHandler, closeHandler));
                        },
                    },
                });

                editorInstance.onMouseDown((e) => {
                    if (e.event.ctrlKey) {
                        e.event.preventDefault();
                        connection.sendRequest('textDocument/definition', {
                            textDocument: {
                                uri: uri.toString(),
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

                const disposable = languageClient.start();
                connection.onClose(() => disposable.dispose());
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
                    value={this.props.file.content}
                    editorDidMount={this.editorDidMount}
                    onChange={(newValue) => {
                        const changeEvent = {
                            type: CHANGE_EVT_TYPES.SOURCE_MODIFIED,
                            title: 'Modify source',
                        };
                        this.props.file
                            .setContent(newValue, changeEvent);
                    }}
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

SourceEditor.defaultProps = {
    debugHit: null,
    onLintErrors: () => {},
    isUsedAsPreviewComponent: false,
};

export default debuggerHoc(SourceEditor);
