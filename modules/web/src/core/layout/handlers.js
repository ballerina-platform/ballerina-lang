import React from 'react';
import ReactDOM from 'react-dom';
import _ from 'lodash';
import log from 'log';
import { COMMANDS, EVENTS, REGIONS } from './constants';
import { COMMANDS as EDITOR_COMMANDS } from './../editor/constants';
import { withReRenderSupport } from './components/utils';

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
            handler: (id) => {
                const view = _.find(layoutManager.views, ['id', id]);
                if (!_.isNil(view)) {
                    const { region, component, propsProvider, pluginID,
                            regionOptions: { tabTitle, tabIcon, customTitleClass } } = view;
                            
                    switch (region) {
                        case REGIONS.EDITOR_TABS: {
                            const { command: { dispatch } } = layoutManager.appContext;
                            dispatch(EDITOR_COMMANDS.OPEN_CUSTOM_EDITOR_TAB, {
                                id,
                                title: tabTitle,
                                icon: tabIcon,
                                customTitleClass,
                                component: withReRenderSupport(component, pluginID),
                                propsProvider,
                            });
                        }
                            break;
                        // TODO: Implement show view for other regions
                        default: log.warn('Cannot find region to render');
                    }
                } else {
                    log.error(`Cannot find a view with id ${id}`);
                }
            },
        },
        {
            cmdID: COMMANDS.HIDE_VIEW,
            handler: (id) => {
                // TODO
            },
        },
        {
            cmdID: COMMANDS.TOGGLE_BOTTOM_PANEL,
            handler: () => {
                layoutManager.trigger(EVENTS.TOGGLE_BOTTOM_PANLEL);
            },
        },
        {
            cmdID: COMMANDS.POPUP_DIALOG,
            handler: (args) => {
                const { id } = args;
                const dialogDef = _.find(layoutManager.dialogs, ['id', id]);
                if (dialogDef) {
                    const container = document.getElementById(layoutManager.config.dialogContainer);
                    const { component, propsProvider } = dialogDef;
                    const root = React.createElement(component, propsProvider(), null);
                    ReactDOM.unmountComponentAtNode(container);
                    ReactDOM.render(root, container);
                } else {
                    log.error(`A Dialog with id ${id} is not found`);
                }
            },
        },
    ];
}
