import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS } from './../constants';
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
    }

    /**
     * Set Active editor
     * @param {Editor} editor tab instanc
     */
    setActiveEditor(editor) {
        if (_.isNil(editor)) {
            this.forceUpdate();
            return;
        }
        this.props.editorPlugin.setActiveEditor(editor);
    }

    /**
     * On Tab Close
     * @param {Editor} targetEditor Editor instance
     */
    onTabClose(targetEditor) {
        this.props.editorPlugin.onTabClose(targetEditor);
    }

    /**
     * Make an editor tab
     * @param {Editor} editor Editor tab
     */
    makeTabPane(editor) {
        const { activeEditorID } = this.props.editorPlugin;
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
                        isActive={activeEditorID === file.fullPath}
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
                        isActive={activeEditorID === id}
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
        const { activeEditorID, openedEditors } = this.props.editorPlugin;
        const tabs = [];
        openedEditors.forEach((editor) => {
            tabs.push(this.makeTabPane(editor));
        });

        return (
            <div className="editor-area" >
                <Tabs
                    activeKey={activeEditorID}
                    onChange={(key) => {
                        const editor = _.find(openedEditors, ['id', key]);
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
