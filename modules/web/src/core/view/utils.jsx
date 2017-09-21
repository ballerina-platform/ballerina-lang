import React from 'react';
import PropTypes from 'prop-types';
import { ContextMenuTrigger, ContextMenu, MenuItem } from 'react-contextmenu';

const MENU_TYPE = 'MULTI';

const targets = [{
    name: 'Banana'
}, {
    name: 'Apple'
}, {
    name: 'Papaya'
}, {
    name: 'Mango'
}, {
    name: 'Orange'
}, {
    name: 'Pineapple'
}];

function collect(props) {
    return { name: props.name };
}

export function withContextMenu(Trigger) {
    class ContextMenuTriggerWrapper extends React.Component {
        constructor(props) {
            super(props);
    
            this.state = { logs: [] };
        }
    
        handleClick(e, data, target) {
            const count = parseInt(target.getAttribute('data-count'), 10);
    
            if (data.action === 'Added') {
                target.setAttribute('data-count', count + 1);
    
                return this.setState(({ logs }) => ({
                    logs: [`${data.action} 1 ${data.name}`, ...logs]
                }));
            }
    
            if (data.action === 'Removed' && count > 0) {
                target.setAttribute('data-count', count - 1);
    
                return this.setState(({ logs }) => ({
                    logs: [`${data.action} 1 ${data.name}`, ...logs]
                }));
            }
    
            return this.setState(({ logs }) => ({
                logs: [` ${data.name} cannot be ${data.action.toLowerCase()}`, ...logs]
            }));
        }
    
        render() {
            return (
                <div>
                    <ContextMenuTrigger
                        id={MENU_TYPE}
                        holdToDisplay={1000}
                        collect={collect}
                    >
                        <Trigger {...this.props} />
                    </ContextMenuTrigger>
                    <ContextMenu id={MENU_TYPE}>
                        <MenuItem onClick={this.handleClick} data={{ action: 'Added' }}>Add 1 count</MenuItem>
                        <MenuItem onClick={this.handleClick} data={{ action: 'Removed' }}>Remove 1 count</MenuItem>
                    </ContextMenu>
                </div>
            );
        }
    }

    ContextMenuTriggerWrapper.contextTypes = {
        command: PropTypes.shape({
            on: PropTypes.func,
            dispatch: PropTypes.func,
        }).isRequired,
    };

    return ContextMenuTriggerWrapper;
}
