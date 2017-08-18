import React from 'react';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';

/**
 * React component for EditorArea Region.
 *
 * @class EditorArea
 * @extends {React.Component}
 */
class EditorArea extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div
                className="editor-area"
                ref={(ref) => {
                    this.container = ref;
                }
                }
            >
                <Tabs
                    defaultActiveKey="2"
                    onChange={() => {}}
                    renderTabBar={() =>
                        (
                            <ScrollableInkTabBar
                                style={{ maxWidth: this.container ? this.container.clientWidth : '900px' }}
                            />
                        )
                    }
                    renderTabContent={() =>
                        <TabContent />
                    }
                >
                    <TabPane tab='tab 1' key="1">TabContent</TabPane>
                    <TabPane tab='tab 2' key="2">TabContent</TabPane>
                    <TabPane tab='tab 3' key="3">TabContent</TabPane>
                    <TabPane tab='tab 4' key="4">TabContent</TabPane>
                    <TabPane tab='tab 5' key="5">TabContent</TabPane>
                    <TabPane tab='tab 6' key="6">TabContent</TabPane>
                    <TabPane tab='tab 7' key="7">TabContent</TabPane>
                    <TabPane tab='tab 8' key="8">TabContent</TabPane>
                    <TabPane tab='tab 9' key="9">TabContent</TabPane>
                    <TabPane tab='tab 10' key="10">TabContent</TabPane>
                    <TabPane tab='tab 11' key="11">TabContent</TabPane>
                    <TabPane tab='tab 12' key="12">TabContent</TabPane>
                    <TabPane tab='tab 13' key="13">TabContent</TabPane>
                    <TabPane tab='tab 14' key="14">TabContent</TabPane>
                    <TabPane tab='tab 15' key="15">TabContent</TabPane>
                    <TabPane tab='tab 16' key="16">TabContent</TabPane>
                    <TabPane tab='tab 17' key="17">TabContent</TabPane>
                    <TabPane tab='tab 18' key="18">TabContent</TabPane>
                    <TabPane tab='tab 19' key="19">TabContent</TabPane>
                </Tabs>
            </div>
        );
    }
}

EditorArea.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default EditorArea;
