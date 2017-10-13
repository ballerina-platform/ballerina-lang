import React from 'react';
import classnames from 'classnames';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Tab, Nav, NavItem } from 'react-bootstrap';
import ActivityBar from './ActivityBar';
import { HISTORY } from './../constants';
import { createViewFromViewDef } from './utils';

const activityBarWidth = 42;
const panelTitleHeight = 36;

/**
 * A Tab inside Left Panel
 */
class LeftPanelTab extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.scroller = undefined;
    }

    /**
     * @override
     */
    getChildContext() {
        return {
            getScroller: () => {
                return this.scroller;
            },
        };
    }

    /**
     * @inheritdoc
     */
    render() {
        const { viewDef, width, height, panelResizeInProgress,
                viewDef: {
                    id,
                    regionOptions: {
                        panelTitle,
                        panelActions,
                    },
                },
            } = this.props;
        const actions = [];
        if (!_.isNil(panelActions) && _.isArray(panelActions)) {
            panelActions.forEach(({ icon, isActive, handleAction }, index) => {
                const isActionactive = _.isFunction(isActive) ? isActive() : true;
                actions.push((
                    <i
                        key={id + index}
                        className={classnames('fw', `fw-${icon}`, { active: isActionactive })}
                        onClick={() => {
                            if (isActionactive && _.isFunction(handleAction)) {
                                handleAction();
                            }
                        }}
                    />
                ));
            });
        }
        const dimensions = {
            width: width - activityBarWidth,
            height: height - panelTitleHeight,
        };
        const viewProps = {
            ...dimensions,
            panelResizeInProgress,
        };
        return (
            <div>
                <div>
                    <div className="panel-title">
                        {panelTitle}
                    </div>
                    <div className="panel-actions">{actions}</div>
                </div>
                <Scrollbars
                    style={dimensions}
                    className="panel-content-scroll-container"
                    ref={(ref) => {
                        this.scroller = ref;
                    }}
                    autoHide // Hide delay in ms
                    autoHideTimeout={1000}
                >
                    <div className="panel-content" style={{ width: width - activityBarWidth }}>
                        {
                            createViewFromViewDef(viewDef, viewProps)
                        }
                    </div>
                </Scrollbars>
            </div>
        );
    }

}

LeftPanelTab.propTypes = {
    viewDef: PropTypes.instanceOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

LeftPanelTab.childContextTypes = {
    getScroller: PropTypes.func.isRequired,
};

/**
 * React component for LeftPanel Region.
 *
 * @class LeftPanel
 * @extends {React.Component}
 */
class LeftPanel extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props, context) {
        super(props, context);
    }

    /**
     * @inheritdoc
     */
    render() {
        const tabs = [];
        const panes = [];
        const { views, onActiveViewChange, ...restProps } = this.props;
        views.forEach((viewDef) => {
            const {
                    id,
                    regionOptions: {
                        activityBarIcon,
                    },
                  } = viewDef;
            tabs.push((
                <NavItem key={id} eventKey={id}>
                    <i className={`fw fw-${activityBarIcon} fw-lg`} />
                </NavItem>
            ));
            const propsForTab = {
                viewDef,
                ...restProps,
            };
            panes.push((
                <Tab.Pane key={id} eventKey={id}>
                    <LeftPanelTab {...propsForTab} />
                </Tab.Pane>
            ));
        });
        const activeViewPrev = this.props.activeView || this.context.history.get(HISTORY.ACTIVE_LEFT_PANEL_VIEW);
        return (
            <div className="left-panel">
                <div>
                    <Tab.Container
                        id="activity-bar-tabs"
                        activeKey={activeViewPrev}
                        onSelect={(key) => {
                            const activeView = activeViewPrev !== key ? key : null;
                            // if same tab is selected, disable tabs
                            this.context.history.put(HISTORY.ACTIVE_LEFT_PANEL_VIEW, activeView);
                            onActiveViewChange(activeView);
                        }}
                    >
                        <div>
                            <ActivityBar>
                                <Nav bsStyle="tabs">
                                    {tabs}
                                </Nav>
                            </ActivityBar>
                            <Tab.Content animation>
                                {panes}
                            </Tab.Content>
                        </div>
                    </Tab.Container>
                </div>
            </div>
        );
    }
}

LeftPanel.propTypes = {
    onActiveViewChange: PropTypes.func.isRequired,
    views: PropTypes.arrayOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    activeView: PropTypes.string,
};

LeftPanel.defaultProps = {
    activeView: undefined,
};

LeftPanel.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default LeftPanel;
