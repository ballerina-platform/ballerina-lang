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
        const { width, height } = this.props;
        return (
            <div className="editor-area" >
                {this.props.views.map((viewDef) => {
                    return createViewFromViewDef(viewDef, { width, height });
                })}
            </div>
        );
    }
}

EditorArea.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default EditorArea;
