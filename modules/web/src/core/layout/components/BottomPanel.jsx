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
            activeView: context.history.get(HISTORY.ACTIVE_BOTTOM_PANEL_VIEW) || undefined,
        };
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
                                        <i className="fw fw-up" />
                                        <i className="fw fw-cancel" />
                                    </div>
                                }
                            />
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

BottomPanel.propTypes = {
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
