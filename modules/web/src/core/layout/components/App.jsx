import React from 'react';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import LeftPanel from './LeftPanel';
import EditorArea from './EditorArea';
import BottomPanel from './BottomPanel';
import Header from './Header';
import { REGIONS } from './../constants';

/**
 * React component for App.
 *
 * @class App
 * @extends {React.Component}
 */
class App extends React.Component {

    /**
     * Get views of given Region
     *
     * @returns {Array<React.Component>}
     */
    getViewsForRegion(region) {
        return this.props.layout[region].map((viewDef) => {
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
            <View {...propsProvider()} key={viewDef.id} definition={viewDef} />
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="">
                <Header>
                    {this.getViewsForRegion(REGIONS.HEADER)}
                </Header>
                <SplitPane
                    split="vertical"
                    className="left-right-split-pane"
                    minSize={300}
                    maxSize={700}
                >
                    <LeftPanel>
                        {this.getViewsForRegion(REGIONS.LEFT_PANEL)}
                    </LeftPanel>
                    <SplitPane
                        className="top-bottom-split-pane"
                        split="horizontal"
                        primary="second"
                        minSize={300}
                        maxSize={700}
                        onChange={() => {
                            this.forceUpdate();
                        }
                        }
                    >
                        <EditorArea />
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
