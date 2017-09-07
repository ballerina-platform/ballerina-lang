import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS, HISTORY, COMMANDS } from './../constants';
import EditorTab from './../model/EditorTab';

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
        this.onTabClose = this.onTabClose.bind(this);
        this.onOpenFileInEditor = this.onOpenFileInEditor.bind(this);

        const { command, pref: { history } } = this.props.editorPlugin.appContext;

        this.state = {
            activeEditor: history.get(HISTORY.ACTIVE_EDITOR),
            openedEditors: [],
        };
        command.on(COMMANDS.OPEN_FILE_IN_EDITOR, this.onOpenFileInEditor);
    }

    /**
     * On command open-file-in-editor
     * @param {Object} command args
     */
    onOpenFileInEditor({ activateEditor, file, editor }) {
        const { openedEditors } = this.state;
        const editorTab = new EditorTab(file, editor);
        openedEditors.push(editorTab);
        if (activateEditor || _.isNil(this.state.activeEditor)) {
            this.setActiveEditor(editorTab.id);
        } else {
            this.forceUpdate();
        }
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
     * On Tab Close
     * @param {EditorTab} editorTab Editor tab instance
     */
    onTabClose(targetEditorTab) {
        const { workspace } = this.props.editorPlugin.appContext;
        const searchByID = editorTab => editorTab.id === targetEditorTab.id;
        const editorTabIndex = _.findIndex(this.state.openedEditors, searchByID);
        const newActiveEditorTabIndex = editorTabIndex > 0 ? editorTabIndex - 1 : 1;
        const newActiveEditorTabKey = !_.isNil(this.state.openedEditors[newActiveEditorTabIndex])
                                ? this.state.openedEditors[newActiveEditorTabIndex].id
                                : undefined;
        _.remove(this.state.openedEditors, searchByID);
        this.setActiveEditor(newActiveEditorTabKey);
        workspace.closeFile(targetEditorTab.file);
    }

    /**
     * Make an editor tab
     * @param {EditorTab} editorTab Editor tab
     */
    makeTabPane(editorTab) {
        const { file, editor, editor: { customPropsProvider } } = editorTab;
        return (
            <TabPane
                tab={
                    <div data-extra="tab-bar-title">
                        <i className="fw fw-ballerina tab-icon" />
                        {file.name}
                        <button
                            type="button"
                            className="close close-tab pull-right"
                            onClick={() => this.onTabClose(editorTab)}
                        >
                            ×
                        </button>
                    </div>
                }
                data-extra="tabpane"
                key={file.fullPath}
            >
                <editor.component
                    isActive={this.state.activeEditor === file.fullPath}
                    file={file}
                    commandProxy={this.props.editorPlugin.appContext.command}
                    {...customPropsProvider()}
                />
            </TabPane>
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        const tabs = [];
        this.state.openedEditors.forEach((editorTab) => {
            tabs.push(this.makeTabPane(editorTab));
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
                        <TabContent animated={false} />
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
