import React from 'react';
import PropTypes from 'prop-types';
import { createViewFromViewDef } from './utils';

/**
 * React component for ToolArea Region.
 *
 * @class ToolArea
 * @extends {React.Component}
 */
class ToolArea extends React.Component {

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps, nextState) {
        return !nextProps.panelResizeInProgress;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { width, height, panelResizeInProgress } = this.props;
        const viewProps = { width, height, panelResizeInProgress };
        return (
            <div className="tool-area">
                {this.props.views.map((viewDef) => {
                    return createViewFromViewDef(viewDef, viewProps);
                })}
            </div>
        );
    }
}

ToolArea.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default ToolArea;
