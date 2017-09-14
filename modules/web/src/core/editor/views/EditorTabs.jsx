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
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import SplitPane from 'react-split-pane';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import TabBar from 'rc-tabs/lib/TabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS } from './../constants';
import Editor from './../model/Editor';
import CustomEditor from './../model/CustomEditor';
import EditorTabTitle from './EditorTabTitle';

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
        this.onSplitViewButtonClick = this.onSplitViewButtonClick.bind(this);
        this.editorArea = undefined;

        this.state = {
            previewViewEnabled: false,
        };

        this.props.editorPlugin.appContext.command.on('show-split-view', (enabled) => {
            this.setState({
                previewViewEnabled: enabled,
            });
        });
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
     * On split view button/icon is clicked.
     * @memberof EditorTabs
     */
    onSplitViewButtonClick() {
        this.props.editorPlugin.appContext.command.dispatch('show-split-view', !this.state.previewViewEnabled);
        this.setState({
            previewViewEnabled: !this.state.previewViewEnabled,
        });
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
                        <EditorTabTitle editor={editor} onTabClose={this.onTabClose} />
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
                        <div data-extra="tab-bar-title" className={`tab-title-wrapper ${editor.customTitleClass}`}>
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

    renderPreviewTab() {
        const editor = this.props.editorPlugin.appContext.editor.getActiveEditor();
        if (!_.isNil(editor)) {
            const { file, definition } = editor;
            const previewDefinition = definition.previewView;
            return (
                <TabPane
                    tab={
                        <EditorTabTitle editor={editor} onTabClose={this.onSplitViewButtonClick} />
                    }
                    data-extra="tabpane"
                    key='source-view'
                >
                    <div className='ballerina-editor'>
                        <previewDefinition.component
                            file={file}
                            commandProxy={this.props.editorPlugin.appContext.command}
                            {...previewDefinition.customPropsProvider()}
                        />
                    </div>
                </TabPane>
            );
        } else {
            return (null);
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

        const activeEditor = this.props.editorPlugin.appContext.editor.getActiveEditor();
        if (this.state.previewViewEnabled && !(activeEditor instanceof CustomEditor)) {
            const previewTab = this.renderPreviewTab();
            return (<div
                className="editor-area"
                ref={(ref) => {
                    this.editorArea = ref;
                }}
            >
                <SplitPane
                    split="vertical"
                    minSize={150}
                    defaultSize={this.editorArea.offsetWidth / 2}
                    onDragFinished={() => { this.forceUpdate(); }}
                >
                    <Tabs
                        onClick={this.onTabClick}
                        activeKey={activeEditorID}
                        onChange={(key) => {
                            const editor = _.find(openedEditors, ['id', key]);
                            this.setActiveEditor(editor);
                        }}
                        renderTabBar={() =>
                            (
                                <TabBar />
                            )
                        }
                        renderTabContent={() =>
                            <TabContent animated={false} />
                        }
                    >
                        {tabs}
                    </Tabs>
                    <Tabs
                        activeKey='source-view'
                        renderTabContent={() =>
                            <TabContent animated={false} />
                        }
                        renderTabBar={() =>
                            (
                                <ScrollableInkTabBar />
                            )
                        }
                    >
                        {previewTab}
                    </Tabs>
                </SplitPane>
            </div>);
        } else {
            return (
                <div
                    className="editor-area"
                    ref={(ref) => {
                        this.editorArea = ref;
                    }}
                >
                    <Tabs
                        onClick={this.onTabClick}
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
