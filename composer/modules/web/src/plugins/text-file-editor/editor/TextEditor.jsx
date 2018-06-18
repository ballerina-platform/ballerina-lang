import React from 'react';
import PropTypes from 'prop-types';
import File from 'core/workspace/model/file';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import { getMonacoKeyBinding } from 'plugins/ballerina/utils/monaco-key-utils';
import MonacoBasedUndoManager from 'plugins/ballerina/utils/monaco-based-undo-manager';
import { Dimmer, Loader } from 'semantic-ui-react';
import MonacoEditor from 'react-monaco-editor';
import './TextEditor.css';

const MONACO_OPTIONS = {
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
};

const webpackHash = process.env.NODE_ENV === 'production'
            || process.env.NODE_ENV === 'electron'
            ? __webpack_hash__ : __webpack_hash__();

self.MonacoEnvironment = {
    getWorkerUrl: function (moduleId, label) {
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
    }
};

const getLanguageForExt = function (ext) {
    let language = '';
    switch (ext) {
        case 'js' : language = 'javascript'; break;
        case 'py' : language = 'python'; break;
        case 'hbs' : language = 'handlebars'; break;
        case 'yml' : language = 'yaml'; break;
        case 'md' : language = 'markdown'; break;
        case 'sh' : language = 'powershell'; break;
        default: language = ext;
    }
    return language;
};

/**
 * Source editor component which wraps monaco editor
 */
class TextEditor extends React.Component {

    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.state = {
            editorMounted: false,
        };
        this.monaco = undefined;
        this.editorInstance = undefined;
        this.editorDidMount = this.editorDidMount.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.editorModel.undoManager = new MonacoBasedUndoManager(this);
        this.props.file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    getCurrentModel() {
        const uri = this.monaco.Uri.parse(this.props.file.toURI());
        return this.monaco.editor.getModel(uri);
    }

    undo(title) {
        this.editorInstance.trigger(title, 'undo');
    }

    redo(title) {
        this.editorInstance.trigger(title, 'redo');
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
        const language = getLanguageForExt(this.props.file.extension);
        const uri = monaco.Uri.parse(this.props.file.toURI());
        let modelForFile = monaco.editor.getModel(uri);
        if (!modelForFile) {
            modelForFile = monaco.editor.createModel(this.props.file.content, language, uri);
        }
        editorInstance.setModel(modelForFile);
        modelForFile.onDidChangeContent((evt) => {
            const changeEvent = {
                type: 'text-modified',
                title: 'Modify Text',
                data: {
                    sourceEditor: this,
                },
            };
            this.props.file
                .setContent(editorInstance.getValue(), changeEvent);
        });
        this.setState({
            editorMounted: true,
        });
        this.bindCommands();
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
     * @inheritdoc
     */
    componetWillUnmount() {
        this.props.file.off(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * @inheritDoc
     */
    render() {
        return (
            <div className='text-editor'>
                {!this.state.editorMounted &&
                    <Dimmer active inverted>
                        <Loader inverted />
                    </Dimmer>
                }
                <MonacoEditor
                    language={getLanguageForExt(this.props.file.extension)}
                    theme='vs-dark'
                    width={this.props.width}
                    height={this.props.height}
                    editorWillMount={this.editorWillMount}
                    editorDidMount={this.editorDidMount}
                    options={MONACO_OPTIONS}
                />
            </div>
        );
    }
}

TextEditor.propTypes = {
    editorModel: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.instanceOf(File).isRequired,
    textEditorPlugin: PropTypes.objectOf(Object).isRequired,
    isActive: PropTypes.func.isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
        off: PropTypes.func.isRequired,
    }).isRequired,
    isPreviewViewEnabled: PropTypes.bool,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    onUndoableOperation: PropTypes.func.isRequired,
};

TextEditor.defaultProps = {
    isPreviewViewEnabled: false,
};

export default TextEditor;
