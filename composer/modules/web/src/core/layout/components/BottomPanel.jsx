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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import { HISTORY, COMMANDS } from './../constants';
import { createViewFromViewDef } from './utils';

const panelTitleHeight = 37;

/**
 * React component for BottomPanel Region.
 *
 * @class BottomPanel
 * @extends {React.Component}
 */
class BottomPanel extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props, context) {
        super(props, context);
        const defaultActiveView = (this.props.views[0] && this.props.views[0].id) || undefined;
        this.state = {
            maximized: this.props.maximize,
            activeView: context.history.get(HISTORY.ACTIVE_BOTTOM_PANEL_VIEW) || defaultActiveView,
        };
        this.updateActions = this.updateActions.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { on } = this.context.command;
        on(COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, this.updateActions);
    }

    /**
     * @inheritdoc
     */
    componentWillReceiveProps(newProps) {
        if (this.state.maximized !== newProps.maximize) {
            this.setState({
                maximized: newProps.maximize,
            });
        }
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        const { off } = this.context.command;
        off(COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, this.updateActions);
    }

    /**
     * upon maximize toggle
     */
    onToggleMaximizedState() {
        const maximized = !this.state.maximized;
        this.setState({
            maximized,
        });
        this.props.onToggleMaximizedState(maximized);
    }

    /**
     * upon close
     */
    onClose() {
        this.props.onClose();
    }

    /**
     * Update Actions
     */
    updateActions() {
        this.forceUpdate();
    }

    /**
     * @inheritdoc
     */
    render() {
        const tabs = [];
        this.props.views.forEach((viewDef) => {
            const {
                    id,
                    regionOptions: {
                        panelTitle,
                    },
                  } = viewDef;
            const { width, height, panelResizeInProgress } = this.props;
            const viewProps = {
                width,
                height: height - panelTitleHeight,
                panelResizeInProgress,
            };
            tabs.push((
                <TabPane
                    tab={
                        <div>
                            {panelTitle}
                        </div>
                    }
                    data-extra="tabpane"
                    key={`${id}`}
                >
                    {createViewFromViewDef(viewDef, viewProps)}
                </TabPane>
            ));
        });

        return (
            <div className="bottom-panel" >
                <Tabs
                    activeKey={this.state.activeView}
                    defaultActiveKey={this.props.views[0] ? this.props.views[0].id : undefined}
                    onChange={(activeView) => {
                        this.context.history.put(HISTORY.ACTIVE_BOTTOM_PANEL_VIEW, activeView);
                        this.setState({
                            activeView,
                        });
                        //this.props.onActiveViewChange(activeView);
                    }}
                    renderTabBar={() =>
                        (
                            <ScrollableInkTabBar
                                extraContent={
                                    <div className="actions">
                                        <i
                                            className={`fw fw-${this.state.maximized ? 'down' : 'up'}`}
                                            onClick={() => this.onToggleMaximizedState()}
                                        />
                                        <i
                                            className="fw fw-cancel"
                                            onClick={() => this.onClose()}
                                        />
                                    </div>
                                }
                            />
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

BottomPanel.propTypes = {
    maximize: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    onToggleMaximizedState: PropTypes.func.isRequired,
    onActiveViewChange: PropTypes.func.isRequired,
    views: PropTypes.arrayOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

BottomPanel.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default BottomPanel;
