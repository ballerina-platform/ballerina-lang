import React from 'react';
import PropTypes from 'prop-types';
import { createViewFromViewDef } from './utils';

/**
 * React component for EditorArea Region.
 *
 * @class EditorArea
 * @extends {React.Component}
 */
class EditorArea extends React.Component {
    /**
     * @inheritdoc
     */
    render() {
        const { width, height, panelResizeInProgress } = this.props;
        const viewProps = { width, height, panelResizeInProgress };
        return (
            <div className="editor-area" >
                {this.props.views.map((viewDef) => {
                    return createViewFromViewDef(viewDef, viewProps);
                })}
            </div>
        );
    }
}

EditorArea.propTypes = {
    panelResizeInProgress: PropTypes.bool.isRequired,
    views: PropTypes.arrayOf(Object).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default EditorArea;
