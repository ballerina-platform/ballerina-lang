import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS, HISTORY, COMMANDS, EVENTS } from './../constants';
import Editor from './../model/Editor';
import CustomEditor from './../model/CustomEditor';

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
        this.onOpenCustomEditorTab = this.onOpenCustomEditorTab.bind(this);

        const { command, pref: { history } } = this.props.editorPlugin.appContext;

        this.state = {
            activeEditorID: history.get(HISTORY.ACTIVE_EDITOR),
            openedEditors: [],
        };
        command.on(COMMANDS.OPEN_FILE_IN_EDITOR, this.onOpenFileInEditor);
        command.on(COMMANDS.OPEN_CUSTOM_EDITOR_TAB, this.onOpenCustomEditorTab);
    }

    /**
     * On command open-custom-editor-tab
     * @param {Object} command args
     */
    onOpenCustomEditorTab(args) {
        const { id, title, icon, component, propsProvider } = args;
        this.state.openedEditors.push(new CustomEditor(id, title, icon, component, propsProvider));
        this.forceUpdate();
    }

    /**
     * On command open-file-in-editor
     * @param {Object} command args
     */
    onOpenFileInEditor({ activateEditor, file, editorDefinition }) {
        const { openedEditors } = this.state;
        const editor = new Editor(file, editorDefinition);
        openedEditors.push(editor);
        if (activateEditor || _.isNil(this.state.activeEditorID)) {
            this.setActiveEditor(editor);
        } else {
            this.forceUpdate();
        }
        editor.on(EVENTS.UPDATE_TAB_TITLE, () => {
            this.forceUpdate();
        });
    }

    /**
     * Set Active editor
     * @param {Editor} editor tab instanc
     */
    setActiveEditor(editor) {
        if (_.isNil(editor)) {
            return;
        }
        const { pref: { history } } = this.props.editorPlugin.appContext;
        history.put(HISTORY.ACTIVE_EDITOR, editor.id);
        this.state.activeEditorID = editor.id;
        this.forceUpdate();
        this.props.editorPlugin.setActiveEditor(editor);
    }

    /**
     * On Tab Close
     * @param {Editor} targetEditor Editor instance
     */
    onTabClose(targetEditor) {
        const { workspace } = this.props.editorPlugin.appContext;
        const searchByID = editor => editor.id === targetEditor.id;
        const editorIndex = _.findIndex(this.state.openedEditors, searchByID);
        const newActiveEditorIndex = editorIndex > 0 ? editorIndex - 1 : 1;
        const newActiveEditor = !_.isNil(this.state.openedEditors[newActiveEditorIndex])
                                ? this.state.openedEditors[newActiveEditorIndex]
                                : undefined;
        _.remove(this.state.openedEditors, searchByID);
        this.setActiveEditor(newActiveEditor);
        if (targetEditor instanceof Editor) {
            workspace.closeFile(targetEditor.file);
        }
    }

    /**
     * Make an editor tab
     * @param {Editor} editor Editor tab
     */
    makeTabPane(editor) {
        if (editor instanceof Editor) {
            const { file, definition, definition: { customPropsProvider } } = editor;
            return (
                <TabPane
                    tab={
                        <div data-extra="tab-bar-title" className={`tab-title-wrapper ${editor.customTitleClass}`}>
                            <i className="fw fw-ballerina tab-icon" />
                            {file.name}
                            <button
                                type="button"
                                className="close close-tab pull-right"
                                onClick={() => this.onTabClose(editor)}
                            >
                                ×
                            </button>
                        </div>
                    }
                    data-extra="tabpane"
                    key={file.fullPath}
                >
                    <definition.component
                        editorTab={editor}
                        isActive={this.state.activeEditorID === file.fullPath}
                        file={file}
                        commandProxy={this.props.editorPlugin.appContext.command}
                        {...customPropsProvider()}
                    />
                </TabPane>
            );
        } else if (editor instanceof CustomEditor) {
            const { id, title, icon, propsProvider } = editor;
            return (
                <TabPane
                    tab={
                        <div data-extra="tab-bar-title" className="tab-title-wrapper">
                            <i className={`fw fw-${icon} tab-icon`} />
                            {title}
                            <button
                                type="button"
                                className="close close-tab pull-right"
                                onClick={() => this.onTabClose(editor)}
                            >
                                ×
                            </button>
                        </div>
                    }
                    data-extra="tabpane"
                    key={id}
                >
                    <editor.component
                        isActive={this.state.activeEditorID === id}
                        {...propsProvider()}
                    />
                </TabPane>
            );
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        const tabs = [];
        this.state.openedEditors.forEach((editor) => {
            tabs.push(this.makeTabPane(editor));
        });

        return (
            <div className="editor-area" >
                <Tabs
                    activeKey={this.state.activeEditorID}
                    onChange={(key) => {
                        const editor = _.find(this.state.openedEditors, ['id', key]);
                        this.setActiveEditor(editor);
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
