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
    var FileMenu = {
        id: "file",
        label: "File",
        items: [
            {
                id: "new",
                label: "New",
                command: {
                    id: "create-new-tab",
                    shortcuts: {
                        mac: "command+ctrl+n",
                        other: "ctrl+alt+n"
                    }
                },
                disabled: false
            },
            {
                id: "open",
                label: "Open",
                command: {
                    id: "open-file-open-dialog",
                    shortcuts: {
                        mac: "command+o",
                        other: "ctrl+o"
                    }
                },
                disabled: false
            },
            {
                id: "save",
                label: "Save",
                command: {
                    id: "open-file-save-dialog",
                    shortcuts: {
                        mac: "command+s",
                        other: "ctrl+s"
                    }
                },
                disabled: false
            }

            ]

    };

    return FileMenu;
}));