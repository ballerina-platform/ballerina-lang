import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Menu, { SubMenu, MenuItem, Divider } from 'rc-menu';
import View from './../../view/view';
import { VIEW_IDS, MENU_DEF_TYPES } from './../constants';
import 'rc-menu/assets/index.css';

/**
 * Application Menu Controller
 */
class ApplicationMenu extends View {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.onMenuItemClick = this.onMenuItemClick.bind(this);
        this.state = {
            activeKeys: [],
        };
    }

    /**
     * Click event handler for rc-menu
     * @param {Object} evt
     */
    onMenuItemClick(evt) {
        const { item: { props: { menuDef: { command, commandArgs = {}, type } } } } = evt;
        if (type === MENU_DEF_TYPES.ITEM) {
            this.props.dispatch(command, commandArgs);
            this.setState({
                activeKeys: [],
            });
        }
    }

    /**
     * @inheritdoc
     */
    getID() {
        return VIEW_IDS.APP_MENU;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="application-menu">
                <Menu
                    mode="horizontal"
                    onClick={this.onMenuItemClick}
                    selectable={false}
                    onOpenChange={(openKeys) => {
                        this.setState({
                            activeKeys: openKeys,
                        });
                    }}
                    openKeys={this.state.activeKeys}
                >
                    {this.props.menu.map(root => this.renderMenuNode(root))}
                </Menu>
            </div>
        );
    }

    /**
     * Render a menu item
     * @param {Object} item Menu Definition
     * @returns {React.Component}
     */
    renderMenuNode(item) {
        const { type, id, label } = item;
        const shortcutLabel = this.props.getLabelForCommand(item.command);
        const children = [];
        if (!_.isNil(item.children) && !_.isEqual(type, MENU_DEF_TYPES.ITEM)) {
            item.children.forEach((child) => {
                if (_.get(child, 'divider.before', false)) {
                    children.push((<Divider key={`${child.id}.divider.before`} />));
                }
                children.push(this.renderMenuNode(child));
                if (_.get(child, 'divider.after', false)) {
                    children.push((<Divider key={`${child.id}.divider.after`} />));
                }
            });
        }

        switch (type) {
            case MENU_DEF_TYPES.ROOT:
                return (
                    <SubMenu className="root-item" disabled={!item.isActive()} title={label} key={id}>
                        {children}
                    </SubMenu >
                );
            case MENU_DEF_TYPES.GROUP:
                return (
                    <SubMenu
                        disabled={!item.isActive()}
                        title={
                            <div style={{ display: 'block', minHeight: '14px', paddingRight: '10px' }}>
                                <div style={{ position: 'absolute', left: '5px' }}>
                                    <i className={`fw fw-${item.icon}`} />
                                </div>
                                <div className="menu-label" style={{ position: 'absolute', left: '25px' }}>
                                    {label}
                                </div>
                                <div style={{ position: 'absolute', right: '0px' }}>
                                    <i className={'fw fw-right'} style={{ marginRight: '5px' }} />
                                </div>
                                <div style={{ opacity: '0', margin: '0 25px 0 25px', cursor: 'default' }}>
                                    {label}
                                </div>
                            </div>
                        }
                        key={id}
                    >
                        {children}
                    </SubMenu >
                );
            case MENU_DEF_TYPES.ITEM:
                return (
                    <MenuItem disabled={!item.isActive()} key={id} menuDef={item}>
                        <div style={{ display: 'block', minHeight: '14px', paddingRight: '10px' }}>
                            <div style={{ position: 'absolute', left: '7px' }}>
                                <i className={`fw fw-${item.icon}`} />
                            </div>
                            <div className="menu-label" style={{ position: 'absolute', left: '27px' }}>
                                {label}
                            </div>
                            <div className="menu-label" style={{ position: 'absolute', right: '7px' }}>
                                {shortcutLabel}
                            </div>
                            <div style={{ opacity: '0', margin: '0 25px 0 25px', cursor: 'default' }}>
                                {label} {shortcutLabel}
                            </div>
                        </div>
                    </MenuItem>
                );
            default:
                return (<div />);
        }
    }
}

ApplicationMenu.propTypes = {
    dispatch: PropTypes.func.isRequired,
    getLabelForCommand: PropTypes.func.isRequired,
    menu: PropTypes.arrayOf(Object).isRequired,
};

export default ApplicationMenu;
