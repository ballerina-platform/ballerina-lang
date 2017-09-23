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
    render() {
        const { width, height } = this.props;
        return (
            <div className="tool-area">
                {this.props.views.map((viewDef) => {
                    return createViewFromViewDef(viewDef, { width, height });
                })}
            </div>
        );
    }
}

ToolArea.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default ToolArea;
