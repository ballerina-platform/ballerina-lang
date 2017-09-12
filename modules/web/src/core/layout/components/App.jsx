import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import LeftPanel from './LeftPanel';
import EditorArea from './EditorArea';
import BottomPanel from './BottomPanel';
import Header from './Header';
import { REGIONS, HISTORY, EVENTS } from './../constants';
import { withReRenderSupport } from './utils';

const leftPanelDefaultSize = 300;
const leftPanelMaxSize = 700;
const bottomPanelDefaultSize = 300;
const bottomPanelMaxSize = 700;
const headerHeight = 35;
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
        const showLeftPanel = !_.isNil(history.get(HISTORY.ACTIVE_LEFT_PANEL_VIEW));
        const leftPanelSize = showLeftPanel
                                    ? (parseInt(this.props.appContext
                                        .pref.history.get(HISTORY.LEFT_PANEL_SIZE), 10)
                                            || leftPanelDefaultSize)
                                    : 0;
        const bottomPanelIsActive = history.get(HISTORY.BOTTOM_PANEL_IS_ACTIVE);
        const showBottomPanel = !_.isNil(bottomPanelIsActive) ? bottomPanelIsActive : false;
        const bottomPanelSize = showBottomPanel
                                    ? (parseInt(this.props.appContext
                                        .pref.history.get(HISTORY.BOTTOM_PANEL_SIZE), 10)
                                            || bottomPanelDefaultSize)
                                    : 0;
        this.state = {
            showLeftPanel,
            leftPanelSize,
            showBottomPanel,
            bottomPanelSize,
            documentHeight: window.innerHeight,
            documentWidth: window.innerWidth,
        };
        this.leftRightSplitPane = undefined;
        this.topBottomSplitPane = undefined;

        // handle window resize events
        window.addEventListener('resize', this.handleWindowResize.bind(this));
        this.props.layoutPlugin.on(EVENTS.TOGGLE_BOTTOM_PANLEL, () => {
            this.setBottomPanelState(!this.state.showBottomPanel);
        });
    }

    /**
     * @inheritdoc
     */
    getChildContext() {
        return {
            history: this.props.appContext.pref.history,
            command: this.props.appContext.command,
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
     * This will rerender app when the window is resized.
     *
     * @param {object} event event object.
     * @memberof App
     */
    handleWindowResize() {
        this.setState({
            documentHeight: window.innerHeight,
            documentWidth: window.innerWidth,
        });
    }

    /**
     * Creates View from view Def
     * @param {Object} viewDef View Definition
     */
    createViewFromViewDef(viewDef) {
        const { component, propsProvider, pluginID } = viewDef;
        const View = withReRenderSupport(component, pluginID);
        return (
            <View {...propsProvider()} key={viewDef.id} definition={viewDef} />
        );
    }

     /**
     * Update left panel state
     * @param {boolean} showLeftPanel
     * @param {number} leftPanelSize
     */
    setLeftPanelState(showLeftPanel, leftPanelSize) {
        const { history } = this.props.appContext.pref;
        if (_.isNil(leftPanelSize)) {
            const sizeFromHistory = history.get(HISTORY.LEFT_PANEL_SIZE);
            leftPanelSize = !_.isNil(sizeFromHistory) && sizeFromHistory !== 0
                                    ? sizeFromHistory : leftPanelDefaultSize;
        }
        history.put(HISTORY.LEFT_PANEL_SIZE, leftPanelSize);
        this.setState({
            showLeftPanel,
            leftPanelSize,
        });
    }

    /**
     * Update bottom panel state
     * @param {boolean} showBottomPanel
     * @param {number} bottomPanelSize
     */
    setBottomPanelState(showBottomPanel, bottomPanelSize) {
        const { history } = this.props.appContext.pref;
        if (_.isNil(bottomPanelSize)) {
            const sizeFromHistory = history.get(HISTORY.BOTTOM_PANEL_SIZE);
            bottomPanelSize = !_.isNil(sizeFromHistory) && sizeFromHistory !== 0
                                    ? sizeFromHistory : bottomPanelDefaultSize;
        }
        history.put(HISTORY.BOTTOM_PANEL_IS_ACTIVE, showBottomPanel);
        history.put(HISTORY.BOTTOM_PANEL_SIZE, bottomPanelSize);
        this.setState({
            showBottomPanel,
            bottomPanelSize,
        });
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
                    ref={(ref) => { this.leftRightSplitPane = ref; }}
                    split="vertical"
                    className="left-right-split-pane"
                    minSize={this.state.showLeftPanel ? leftPanelDefaultSize : 0}
                    maxSize={leftPanelMaxSize}
                    defaultSize={this.state.showLeftPanel ? this.state.leftPanelSize : 0}
                    onDragFinished={(size) => {
                        this.setLeftPanelState(true, size);
                        if (!_.isNil(this.leftRightSplitPane)) {
                            this.leftRightSplitPane.setState({
                                resized: false,
                                draggedSize: undefined,
                            });
                        }
                    }
                    }
                    pane2Style={
                        this.state.showLeftPanel ? {} : { position: 'relative', left: '42px' }
                    }
                >
                    <LeftPanel
                        width={this.state.leftPanelSize}
                        height={this.state.documentHeight - (headerHeight)}
                        onActiveViewChange={
                            (newView) => {
                                if (!_.isNil(this.leftRightSplitPane)) {
                                    this.leftRightSplitPane.setState({
                                        resized: false,
                                        draggedSize: undefined,
                                    });
                                }
                                this.setLeftPanelState(!_.isNil(newView));
                            }
                        }
                    >
                        {this.getViewsForRegion(REGIONS.LEFT_PANEL)}
                    </LeftPanel>
                    <SplitPane
                        ref={(ref) => { this.topBottomSplitPane = ref; }}
                        className="top-bottom-split-pane"
                        split="horizontal"
                        primary="second"
                        minSize={this.state.showBottomPanel ? bottomPanelDefaultSize : 0}
                        maxSize={bottomPanelMaxSize}
                        defaultSize={this.state.showBottomPanel ? this.state.bottomPanelSize : 0}
                        onDragFinished={(size) => {
                            if (!_.isNil(this.topBottomSplitPane)) {
                                this.topBottomSplitPane.setState({
                                    resized: false,
                                    draggedSize: undefined,
                                });
                            }
                            this.setBottomPanelState(true, size);
                        }
                        }
                        pane1Style={{
                            height: this.state.documentHeight - (headerHeight + this.state.bottomPanelSize),
                        }}
                    >
                        <EditorArea>
                            {this.getViewsForRegion(REGIONS.EDITOR_AREA)}
                        </EditorArea>
                        <BottomPanel
                            maximize={this.state.showBottomPanel && this.state.bottomPanelSize === bottomPanelMaxSize}
                            onClose={
                                () => {
                                    this.setBottomPanelState(false);
                                }
                            }
                            onToggleMaximizedState={
                                (isMaximized) => {
                                    const newSize = isMaximized ? bottomPanelMaxSize : bottomPanelDefaultSize;
                                    this.setBottomPanelState(true, newSize);
                                }
                            }
                            onActiveViewChange={
                                (newView) => {
                                    this.setBottomPanelState(!_.isNil(newView));
                                    if (!_.isNil(this.topBottomSplitPane)) {
                                        this.topBottomSplitPane.setState({
                                            resized: false,
                                            draggedSize: undefined,
                                        });
                                    }
                                }
                            }
                        >
                            {this.getViewsForRegion(REGIONS.BOTTOM_PANEL)}
                        </BottomPanel>
                    </SplitPane>
                </SplitPane>
            </div>
        );
    }
}

App.propTypes = {
    layoutPlugin: PropTypes.objectOf(Object).isRequired,
    appContext: PropTypes.objectOf(Object).isRequired,
    layout: PropTypes.objectOf(Object).isRequired,
};

App.childContextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default App;
