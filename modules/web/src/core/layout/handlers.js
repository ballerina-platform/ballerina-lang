import React from 'react';
import ReactDOM from 'react-dom';
import _ from 'lodash';
import log from 'log';
import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of layout manager plugin.
 * @param {LayoutManager} LayoutManager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(layoutManager) {
    return [
        {
            cmdID: COMMANDS.SHOW_VIEW,
            handler: (id, region, viewProps) => {
                
            },
        },
        {
            cmdID: COMMANDS.HIDE_VIEW,
            handler: (id) => {
                // TODO
            },
        },
        {
            cmdID: COMMANDS.POPUP_DIALOG,
            handler: (args) => {
                const { id } = args;
                const dialogDef = _.find(layoutManager.dialogs, ['id', id]);
                if (dialogDef) {
                    const { component, propsProvider } = dialogDef;
                    const root = React.createElement(component, propsProvider(), null);
                    ReactDOM.render(root, document.getElementById(layoutManager.config.container));
                } else {
                    log.error(`A Dialog with id ${id} is not found`);
                }
            },
        },
    ];
}
