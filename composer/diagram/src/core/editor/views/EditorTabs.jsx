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
import { Scrollbars } from 'react-custom-scrollbars';
import Tabs, { TabPane } from 'rc-tabs';
import SplitPane from 'react-split-pane';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import { VIEWS, HISTORY } from './../constants';
import Editor from './../model/Editor';
import CustomEditor from './../model/CustomEditor';
import EditorTabTitle from './EditorTabTitle';
import ErrorBoundary from './ErrorBoundary';

const tabTitleHeight = 21;

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
        this.onPreviewTabClose = this.onPreviewTabClose.bind(this);
        this.previewSplitRef = undefined;

        const { history } = props.editorPlugin.appContext.pref;
        const previewPanelEnabledHistory = history.get(HISTORY.PREVIEW_VIEW_IS_ACTIVE);
        const previewPanelEnabled = !_.isNil(previewPanelEnabledHistory) ? previewPanelEnabledHistory : false;
        const previewPanelSize = previewPanelEnabled
                                ? (parseInt(history.get(HISTORY.PREVIEW_VIEW_PANEL_SIZE), 10)
                                        || this.props.width / 2)
                                : 0;

        // Setting view states.
        this.state = {
            panelResizeInProgress: false,
            previewPanelEnabled,
            previewPanelSize,
        };

        // Binding commands.
        props.editorPlugin.appContext.command.on('show-preview-panel', () => {
            this.setPreviewPanelState(true);
        });

        this.isTabActive = this.isTabActive.bind(this);
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
     * On split view close.
     * @memberof EditorTabs
     */
    onPreviewTabClose() {
        this.setPreviewPanelState(false);
    }

    /**
     * On Tab Close
     * @param {Editor} targetEditor Editor instance
     */
    onTabClose(targetEditor) {
        this.props.editorPlugin.onTabClose(targetEditor);
    }

    /**
    * Update preview panel state
    * @param {boolean} previewPanelEnabled
    * @param {number} previewPanelSize
    */
    setPreviewPanelState(previewPanelEnabled, previewPanelSize) {
        const { history } = this.props.editorPlugin.appContext.pref;
        if (_.isNil(previewPanelSize)) {
            const sizeFromHistory = history.get(HISTORY.PREVIEW_VIEW_PANEL_SIZE);
            previewPanelSize = !_.isNil(sizeFromHistory) && sizeFromHistory !== 0
                                    ? sizeFromHistory
                                    : this.props.width / 2;
        }
        history.put(HISTORY.PREVIEW_VIEW_IS_ACTIVE, previewPanelEnabled);
        history.put(HISTORY.PREVIEW_VIEW_PANEL_SIZE, previewPanelSize);
        this.setState({
            previewPanelEnabled,
            previewPanelSize,
        });
    }


    /**
     * Function to pass as a property to check if the tab active.
     *
     * @memberof EditorTabs
     */
    isTabActive(fileId) {
        const { activeEditorID } = this.props.editorPlugin;
        return activeEditorID === fileId;
    }

    /**
     * Make an editor tab
     * @param {Editor} editor Editor tab
     */
    makeTabPane(editor) {
        const { activeEditorID } = this.props.editorPlugin;
        const dimensions = {
            width: this.props.width -
                (this.state.previewPanelEnabled
                    ? this.state.previewPanelSize : 0),
            height: this.props.height - tabTitleHeight,
        };
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
                    data-extra='tabpane'
                    key={file.fullPath}
                >
                    <ErrorBoundary>
                        <definition.component
                            editorModel={editor}
                            isActive={() => { return this.isTabActive(file.fullPath); }}
                            file={file}
                            commandProxy={this.props.editorPlugin.appContext.command}
                            {...customPropsProvider()}
                            isPreviewViewEnabled={this.state.previewPanelEnabled}
                            {...dimensions}
                            panelResizeInProgress={this.props.panelResizeInProgress || this.state.panelResizeInProgress}
                        />
                    </ErrorBoundary>
                </TabPane>
            );
        } else if (editor instanceof CustomEditor) {
            const { id, title, icon, propsProvider, additionalProps } = editor;
            const customTabDimensions = {
                width: this.props.width, // custom tabs doesn't support split view hence full width
                height: this.props.height - tabTitleHeight,
            };
            const finalProps = {
                ...propsProvider(),
                ...additionalProps,
                ...customTabDimensions,
                isActive: () => { return this.isTabActive(id); },
                panelResizeInProgress: this.props.panelResizeInProgress || this.state.panelResizeInProgress,
            };
            return (
                <TabPane
                    tab={
                        <div data-extra='tab-bar-title' className={`tab-title-wrapper ${editor.customTitleClass}`}>
                            <button
                                type='button'
                                className='close close-tab pull-right'
                                onClick={(evt) => {
                                    this.onTabClose(editor);
                                    evt.stopPropagation();
                                    evt.preventDefault();
                                }}
                            >
                                ×
                            </button>
                            {icon ? <i className={`fw fw-${icon} tab-icon`} /> : ''}
                            {_.isFunction(title) ? title(finalProps) : title}
                        </div>
                    }
                    data-extra='tabpane'
                    key={id}
                >
                    <ErrorBoundary>
                        <Scrollbars
                            style={customTabDimensions}
                            autoHide // Hide delay in ms
                            autoHideTimeout={1000}
                        >
                            <editor.component
                                {...finalProps}
                            />
                        </Scrollbars>
                    </ErrorBoundary>
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
        const dimensions = {
            width: this.state.previewPanelSize,
            height: this.props.height - tabTitleHeight,
        };
        if (!_.isNil(editor) && !(editor instanceof CustomEditor) && editor.definition.previewView) {
            const { file, definition } = editor;
            const previewDefinition = definition.previewView;
            return (
                <TabPane
                    tab={
                        <EditorTabTitle editor={editor} onTabClose={this.onPreviewTabClose} />
                    }
                    data-extra='tabpane'
                    key='preview-view'
                >
                    <div className='editor-split-view' style={{ ...dimensions }}>
                        <previewDefinition.component
                            file={file}
                            commandProxy={this.props.editorPlugin.appContext.command}
                            {...previewDefinition.customPropsProvider()}
                            {...dimensions}
                            togglePreviewPanel={(isEnabled = !this.state.previewPanelEnabled) => {
                                this.setPreviewPanelState(isEnabled);
                            }
                            }
                            isUsedAsPreviewComponent
                        />
                        <div
                            className='bottom-right-controls-container split-view-controls-container'
                        >
                            <div className='view-split-view-btn btn-icon' onClick={this.onPreviewTabClose}>
                                <div className='bottom-label-icon-wrapper'>
                                    <i className='fw fw-code fw-inverse' />
                                </div>
                                <div className='bottom-view-label'>
                                    Close Split View
                                </div>
                            </div>
                        </div>
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
        const { activeEditorID, openedEditors, activeEditor } = this.props.editorPlugin;
        const tabs = [];
        openedEditors.forEach((editor) => {
            tabs.push(this.makeTabPane(editor));
        });

        const previewTab = this.renderPreviewTab();
        return (<div className='editor-area'>
            <SplitPane
                ref={(ref) => { this.previewSplitRef = ref; }}
                split='vertical'
                allowResize={this.state.previewPanelEnabled}
                minSize={
                    this.state.previewPanelEnabled && !(activeEditor instanceof CustomEditor)
                        ? this.props.width * 0.25
                        : 0
                }
                defaultSize={
                    this.state.previewPanelEnabled && !(activeEditor instanceof CustomEditor)
                        ? this.state.previewPanelSize
                        : 0
                }
                maxSize={this.props.width * 0.75}
                onChange={(previewPanelSize) => {
                    this.state.panelResizeInProgress = true;
                    this.setPreviewPanelState(true, previewPanelSize);
                }}
                onDragFinished={() => {
                    if (!_.isNil(this.previewSplitRef)) {
                        this.previewSplitRef.setState({
                            resized: false,
                            draggedSize: undefined,
                        });
                    }
                    this.setState({
                        panelResizeInProgress: false,
                    });
                }
                }
                primary='second'
                pane1Style={{
                    maxWidth: this.props.width -
                        (this.state.previewPanelEnabled && !(activeEditor instanceof CustomEditor)
                           ? this.state.previewPanelSize : 0),
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
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

EditorTabs.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default EditorTabs;
