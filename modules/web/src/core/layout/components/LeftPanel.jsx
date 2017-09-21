import React from 'react';
import classnames from 'classnames'
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Tab, Nav, NavItem } from 'react-bootstrap';
import ActivityBar from './ActivityBar';
import { HISTORY } from './../constants';


const activityBarWidth = 42;
const panelTitleHeight = 37;

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
        this.state = {
            activeView: context.history.get(HISTORY.ACTIVE_LEFT_PANEL_VIEW) || null,
        };
    }

    /**
     * @inheritdoc
     */
    render() {
        const tabs = [];
        const panes = [];
        this.props.children.forEach((view) => {
            const {
                    props: {
                        definition: {
                            id,
                            regionOptions: {
                                activityBarIcon,
                                panelTitle,
                                panelActions,
                            },
                        },
                    },
                  } = view;
            const actions = [];
            tabs.push((
                <NavItem key={id} eventKey={id}>
                    <i className={`fw fw-${activityBarIcon} fw-lg`} />
                </NavItem>
            ));

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

            panes.push((
                <Tab.Pane key={id} eventKey={id}>
                    <div>
                        <div>
                            <div className="panel-title">
                                {panelTitle}
                            </div>
                            <div className="panel-actions">{actions}</div>
                        </div>
                        <Scrollbars
                            style={{
                                width: this.props.width - activityBarWidth,
                                height: this.props.height - panelTitleHeight,
                            }}
                        >
                            <div className="panel-content">{view}</div>
                        </Scrollbars>
                    </div>
                </Tab.Pane>
            ));
        });
        return (
            <div className="left-panel">
                <div>
                    <Tab.Container
                        id="activity-bar-tabs"
                        activeKey={this.state.activeView}
                        onSelect={(key) => {
                            // if same tab is selected, disable tabs
                            const activeView = this.state.activeView !== key ? key : null;
                            this.context.history.put(HISTORY.ACTIVE_LEFT_PANEL_VIEW, activeView);
                            this.setState({
                                activeView,
                            });
                            this.props.onActiveViewChange(activeView);
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
    children: PropTypes.arrayOf(PropTypes.element),
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
