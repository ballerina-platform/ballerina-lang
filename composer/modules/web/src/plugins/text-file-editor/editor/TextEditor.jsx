import React from 'react';
import PropTypes from 'prop-types';
import File from 'core/workspace/model/file';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import { getMonacoKeyBinding } from 'plugins/ballerina/utils/monaco-key-utils';
import { Dimmer, Loader } from 'semantic-ui-react';
import MonacoEditor from 'react-monaco-editor';
import { EVENTS as EDITOR_EVENTS } from 'core/editor/constants';
import { withUndoRedoSupport } from 'core/editor/views/utils';
import TextEditorUndoableOperation from './TextEditorUndoableOperation';
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
        this.onFileContentModified = this.onFileContentModified.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * On File Modifications
     */
    onFileContentModified(changeEvent) {
        if (changeEvent.originEvt.type !== EDITOR_EVENTS.UNDO_EVENT
            && changeEvent.originEvt.type !== EDITOR_EVENTS.REDO_EVENT) {
            const undoableOp = new TextEditorUndoableOperation({
                file: this.props.file,
                changeEvent,
                monacoEditorInstance: this.editorInstance,
                textEditorPlugin: this.props.textEditorPlugin,
            });
            this.props.onUndoableOperation(undoableOp);
        }
    }

    /**
     * Life-cycle hook for editor did mount
     *
     * @param {IEditor} editorInstance Current editor instance
     * @param {Object} monaco Monaco API
     */
    editorDidMount(editorInstance, monaco) {
        this.editorInstance = editorInstance;
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
                    value={this.props.file.content}
                    editorWillMount={this.editorWillMount}
                    editorDidMount={this.editorDidMount}
                    onChange={(newValue) => {
                        const changeEvent = {
                            type: 'text-modified',
                            title: 'Modify Text',
                        };
                        this.props.file
                            .setContent(newValue, changeEvent);
                    }}
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

export default withUndoRedoSupport(TextEditor);
