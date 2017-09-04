import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS, HISTORY, COMMANDS } from './../constants';

/**
 * Editor Tabs
 */
class EditorTabs extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.EXPLORER;
    }

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { command: { on }, pref: { history } } = this.props.editorPlugin.appContext;
        on(COMMANDS.OPEN_FILE_IN_EDITOR, (args) => {
            const { activateEditor } = args;
            const { openedEditors } = this.state;
            openedEditors.push(args);
            if (activateEditor || _.isNil(this.state.activeEditor)) {
                this.setActiveEditor(args.file.fullPath);
            } else {
                this.forceUpdate();
            }
        });
        this.state = {
            activeEditor: history.get(HISTORY.ACTIVE_EDITOR),
            openedEditors: [],
        };
    }

    /**
     * Set Active editor
     * @param {String} filePath Path
     */
    setActiveEditor(path) {
        const { pref: { history } } = this.props.editorPlugin.appContext;
        history.put(HISTORY.ACTIVE_EDITOR, path);
        this.state.activeEditor = path;
        this.forceUpdate();
    }

    /**
     * @inheritdoc
     */
    render() {
        const { workspace } = this.props.editorPlugin.appContext;
        const onTabClose = (targetFile) => {
            const searchByFilePath = ({ file }) => file.fullPath === targetFile.fullPath;
            const editorIndex = _.findIndex(this.state.openedEditors, searchByFilePath);
            const newEditorIndex = editorIndex > 0 ? editorIndex - 1 : 1;
            const newEditorKey = !_.isNil(this.state.openedEditors[newEditorIndex])
                                    ? this.state.openedEditors[newEditorIndex].file.fullPath
                                    : undefined;
            _.remove(this.state.openedEditors, searchByFilePath);
            this.setActiveEditor(newEditorKey);
            workspace.closeFile(targetFile);
        };
        const tabTitle = file => (
            <div data-extra="tab-bar-title">
                <i className="fw fw-ballerina tab-icon" />
                {file.name}
                <button
                    type="button"
                    className="close close-tab pull-right"
                    onClick={() => onTabClose(file)}
                >
                    ×
                </button>
            </div>
        );
        const makeTabPane = (file, editor) => {
            const { customPropsProvider } = editor;
            return (
                <TabPane tab={tabTitle(file)} data-extra="tabpane" key={file.fullPath} >
                    <editor.component
                        isActive={this.state.activeEditor === file.fullPath}
                        file={file}
                        commandProxy={this.props.editorPlugin.appContext.command}
                        {...customPropsProvider()}
                    />
                </TabPane>
            );
        };

        const tabs = [];
        this.state.openedEditors.forEach(({ file, editor }) => {
            tabs.push(makeTabPane(file, editor));
        });

        return (
            <div className="editor-area" >
                <Tabs
                    activeKey={this.state.activeEditor}
                    onChange={(key) => {
                        this.setActiveEditor(key);
                    }}
                    renderTabBar={() =>
                        (
                            <ScrollableInkTabBar />
                        )
                    }
                    renderTabContent={() =>
                        <TabContent />
                    }
                >
                    {tabs}
                </Tabs>
            </div>
        );
    }
}

EditorTabs.propTypes = {
    editorPlugin: PropTypes.objectOf(Object).isRequired,
};

EditorTabs.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default EditorTabs;
