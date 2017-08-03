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
import GoldenLayout from 'golden-layout';
import Plugin from 'plugin/plugin';
import 'golden-layout/src/css/goldenlayout-base.css';
import 'golden-layout/src/css/goldenlayout-dark-theme.css';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';

/* This is the default layout of the composer third party apps can
 overise this by passing in a special config. */
const defaultLayout = {
    settings: {
        constrainDragToContainer: true,
        reorderEnabled: true,
        selectionEnabled: false,
        popoutWholeStack: false,
        blockedPopoutsThrowError: true,
        closePopoutsOnUnload: true,
        showPopoutIcon: false,
        showMaximiseIcon: false,
        showCloseIcon: false,
    },
    dimensions: {
        borderWidth: 5,
        minItemHeight: 10,
        minItemWidth: 10,
        headerHeight: 20,
        dragProxyWidth: 300,
        dragProxyHeight: 200,
    },
    labels: {
        close: 'close',
        maximise: 'maximise',
        minimise: 'minimise',
        popout: 'open in new window',
    },
    content: [{
        id: 'root',
        type: 'column',
        content: [{
            id: 'header-area',
            type: 'column',
            height: 5,
            content: [{
                header: {
                    show: false,
                },
                splitters: false,
                type: 'component',
                componentName: 'testComponent',
                componentState: { label: 'North' },
            }],
        }, {
            id: 'body',
            type: 'row',
            content: [{
                header: {
                    show: false,
                },
                width: 20,
                id: 'left-panel',
                type: 'component',
                componentName: 'testComponent',
                componentState: { label: 'Left' },
            },
            {
                id: 'right-panel',
                type: 'column',
                content: [{
                    id: 'editor-area',
                    type: 'stack',
                    height: 70,
                    hasHeaders: true,
                    content: [{
                        title: 'file1.bal',
                        type: 'component',
                        componentName: 'testComponent',
                        componentState: { label: 'File1' },
                    },
                    {
                        title: 'file2.bal',
                        type: 'component',
                        componentName: 'testComponent',
                        componentState: { label: 'File2' },
                    }],
                }, {
                    id: 'footer-area',
                    type: 'stack',
                    hasHeaders: true,
                    content: [{
                        title: 'debug output',
                        type: 'component',
                        componentName: 'testComponent',
                        componentState: { label: 'Debug output' },
                    },
                    {
                        title: 'console',
                        type: 'component',
                        componentName: 'testComponent',
                        componentState: { label: 'Console' },
                    }],
                }],
            },
            ],
        },
        ],
    }],
};

/**
 * LayoutManagerPlugin is responsible for loading view components in to the layout.
 *
 * @class LayoutManagerPlugin
 */
class LayoutManagerPlugin extends Plugin {

    constructor() {
        super();
        // this.onDisplayView = this.onDisplayView.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return 'composer.plugin.layout.manager';
    }

    /**
     * @inheritdoc
     */
    init(config) {
        super.init(config);
        // overide default config from passed in layout.
        const glConfig = (this.config && this.config.layout)
                                ? this.config.layout : defaultLayout;
        // create the layout
        this.composerLayout = new GoldenLayout(glConfig);
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        this.render();
    }

    /**
     * @inheritdoc
     */
    getCommandDefinitions() {
        return getCommandDefinitions();
    }

    /**
     * @inheritdoc
     */
    getCommandHandlerDefinitions() {
        return getHandlerDefinitions(this);
    }

    /**
     * Render layout.
     *
     * @memberof LayoutManagerPlugin
     */
    render() {
        this.composerLayout.registerComponent('testComponent', (container, componentState) => {
            container.getElement().html('<h2>' + componentState.label + '</h2>');
        });
        this.composerLayout.init();
    }
}

export default LayoutManagerPlugin;
