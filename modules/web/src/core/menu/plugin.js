/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import log from 'log';
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, VIEW_IDS, MENU_DEF_TYPES } from './constants';

import AppMenuView from './views/AppMenu';

/**
 * MenuPlugin is responsible for rendering menu items.
 *
 * @class MenuPlugin
 */
class MenuPlugin extends Plugin {

    constructor() {
        super();
        this.menus = [];
        this.roots = [];
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Add a menu definition to application menu.
     *
     * @param {Object} menuDef Menu Definition
     */
    addMenu(menuDef) {
        if (!_.isNil(_.find(this.menus, ['id', menuDef.id]))) {
            log.error('Duplicate menu-definition for menu ' + menuDef.id);
        } else {
            this.menus.push(_.cloneDeep(menuDef));
        }
    }

    /**
     * Generate menu from contributed menu definitions
     *
     */
    generateMenuFromDefinitions() {
        const sortedMenus = _.sortBy(this.menus, ['order']);
        const roots = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.ROOT]);
        const groups = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.GROUP]);
        const items = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.ITEM]);

        const findRoot = (id) => {
            return _.find(roots, ['id', id]);
        };

        const findGroup = (id) => {
            return _.find(groups, ['id', id]);
        };

        const nestMenuUnderParent = (menu) => {
            const { id, parent } = menu;
            if (_.isNil(parent)) {
                log.error('Invalid parent-id provied for menu ' + id);
            } else {
                const parentNode = (findRoot(parent)) ? findRoot(parent) : findGroup(parent);
                if (_.isNil(parentNode)) {
                    log.error('Unable to find a parent menu with id ' + parent);
                } else {
                    if (_.isNil(parentNode.children)) {
                        parentNode.children = [];
                    }
                    parentNode.children.push(menu);
                }
            }
        };

        groups.forEach(nestMenuUnderParent);
        items.forEach(nestMenuUnderParent);

        this.roots = roots;
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        this.generateMenuFromDefinitions();
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.APP_MENU,
                    component: AppMenuView,
                    propsProvider: () => {
                        return {
                            dispatch: this.appContext.command.dispatch,
                            menu: this.roots,
                            getLabelForCommand: (cmdID) => {
                                const cmd = this.appContext.command.findCommand(cmdID);
                                return _.get(cmd, 'shortcut.derived.label', '');
                            },
                        };
                    },
                    region: REGIONS.HEADER,
                    displayOnLoad: true,
                },
            ],
        };
    }
}

export default MenuPlugin;
