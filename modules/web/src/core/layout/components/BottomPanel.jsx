import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import { HISTORY } from './../constants';

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
        this.state = {
            maximized: this.props.maximize,
            activeView: context.history.get(HISTORY.ACTIVE_BOTTOM_PANEL_VIEW) || undefined,
        };
    }

     /**
     * @inheritdoc
     */
    componentWillReceiveProps(newProps) {
        if(this.state.maximized !== newProps.maximize) {
            this.setState({
                maximized: newProps.maximize,
            });
        }
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
     * @inheritdoc
     */
    render() {
        const tabs = [];
        this.props.children.forEach((view) => {
            const {
                    props: {
                        definition: {
                            id,
                            regionOptions: {
                                panelTitle,
                            },
                        },
                    },
                  } = view;
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
                    {view}
                </TabPane>
            ));
        });

        return (
            <div className="bottom-panel" >
                <Tabs
                    activeKey={this.state.activeView}
                    defaultActiveKey={this.props.children[0] ? this.props.children[0].props.definition.id : undefined}
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
    children: PropTypes.arrayOf(PropTypes.element).isRequired,
};

BottomPanel.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default BottomPanel;
