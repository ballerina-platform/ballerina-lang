import React from 'react';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import { HISTORY } from './../constants';
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
        this.props.views.forEach((viewDef) => {
            const {
                    id,
                    regionOptions: {
                        panelTitle,
                    },
                  } = viewDef;
            const { width, height } = this.props;
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
                    {createViewFromViewDef(viewDef, { width, height: height - panelTitleHeight })}
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
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

BottomPanel.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default BottomPanel;
