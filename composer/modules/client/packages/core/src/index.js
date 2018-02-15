/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Composer from './app';
import Log from './log/log';
import EventChannel from './event/channel';
import Plugin from './plugin/plugin';
import * as AlertConstants from './alert/constants';
import * as PluginConstants from './plugin/constants';
import * as LayoutConstants from './layout/constants';
import * as EditorConstants from './editor/constants';
import * as MenuConstants from './menu/constants';
import * as WorkspaceConstants from './workspace/constants';
import * as ToolBarConstants from './toolbar/constants';
import * as EditorUtils from './editor/views/utils';
import File from './workspace/model/file';
import Editor from './editor/model/Editor';
import View from './view/view';
import Dialog from './view/Dialog';
import FileTree from './view/tree-view/FileTree';
import UndoableOperation from './editor/undo-manager/undoable-operation';
import * as FSUtils from './workspace/fs-util';
import ScrollBarsWithContextAPI from './view/scroll-bars/ScrollBarsWithContextAPI';

export default {
    Composer,
    Log,
    EventChannel,
    Plugin,
    AlertConstants,
    PluginConstants,
    LayoutConstants,
    EditorConstants,
    WorkspaceConstants,
    ToolBarConstants,
    EditorUtils,
    View,
    ScrollBarsWithContextAPI,
    Dialog,
    File,
    UndoableOperation,
    FSUtils,
    Editor,
    FileTree,
    MenuConstants,
};
