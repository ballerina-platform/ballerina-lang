import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import LeftPanel from './LeftPanel';
import EditorArea from './EditorArea';
import BottomPanel from './BottomPanel';
import Header from './Header';
import { REGIONS, HISTORY } from './../constants';

/**
 * React component for App.
 *
 * @class App
 * @extends {React.Component}
 */
class App extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = this.props.appContext.pref;
        this.state = {
            showLeftPanel: !_.isNil(history.get(HISTORY.ACTIVE_LEFT_PANEL_VIEW)),
            showBottomPanel: history.get(HISTORY.BOTTOM_PANEL_ACTIVE),
        };
    }

    /**
     * @inheritdoc
     */
    getChildContext() {
        return {
            history: this.props.appContext.pref.history,
        };
    }

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
                    minSize={this.state.showLeftPanel ? 300 : 0}
                    maxSize={700}
                    defaultSize={
                        this.state.showLeftPanel
                            ? parseInt(this.props.appContext
                                    .pref.history.get(HISTORY.LEFT_PANEL_SIZE), 10) || 300
                            : 0
                    }
                    onChange={
                        size => this.props.appContext.pref.history.put(HISTORY.LEFT_PANEL_SIZE, size)
                    }
                    pane2Style={
                        this.state.showLeftPanel ? {} : { position: 'relative', left: '42px' }
                    }
                >
                    <LeftPanel
                        onActiveViewChange={
                            (newView) => {
                                this.setState({
                                    showLeftPanel: !_.isNil(newView),
                                });
                            }
                        }
                    >
                        {this.getViewsForRegion(REGIONS.LEFT_PANEL)}
                    </LeftPanel>
                    <SplitPane
                        className="top-bottom-split-pane"
                        split="horizontal"
                        primary="second"
                        minSize={300}
                        maxSize={700}
                        defaultSize={
                            parseInt(this.props.appContext
                                    .pref.history.get(HISTORY.BOTTOM_PANEL_SIZE), 10) || 300
                        }
                        onChange={
                            size => this.props.appContext.pref.history.put(HISTORY.BOTTOM_PANEL_SIZE, size)
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
    appContext: PropTypes.objectOf(Object).isRequired,
    layout: PropTypes.objectOf(Object).isRequired,
};

App.childContextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default App;
