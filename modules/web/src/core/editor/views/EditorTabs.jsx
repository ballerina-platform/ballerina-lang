import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import FileTree from './../../view/FileTree';
import { VIEWS, EVENTS } from './../constants';

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
        this.state = {
            openedFiles: _.times(2, i => `test${i}.bal`),
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
                <i className="fw fw-ballerina tab-icon" />
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
            </TabPane>
        );

        const tabs = [];
        this.state.openedFiles.forEach((file) => {
            tabs.push(makeTabPane(file));
        });

        return (
            <div className="editor-area" >
                <Tabs
                    defaultActiveKey={this.state.openedFiles[0]}
                    onChange={() => {}}
                    renderTabBar={() =>
                        (
                            <ScrollableInkTabBar />
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

EditorTabs.propTypes = {
    editorPlugin: PropTypes.objectOf(Object).isRequired,
};

EditorTabs.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default EditorTabs;
