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
                <ContextMenu id={this.props.id} onShow={this.props.onShow} onHide={this.props.onHide} >
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
    onShow: PropTypes.func,
    onHide: PropTypes.func,
};

ContextMenuTrigger.defaultProps = {
    id: uuid(),
    children: [],
    menu: [],
    onShow: () => {},
    onHide: () => {},
};

export default ContextMenuTrigger;
