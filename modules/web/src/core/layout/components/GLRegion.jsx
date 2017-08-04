import React from 'react';
import PropTypes from 'prop-types';
import GoldenLayout from 'golden-layout';

import 'golden-layout/src/css/goldenlayout-base.css';
import 'golden-layout/src/css/goldenlayout-dark-theme.css';

/**
 * React component for Golden Layout based region.
 *
 * @class GLRegion
 * @extends {React.Component}
 */
class GLRegion extends React.Component {

    constructor(props) {
        super(props);
        const glConfig = props.layout;
        // create the layout
        this.composerLayout = new GoldenLayout(glConfig);
        this.container = undefined;
    }

    componentDidMount() {
        if (this.container) {
            this.composerLayout.registerComponent('testComponent', (container, componentState) => {
                container.getElement().html('<h2>' + componentState.label + '</h2>');
            });
            this.composerLayout.init();
        }
    }

    render() {
        return (
            <div className="app-dynamic-layout" ref={(ref) => { this.container = ref; }} />
        );
    }

}

GLRegion.propTypes = {
    layout: PropTypes.objectOf(Object),
};

GLRegion.defaultProps = {
    layout: {
        settings: {
            constrainDragToContainer: true,
            reorderEnabled: true,
            selectionEnabled: false,
            popoutWholeStack: false,
            blockedPopoutsThrowError: true,
            closePopoutsOnUnload: true,
            showPopoutIcon: false,
            showMaximiseIcon: false,
            showCloseIcon: false,
        },
        dimensions: {
            borderWidth: 2,
            minItemHeight: 10,
            minItemWidth: 10,
            headerHeight: 20,
            dragProxyWidth: 300,
            dragProxyHeight: 200,
        },
        labels: {
            close: 'close',
            maximise: 'maximise',
            minimise: 'minimise',
            popout: 'open in new window',
        },
        content: [{
            id: 'root',
            type: 'column',
            content: [{
                id: 'header-area',
                type: 'column',
                height: 5,
                content: [{
                    header: {
                        show: false,
                        frozen: true,
                    },
                    splitters: false,
                    type: 'component',
                    componentName: 'testComponent',
                    componentState: { label: 'North' },
                }],
            }, {
                id: 'body',
                type: 'row',
                content: [{
                    header: {
                        show: false,
                    },
                    width: 20,
                    id: 'left-panel',
                    type: 'component',
                    componentName: 'testComponent',
                    componentState: { label: 'Left' },
                },
                {
                    id: 'right-panel',
                    type: 'column',
                    content: [{
                        id: 'editor-area',
                        type: 'stack',
                        height: 70,
                        hasHeaders: true,
                        content: [{
                            title: 'file1.bal',
                            type: 'component',
                            componentName: 'testComponent',
                            componentState: { label: 'File1' },
                        },
                        {
                            title: 'file2.bal',
                            type: 'component',
                            componentName: 'testComponent',
                            componentState: { label: 'File2' },
                        }],
                    }, {
                        id: 'footer-area',
                        type: 'stack',
                        hasHeaders: true,
                        content: [{
                            title: 'debug output',
                            type: 'component',
                            componentName: 'testComponent',
                            componentState: { label: 'Debug output' },
                        },
                        {
                            title: 'console',
                            type: 'component',
                            componentName: 'testComponent',
                            componentState: { label: 'Console' },
                        }],
                    }],
                },
                ],
            },
            ],
        }],
    },
};

export default GLRegion;
