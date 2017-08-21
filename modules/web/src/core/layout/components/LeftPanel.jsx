import React from 'react';
import PropTypes from 'prop-types';
import { Tab, Nav, NavItem } from 'react-bootstrap';
import ActivityBar from './ActivityBar';

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
    render() {
        return (
            <div className="left-panel">
                <div>
                    <Tab.Container id="activity-bar-tabs" defaultActiveKey="first">
                        <div>
                            <ActivityBar>
                                <Nav bsStyle="tabs">
                                    <NavItem eventKey="first">
                                        <i className="fw-file-browse fw-lg" />
                                    </NavItem>
                                    <NavItem eventKey="second">
                                        <i className="fw fw-start fw-lg" />
                                    </NavItem>
                                </Nav>
                            </ActivityBar>
                            <Tab.Content animation>
                                <Tab.Pane eventKey="first">
                                Tab 1 content
                                </Tab.Pane>
                                <Tab.Pane eventKey="second">
                                Tab 2 content
                                </Tab.Pane>
                            </Tab.Content>
                        </div>
                    </Tab.Container>
                </div>
            </div>
        );
    }
}

LeftPanel.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default LeftPanel;
