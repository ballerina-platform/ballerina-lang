import React from 'react';
import cn from 'classnames';
import PropTypes from 'prop-types';
import './tool.scss';

/**
 * ToolBar
 */
class Tool extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        const { tool: { icon, commandID, commandArgs = {}, isActive = () => true, isVisible = () => true,
                        description },
                dispatch } = this.props;

        if (!isVisible()) {
            return null;
        }
        return (
            <div
                className={cn('tool-bar-tool', { active: isActive(), visible: isVisible() })}
                onClick={() => {
                    if (isActive() && isVisible()) {
                        dispatch(commandID, commandArgs);
                    }
                }}
                title={description}
            >
                <i className={`fw fw-${icon}`} />
            </div>
        );
    }
}

Tool.propTypes = {
    dispatch: PropTypes.func.isRequired,
    tool: PropTypes.shape({
        id: PropTypes.string.isRequired,
        icon: PropTypes.string.isRequired,
        commandID: PropTypes.string.isRequired,
        commandArgs: PropTypes.objectOf(Object),
        isActive: PropTypes.func,
        description: PropTypes.string,
    }).isRequired,
};

export default Tool;
