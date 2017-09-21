import React from 'react';
import PropTypes from 'prop-types';
import uuid from 'uuid/v1';
import classnames from 'classnames';
import { ContextMenuTrigger as ReactContextMenuTrigger, ContextMenu, MenuItem } from 'react-contextmenu';
import './styles.scss';

/**
 * Class to represent ContextMenuTrigger
 */
class ContextMenuTrigger extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <ReactContextMenuTrigger
                    id={this.props.id}
                    holdToDisplay={1000}
                    collect={() => {}}
                >
                    {this.props.children}
                </ReactContextMenuTrigger>
                <ContextMenu id={this.props.id}>
                    <MenuItem onClick={this.handleClick} data={{ action: 'Added' }}>Add 1 count</MenuItem>
                    <MenuItem onClick={this.handleClick} data={{ action: 'Removed' }}>Remove 1 count</MenuItem>
                </ContextMenu>
            </div>
        );
    }

}

const MenuDef = PropTypes.shape({
    icon: PropTypes.string,
    label: PropTypes.string.isRequired,
    handler: PropTypes.func.isRequired,
    isActive: PropTypes.func.isRequired,
    children: PropTypes.arrayOf(Object),
});

ContextMenuTrigger.propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
    menu: PropTypes.arrayOf(MenuDef),
};

ContextMenuTrigger.defaultProps = {
    id: uuid(),
    children: [],
    menu: [],
};

export default ContextMenuTrigger;
