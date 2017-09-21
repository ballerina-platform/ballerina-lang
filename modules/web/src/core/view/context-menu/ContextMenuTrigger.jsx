import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
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
                    {this.props.menu.map((menuItem, i) => {
                        const { divider, handler, label, isActive } = menuItem;
                        return (
                            divider
                                ? <MenuItem key={i} divider />
                                : <MenuItem
                                    key={i}
                                    disabled={_.isFunction(isActive) ? !isActive() : false}
                                    onClick={handler}
                                >
                                    {label}
                                </MenuItem>
                        );
                    })}
                </ContextMenu>
            </div>
        );
    }

}

const MenuDef = PropTypes.shape({
    divider: PropTypes.bool,
    icon: PropTypes.string,
    label: PropTypes.string,
    handler: PropTypes.func,
    isActive: PropTypes.func,
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
