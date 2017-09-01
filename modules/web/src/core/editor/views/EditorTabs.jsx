import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Tabs, { TabPane } from 'rc-tabs';
import TabContent from 'rc-tabs/lib/TabContent';
import ScrollableInkTabBar from 'rc-tabs/lib/ScrollableInkTabBar';
import 'rc-tabs/assets/index.css';
import View from './../../view/view';
import FileTree from './../../view/FileTree';
import { VIEWS, EVENTS, COMMANDS } from './../constants';

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
            openedEditors: [],
        };
        const { command: { on } } = this.props.editorPlugin.appContext;
        on(COMMANDS.OPEN_FILE_IN_EDITOR, (args) => {
            this.setState({
                openedEditors: this.state.openedEditors.push(args),
            });
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const onTabClose = (sfile) => {
            _.remove(this.state.openedEditors, ({ file }) => file.fullPath === sfile.fullPath);
            this.forceUpdate();
        };
        const tabTitle = file => (
            <div data-extra="tab-bar-title">
                <i className="fw fw-ballerina tab-icon" />
                {file.name}
                <button
                    type="button"
                    className="close close-tab pull-right"
                    onClick={() => onTabClose(file)}
                >
                    ×
                </button>
            </div>
        );
        const makeTabPane = (file, editor) => {
            const { component } = editor;
            return (
                <TabPane tab={tabTitle(file)} data-extra="tabpane" key={`${file.fullPath}`} >
                    <component file={file} />
                </TabPane>
            );
        };

        const tabs = [];
        this.state.openedEditors.forEach(({ file, editor }) => {
            tabs.push(makeTabPane(file, editor));
        });

        return (
            <div className="editor-area" >
                <Tabs
                    defaultActiveKey={this.state.openedEditors[0]}
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
