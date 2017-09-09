import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import LeftPanel from './LeftPanel';
import EditorArea from './EditorArea';
import BottomPanel from './BottomPanel';
import Header from './Header';
import { REGIONS, HISTORY, EVENTS } from './../constants';

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
            const showBottomPanel = !this.state.showBottomPanel;
            const bottomPanelSize = !showBottomPanel ? 0 : bottomPanelDefaultSize;
            history.put(HISTORY.BOTTOM_PANEL_IS_ACTIVE, showBottomPanel);
            history.put(HISTORY.BOTTOM_PANEL_SIZE, bottomPanelSize);
            this.setState({
                showBottomPanel,
                bottomPanelSize,
            });
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
                    ref={(ref) => { this.leftRightSplitPane = ref; }}
                    split="vertical"
                    className="left-right-split-pane"
                    minSize={this.state.showLeftPanel ? leftPanelDefaultSize : 0}
                    maxSize={leftPanelMaxSize}
                    defaultSize={this.state.leftPanelSize}
                    onDragFinished={(size) => {
                        this.props.appContext.pref.history.put(HISTORY.LEFT_PANEL_SIZE, size);
                        this.leftRightSplitPane.setState({
                            resized: false,
                            draggedSize: undefined,
                        });
                        this.setState({
                            leftPanelSize: size,
                        });
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
                                if (_.isNil(newView)) {
                                    this.setState({
                                        showLeftPanel: false,
                                        leftPanelSize: 0,
                                    });
                                } else {
                                    this.setState({
                                        showLeftPanel: true,
                                        leftPanelSize: this.props.appContext.pref.history.get(HISTORY.LEFT_PANEL_SIZE)
                                                            || leftPanelDefaultSize,
                                    });
                                }
                                this.leftRightSplitPane.setState({
                                    resized: false,
                                    draggedSize: undefined,
                                });
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
                        defaultSize={this.state.bottomPanelSize}
                        onDragFinished={(size) => {
                            this.props.appContext.pref.history.put(HISTORY.BOTTOM_PANEL_SIZE, size);
                            this.topBottomSplitPane.setState({
                                resized: false,
                                draggedSize: undefined,
                            });
                            this.setState({
                                bottomPanelSize: size,
                            });
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
                            onClose={
                                () => {
                                    this.props.appContext.pref.history.put(HISTORY.BOTTOM_PANEL_IS_ACTIVE, false);
                                    this.setState({
                                        showBottomPanel: false,
                                        bottomPanelSize: 0,
                                    });
                                }
                            }
                            onToggleMaximizedState={
                                (isMaximized) => {
                                    const newSize = isMaximized ? bottomPanelMaxSize : bottomPanelDefaultSize;
                                    this.props.appContext.pref.history.put(HISTORY.BOTTOM_PANEL_SIZE, newSize);
                                    this.setState({
                                        bottomPanelSize: newSize,
                                    });
                                }
                            }
                            onActiveViewChange={
                                (newView) => {
                                    if (_.isNil(newView)) {
                                        this.setState({
                                            showBottomPanel: false,
                                            bottomPanelSize: 0,
                                        });
                                    } else {
                                        this.setState({
                                            showBottomPanel: true,
                                            bottomPanelSize: this.props.appContext.pref.history.get(HISTORY.BOTTOM_PANEL_SIZE)
                                                                || leftPanelDefaultSize,
                                        });
                                    }
                                    this.topBottomSplitPane.setState({
                                        resized: false,
                                        draggedSize: undefined,
                                    });
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
