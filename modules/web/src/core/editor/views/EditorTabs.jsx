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
import { VIEWS, HISTORY } from './../constants';
import Editor from './../model/Editor';
import CustomEditor from './../model/CustomEditor';
import EditorTabTitle from './EditorTabTitle';
import { withUndoRedoSupport } from './utils';

const DEFAULT_PREVIEW_VIEW_SIZE = 500;
const MINIMUM_PREVIEW_VIEW_SIZE = 250;
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
        this.onPreviewViewTabClose = this.onPreviewViewTabClose.bind(this);
        this.previewSplitRef = undefined;

        const { history } = props.editorPlugin.appContext.pref;
        const previewViewEnabled = !_.isNil(history.get(HISTORY.ACTIVE_PREVIEW_VIEW));

        // Setting view states.
        this.state = {
            previewViewEnabled,
        };

        // Binding commands.
        props.editorPlugin.appContext.command.on('show-split-view', (enabled) => {
            if (enabled) {
                history.put(HISTORY.ACTIVE_PREVIEW_VIEW, true);
            } else {
                history.put(HISTORY.ACTIVE_PREVIEW_VIEW, undefined);
            }

            this.setState({
                previewViewEnabled: enabled,
            });

            this.props.editorPlugin.reRender();
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
     * Gets the size of the preview view.
     * @param {boolean} previewViewEnabled Whether preview view is enabled.
     * @returns {number|string} The preview view size.
     * @memberof EditorTabs
     */
    getPreviewViewSize(previewViewEnabled) {
        const { history } = this.props.editorPlugin.appContext.pref;
        let previewViewSize;
        if (previewViewEnabled) {
            if (history.get(HISTORY.PREVIEW_VIEW_ENABLED_SIZE)) {
                previewViewSize = parseInt(history.get(HISTORY.PREVIEW_VIEW_ENABLED_SIZE), 10);
            } else {
                previewViewSize = DEFAULT_PREVIEW_VIEW_SIZE;
            }
        } else {
            previewViewSize = '100%';
        }

        return previewViewSize;
    }

    /**
     * On split view button/icon is clicked.
     * @memberof EditorTabs
     */
    onPreviewViewTabClose() {
        this.props.editorPlugin.appContext.command.dispatch('show-split-view', false);
        this.setState({
            previewViewEnabled: false,
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
    * Update left panel state
    * @param {boolean} showLeftPanel
    * @param {number} leftPanelSize
    */
    setPreviewViewState(previewViewSize) {
        const { history } = this.props.editorPlugin.appContext.pref;
        if (_.isNil(previewViewSize)) {
            previewViewSize = this.getPreviewViewSize(true);
        }
        history.put(HISTORY.PREVIEW_VIEW_ENABLED_SIZE, previewViewSize);
        this.forceUpdate();
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
                        <EditorTabTitle
                            editor={editor}
                            onTabClose={this.onTabClose}
                            customClass={editor.customTitleClass}
                        />
                    }
                    data-extra="tabpane"
                    key={file.fullPath}
                >
                    <definition.component
                        editorModel={editor}
                        isActive={activeEditorID === file.fullPath}
                        file={file}
                        commandProxy={this.props.editorPlugin.appContext.command}
                        {...customPropsProvider()}
                        isPreviewViewEnabled={this.state.previewViewEnabled}
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
        } else {
            return (null);
        }
    }

    /**
     * Creates the preview tab.
     * @returns {ReactElement} The view.
     * @memberof EditorTabs
     */
    renderPreviewTab() {
        const editor = this.props.editorPlugin.appContext.editor.getActiveEditor();
        if (!_.isNil(editor) && !(editor instanceof CustomEditor)) {
            const { file, definition } = editor;
            const previewDefinition = definition.previewView;
            return (
                <TabPane
                    tab={
                        <EditorTabTitle editor={editor} onTabClose={this.onPreviewViewTabClose} />
                    }
                    data-extra="tabpane"
                    key='preview-view'
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

        const previewTab = this.renderPreviewTab();
        return (<div className="editor-area">
            <SplitPane
                ref={(ref) => { this.previewSplitRef = ref; }}
                split="vertical"
                minSize={this.state.previewViewEnabled ? MINIMUM_PREVIEW_VIEW_SIZE : 0}
                defaultSize={this.getPreviewViewSize(this.state.previewViewEnabled)}
                onDragFinished={(previewViewSize) => {
                    this.setPreviewViewState(previewViewSize);
                    if (!_.isNil(this.previewSplitRef)) {
                        this.previewSplitRef.setState({
                            resized: false,
                            draggedSize: undefined,
                        });
                    }
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
                <Tabs
                    activeKey='preview-view'
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
