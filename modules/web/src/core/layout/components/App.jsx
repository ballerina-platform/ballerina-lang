import React from 'react';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import LeftPanel from './LeftPanel';
import EditorArea from './EditorArea';
import RightPanel from './RightPanel';
import BottomPanel from './BottomPanel';
import Header from './Header';
import ActivityBar from './ActivityBar';
import { REGIONS } from './../constants';

/**
 * React component for App.
 *
 * @class App
 * @extends {React.Component}
 */
class App extends React.Component {

    /**
     * Get views of Header Region
     * @returns {Array<React.Component>}
     */
    getHeaderViews() {
        return this.props.layout[REGIONS.HEADER].map((viewDef) => {
            return this.createViewFromViewDef(viewDef);
        });
    }

    /**
     * Creates View from view Def
     * @param {Object} viewDef View Definition
     */
    createViewFromViewDef(viewDef) {
        const { component, propsProvider } = viewDef;
        const View = component;
        return (
            <View {...propsProvider()} key={viewDef.id} />
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="">
                <Header>
                    {this.getHeaderViews()}
                </Header>
                <ActivityBar />
                <SplitPane
                    split="vertical"
                    className="left-right-split-pane"
                    minSize={300}
                    maxSize={700}
                >
                    <LeftPanel />
                    <SplitPane
                        className="top-bottom-split-pane"
                        split="horizontal"
                        primary="second"
                        minSize={300}
                        maxSize={700}
                    >
                        <SplitPane
                            split="vertical"
                            className="editor-right-split-pane"
                            primary="second"
                            defaultSize={0}
                            maxSize={700}
                            onChange={() => {
                                this.forceUpdate();
                            }
                            }
                        >
                            <EditorArea />
                            <RightPanel />
                        </SplitPane>
                        <BottomPanel />
                    </SplitPane>
                </SplitPane>
            </div>
        );
    }
}

App.propTypes = {
    layout: PropTypes.objectOf(Object).isRequired,
};

export default App;
