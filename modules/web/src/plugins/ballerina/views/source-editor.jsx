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
import _ from 'lodash';
import { getServiceEndpoint } from 'api-client/api-client';
import { listen } from 'vscode-ws-jsonrpc';
import {
    BaseLanguageClient, CloseAction, ErrorAction,
    createMonacoServices, createConnection,
} from 'monaco-languageclient';
import ReconnectingWebSocket from 'reconnecting-websocket';
import debuggerHoc from 'src/plugins/debugger/views/DebuggerHoc';
import File from 'core/workspace/model/file';
import { PLUGIN_ID as BAL_PLUGIN_ID } from 'plugins/ballerina/constants';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import { CONTENT_MODIFIED } from 'plugins/ballerina/constants/events';
import { GO_TO_POSITION } from 'plugins/ballerina/constants/commands';
import MonacoEditor from 'react-monaco-editor';
import SourceViewCompleterFactory from './../../ballerina/utils/source-view-completer-factory';
import { CHANGE_EVT_TYPES } from './constants';
import Grammar from './../utils/monarch-grammar';

const BAL_LANGUAGE = 'ballerina-lang';

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
    }

    /**
     * @inheritDoc
     */
    componentWillReceiveProps(nextProps) {
        // if (!nextProps.parseFailed) {
        //     getLangServerClientInstance()
        //         .then((langserverClient) => {
        //             // Set source view completer
        //             const sourceViewCompleterFactory = this.sourceViewCompleterFactory;
        //             const fileData = {
        //                 fileName: nextProps.file.name,
        //                 filePath: nextProps.file.path,
        //                 fullPath: nextProps.file.fullPath,
        //                 packageName: nextProps.file.packageName,
        //             };
        //             const completer = sourceViewCompleterFactory.getSourceViewCompleter(langserverClient, fileData);
        //             langTools.setCompleters(completer);
        //         })
        //         .catch(error => log.error(error));
        // }

        // const { debugHit, sourceViewBreakpoints } = nextProps;
        // if (this.debugPointMarker) {
        //     this.editor.getSession().removeMarker(this.debugPointMarker);
        // }
        // if (debugHit > 0) {
        //     this.debugPointMarker = this.editor.getSession().addMarker(
        //         new Range(debugHit, 0, debugHit, 2000), 'debug-point-hit', 'line', true);
        // }

        if (this.props.file.id !== nextProps.file.id) {
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
        // this.editor.getSession().setBreakpoints(sourceViewBreakpoints);
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        this.props.commandProxy.off(WORKSPACE_EVENTS.FILE_CLOSED, this.onWorkspaceFileClose);
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
        const uri = monaco.Uri.parse(this.props.file.toURI());
        let modelForFile = monaco.editor.getModel(uri);
        if (!modelForFile) {
            modelForFile = monaco.editor.createModel(this.props.file.content, BAL_LANGUAGE, uri);
        }
        editorInstance.setModel(modelForFile);
        this.props.commandProxy.on(WORKSPACE_EVENTS.FILE_CLOSED, this.onWorkspaceFileClose);
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
                const disposable = languageClient.start();
                connection.onClose(() => disposable.dispose());
            });
    }

    /**
     * Go to given position command handler.
     *
     * @param {Object} args
     * @param {File} args.file File
     * @param {number} args.row Line number
     * @param {number} args.column Offset
     */
    handleGoToPosition(args) {
        if (this.props.file.id === args.file.id) {
            this.goToCursorPosition(args.row, args.column);
        }
    }

    /**
     * Set cursor of the source editor to a
     * specific position.
     *
     * @param {number} row Line Number
     * @param {number} column Offset
     */
    goToCursorPosition(row, column) {
        this.editor.focus();
        this.editor.gotoLine(row + 1, column);
    }

    /**
     * Replace content of the editor while maintaining history
     *
     * @param {*} newContent content to insert
     */
    replaceContent(newContent, skipFileUpdate) {
        if (skipFileUpdate) {
            this.skipFileUpdate = true;
        }
        this.monaco.editor.getModels()[1].setValue(newContent);
    }

    /**
     * Binds a shortcut to ace editor so that it will trigger the command on source view upon key press.
     * All the commands registered app's command manager will be bound to source view upon render.
     *
     * @param command {Object}
     * @param command.id {String} Id of the command to dispatch
     * @param command.shortcuts {Object}
     * @param command.shortcuts.mac {Object}
     * @param command.shortcuts.mac.key {String} key combination for mac platform eg. 'Command+N'
     * @param command.shortcuts.other {Object}
     * @param command.shortcuts.other.key {String} key combination for other platforms eg. 'Ctrl+N'
     */
    bindCommand(command) {
        const { id, argTypes, shortcut } = command;
        const { dispatch } = this.props.commandProxy;
        if (shortcut) {
            const shortcutKey = _.replace(shortcut.derived.key, '+', '-');
            this.editor.commands.addCommand({
                name: id,
                bindKey: { win: shortcutKey, mac: shortcutKey },
                exec() {
                    dispatch(id, argTypes);
                },
            });
        }
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
                        contextmenu: false,
                        renderIndentGuides: true,
                        autoClosingBrackets: true,
                        automaticLayout: true,
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
    parseFailed: PropTypes.bool.isRequired,
    onLintErrors: PropTypes.func,
    sourceViewBreakpoints: PropTypes.arrayOf(Number).isRequired,
    addBreakpoint: PropTypes.func.isRequired,
    removeBreakpoint: PropTypes.func.isRequired,
    debugHit: PropTypes.number,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

SourceEditor.defaultProps = {
    debugHit: null,
    onLintErrors: () => {},
};

export default debuggerHoc(SourceEditor);
