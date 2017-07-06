/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const HelpMenu = {
    id: 'help',
    label: 'Help',
    items: [
        {
            id: 'welcome_page',
            label: 'Welcome Page',
            command: {
                id: 'go-to-welcome-page',
            },
        },
        {
            id: 'user_guide',
            label: 'User Guide',
            command: {
                id: 'go-to-user-guide',
                shortcuts: {
                    mac: {
                        key: 'f1',
                        label: 'F1',
                    },
                    other: {
                        key: 'f1',
                        label: 'F1',
                    },
                },
            },
        },
        {
            id: 'report_issue',
            label: 'Report Issue',
            command: {
                id: 'go-to-report-issue',
            },
        },
        {
            id: 'about',
            label: 'About',
            command: {
                id: 'show-about-dialog',
            },
        },
    ],

};

export default HelpMenu;
