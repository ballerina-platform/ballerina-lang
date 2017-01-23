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

define(([],function (){
    var EditMenu = {
        id: "edit",
        label: "Edit",
        items: [
            {
                id: "undo",
                label: "Undo",
                command: {
                    id: "undo",
                    shortcuts: {
                        mac: {
                            key: "command+z",
                            label: "\u2318Z"
                        },
                        other: {
                            key: "ctrl+z",
                            label: "Ctrl+Z"
                        }
                    }
                },
                disabled: true
            },
            {
                id: "redo",
                label: "Redo",
                command: {
                    id: "redo",
                    shortcuts: {
                        mac: {
                            key: "command+shift+z",
                            label: "\u2318\u21E7Z"
                        },
                        other: {
                            key: "ctrl+shift+z",
                            label: "Ctrl+Shift+Z"
                        }
                    }
                },
                disabled: true
            }

        ]

    };

    return EditMenu;
}));