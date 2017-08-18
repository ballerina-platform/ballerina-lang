import _ from 'lodash';
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
    constructor(props) {
        super(props);
        this.state = {
            openedFiles: ['test.bal', 'test2.bal'],
        };
    }

    /**
     * @inheritdoc
     */
    render() {
        const onTabClose = (file) => {
            _.remove(this.state.openedFiles, item => item === file);
            this.forceUpdate();
        };
        const tabTitle = file => (
            <div data-extra="tab-bar-title">
                {file}
                <button
                    type="button"
                    className="close close-tab pull-right"
                    onClick={() => onTabClose(file)}
                >
                    ×
                </button>
            </div>
        );
        const makeTabPane = file => (
            <TabPane tab={tabTitle(file)} data-extra="tabpane" key={`${file}`}>
                    Tab for file {file}
            </TabPane>
        );

        const tabs = [];
        this.state.openedFiles.forEach((file) => {
            tabs.push(makeTabPane(file));
        });

        return (
            <div
                className="editor-area"
                ref={(ref) => {
                    this.container = ref;
                }
                }
            >
                <Tabs
                    defaultActiveKey={this.state.openedFiles[0]}
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
                    {tabs}
                </Tabs>
            </div>
        );
    }
}

EditorArea.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default EditorArea;
