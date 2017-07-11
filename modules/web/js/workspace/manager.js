/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import $ from 'jquery';
import _ from 'lodash';
import log from 'log';
import 'bootstrap';
import SaveFileDialog from 'dialog/save-to-file-dialog';
import SettingsDialog from 'dialog/settings-dialog';
import FolderOpenDialog from 'dialog/folder-open-dialog';
import FileOpenDialog from 'dialog/open-file-dialog';
import CloseConfirmDialog from 'dialog/close-confirm-dialog';
import ReplaceConfirmDialog from 'dialog/replace-confirm-dialog';
import NewItemDialog from 'dialog/new-item-dialog';
import DeleteItemDialog from 'dialog/delete-item-dialog';
import SwaggerImportDialog from 'dialog/swagger-import-dialog';
import WelcomePage from 'welcome-page/first-launch-welcome';
import Tab from 'tab/tab';
import FileTab from 'tab/file-tab';
import alerts from 'alerts';
import ServiceClient from './service-client';
import DocerinaFile from '../docerina/docerina-file';
import ExportDiagram from 'dialog/export-diagram-dialog';

// workspace manager constructor
/**
 * Arg: application instance
 */
class WorkspaceManager {

    /**
     * Creates an instance of WorkspaceManager.
     * @param {Object} application The application for the composer.
     * @param {Object} application.commandManager The command manager of the application.
     * @memberof WorkspaceManager
     */
    constructor(application) {
        this.app = application;
        this._serviceClient = new ServiceClient({application: this.app});

        if (_.isUndefined(this.app.commandManager)) {
            const error = 'CommandManager is not initialized.';
            log.error(error);
            throw error;
        }
        this.app.commandManager.registerHandler('create-new-tab', this.createNewTab, this);

        this.app.commandManager.registerHandler('undo', this.handleUndo, this);

        this.app.commandManager.registerHandler('redo', this.handleRedo, this);

        this.app.commandManager.registerHandler('save', this.handleSave, this);

        // Open file save dialog
        this.app.commandManager.registerHandler('open-file-save-dialog', this.openFileSaveDialog, this);

        // Open file open dialog
        this.app.commandManager.registerHandler('open-file-open-dialog', this.openFileOpenDialog, this);

        // Open settings dialog
        this.app.commandManager.registerHandler('open-settings-dialog', this.openSettingsDialog, this);

        this.app.commandManager.registerHandler('show-folder-open-dialog', this.showFolderOpenDialog, this);

        this.app.commandManager.registerHandler('open-close-file-confirm-dialog', this.openCloseFileConfirmDialog,
            this);

        this.app.commandManager.registerHandler('open-replace-file-confirm-dialog', this.openReplaceFileConfirmDialog,
            this);

        // Go to Welcome Page.
        this.app.commandManager.registerHandler('go-to-welcome-page', this.goToWelcomePage);

        this.app.commandManager.registerHandler('show-about-dialog', this.showAboutDialog);

        this.app.commandManager.registerHandler('create-new-item-at-path', this.handleCreateNewItemAtPath, this);

        this.app.commandManager.registerHandler('remove-from-disk', this.handleRemoveFromDisk, this);

        // Go to User Guide.
        this.app.commandManager.registerHandler('go-to-user-guide', this.showUserGuide, this);

        // Go to Report Issues.
        this.app.commandManager.registerHandler('go-to-report-issue', this.showReportIssue, this);

        // Import a swagger definition.
        this.app.commandManager.registerHandler('import-swagger-def', this.importSwaggerDefinition, this);

        // Open documentation for the given package.
        this.app.commandManager.registerHandler('open-documentation', this.showDocumentationFor, this);

        // Open Export Diagram
        this.app.commandManager.registerHandler('export-diagram', this.exportDiagram, this);
    }

    /**
     * Gets the service client.
     *
     * @returns {ServiceClient} The client.
     * @memberof WorkspaceManager
     */
    getServiceClient() {
        return this._serviceClient;
    }

    listenToTabController() {
        this.app.tabController.on('active-tab-changed', this.onTabChange, this);
    }

    onTabChange() {
        this.updateMenuItems();
    }

    createNewTab(options) {
        this.app.tabController.newTab(options);
    }

    displayInitialTab() {
        const startupFile = _.get(this.app.config, 'startupFile');
        if (!_.isNil(startupFile)) {
            // open provided startup file
            this.app.commandManager.dispatch('open-file', startupFile);
        } else {
            // user has no active tabs from last session
            if (!this.app.tabController.hasFilesInWorkingSet()) {
                // create a generic tab - without ballerina editor components
                const tab = this.app.tabController.newTab({
                    tabModel: Tab,
                    tabOptions: {title: 'welcome-page'},
                });
                // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                const opts = _.get(this.app.config, 'welcome');
                _.set(opts, 'application', this.app);
                _.set(opts, 'tab', tab);
                _.set(opts, 'balHome', _.get(this.app.config, 'balHome'));
                this.welcomePage = new WelcomePage(opts);
                this.welcomePage.render();
            }
        }
    }

    /**
     * Showing the welcome page. If "welcome page" exists, then show existing "welcome page", else create new
     * "welcome page".
     * @param workspaceManager - The workspace manager.
     */
    showWelcomePage(workspaceManager) {
        const existingWelcomeTab = _.find(this.app.tabController.getTabList(), tab => tab._title === 'welcome-page');

        if (_.isUndefined(existingWelcomeTab)) {
            // Creating a new welcome tab.
            const tab = this.app.tabController.newTab({
                tabModel: Tab,
                tabOptions: {title: 'welcome-page'},
            });
            // Showing FirstLaunchWelcomePage instead of regularWelcomePage
            const opts = _.get(this.app.config, 'welcome');
            _.set(opts, 'application', this.app);
            _.set(opts, 'tab', tab);
            _.set(opts, 'balHome', _.get(this.app.config, 'balHome'));
            workspaceManager.welcomePage = new WelcomePage(opts);
            workspaceManager.welcomePage.render();
        } else {
            // Showing existing welcome tab.
            this.app.tabController.setActiveTab(existingWelcomeTab);
        }
    }

    showDocumentationFor(packageName, functionName) {
        const tab = this.app.tabController.newTab({
            tabModel: Tab,
            tabOptions: {
                title: 'docs-' + packageName + (functionName ? (':' + functionName) : ''),
            }
        });

        const opts = _.get(this.app.config, 'docs');
        _.set(opts, 'application', this.app);
        _.set(opts, 'tab', tab);
        _.set(opts, 'balHome', _.get(this.app.config, 'balHome'));
        _.set(opts, 'packageName', packageName);
        _.set(opts, 'functionName', functionName);

        let docerinaFile = new DocerinaFile(opts);
        docerinaFile.render();
    }

    openFileSaveDialog(options) {
        if (this.app.isElectronMode()) {
            this.openNativeFileSaveDialog(options);
        } else {
            if (_.isNil(this._saveFileDialog)) {
                this._saveFileDialog = new SaveFileDialog(this.app);
            }
            this._saveFileDialog.render();

            if (!_.isNil(options) && _.isFunction(options.callback)) {
                let isSaved = false;
                this._saveFileDialog.once('save-completed', (success) => {
                    isSaved = success;
                }, this);
                this._saveFileDialog.once('unloaded', () => {
                    options.callback(isSaved);
                }, this);
            }

            this._saveFileDialog.show();
            const activeTab = this.app.tabController.getActiveTab();
            if (!_.isNil(activeTab) && _.isFunction(activeTab.getFile)) {
                const activeFile = activeTab.getFile();
                if (activeFile.isPersisted()) {
                    this._saveFileDialog.once('loaded', function () {
                        this._saveFileDialog.setSelectedFile(activeFile.getPath(), activeFile.getName());
                    }, this);
                }
            }
        }
    }

    /**
     * Export Diagram as a SVG file.
     *
     * @param {object} options - config for the export diagram dialog.
     * */
    exportDiagram(options) {
        if (this.app.isElectronMode()) {
            //TODO: Support export in Electron mode.
        } else {
            if (_.isNil(this._exportDiagram)) {
                this._exportDiagram = new ExportDiagram(this.app);
            }
            this._exportDiagram.render();

            if (!_.isNil(options) && _.isFunction(options.callback)) {
                let isSaved = false;
                this._exportDiagram.once('export-completed', (success) => {
                    isSaved = success;
                }, this);
                this._exportDiagram.once('unloaded', () => {
                    options.callback(isSaved);
                }, this);
            }

            this._exportDiagram.show();
            const activeTab = this.app.tabController.getActiveTab();
            if (!_.isNil(activeTab) && _.isFunction(activeTab.getFile)) {
                const activeFile = activeTab.getFile();
                this._exportDiagram.once('loaded', function () {
                    this._exportDiagram.setDiagramPath(activeTab, activeFile.getPath(), activeFile.getName());
                }, this);
            }
        }
    }

    openNativeFileSaveDialog(options) {
        const renderer = this.app.getNativeRenderProcess();
        const fileSavedCallback = (!_.isNil(options)) ? options.callback : undefined;
        renderer.send('show-file-save-dialog');
        renderer.once('file-save-path-selected', (event, path) => {
            const activeTab = this.app.tabController.getActiveTab();
            if (!_.isNil(activeTab) && _.isFunction(activeTab.getFile)) {
                const activeFile = activeTab.getFile();
                const folderPath = path.substring(0, path.lastIndexOf(this.app.getPathSeperator()));
                const fileName = path.substring(path.lastIndexOf(this.app.getPathSeperator()) + 1);
                const config = activeTab.getBallerinaFileEditor().getContent();

                activeFile.setPath(folderPath)
                    .setName(fileName)
                    .setContent(config);

                const result = this._serviceClient.writeFile(activeFile);
                if (!_.isNil(fileSavedCallback) && _.isFunction(fileSavedCallback)) {
                    if (_.isNil(result) || result.error) {
                        fileSavedCallback(false);
                        if (!_.isNil(result.message)) {
                            alerts.error(result.message);
                        }
                    } else {
                        fileSavedCallback(true);
                    }
                }
            }
        });
    }

    openSettingsDialog() {
        /* var settingsModal = $(_.get(app, 'config.settings_dialog.selector'));
         settingsModal.modal('show');*/
        if (_.isNil(this._openSettingsDialog)) {
            const opts = _.cloneDeep(_.get(this.app.config, 'settings_dialog'));
            _.set(opts, 'application', this.app);
            this._openSettingsDialog = new SettingsDialog(opts);
        }
        this._openSettingsDialog.render();
        this._openSettingsDialog.show();
    }

    showFolderOpenDialog() {
        if (this.app.isElectronMode()) {
            this.openNativeFolderOpenDialog();
        } else {
            if (_.isNil(this._folderOpenDialog)) {
                const opts = _.cloneDeep(_.get(this.app.config, 'open_folder_dialog'));
                _.set(opts, 'application', this.app);
                this._folderOpenDialog = new FolderOpenDialog(opts);
            }
            this._folderOpenDialog.render();
            this._folderOpenDialog.show();
        }
    }

    openNativeFolderOpenDialog() {
        const renderer = this.app.getNativeRenderProcess();
        renderer.send('show-folder-open-dialog');
        renderer.once('folder-opened', (event, path) => {
            this.app.commandManager.dispatch('open-folder', path);
        });
    }

    openFileOpenDialog() {
        if (this.app.isElectronMode()) {
            this.openNativeFileOpenDialog();
        } else {
            if (_.isNil(this._openFileDialog)) {
                this._openFileDialog = new FileOpenDialog(this.app);
            }
            this._openFileDialog.render();
            this._openFileDialog.show();
        }
    }

    openNativeFileOpenDialog() {
        const renderer = this.app.getNativeRenderProcess();
        renderer.send('show-file-open-dialog');
        renderer.once('file-opened', (event, path) => {
            this.app.commandManager.dispatch('open-file', path);
        });
    }

    openCloseFileConfirmDialog(options) {
        if (_.isNil(this._closeFileConfirmDialog)) {
            this._closeFileConfirmDialog = new CloseConfirmDialog();
            this._closeFileConfirmDialog.render();
        }
        this._closeFileConfirmDialog.askConfirmation(options);
    }

    openReplaceFileConfirmDialog(options) {
        if (_.isNil(this._replaceFileConfirmDialog)) {
            this._replaceFileConfirmDialog = new ReplaceConfirmDialog();
        }
        // This dialog need to be re-rendered so that it comes on top of save file dialog.
        this._replaceFileConfirmDialog.render();

        this._replaceFileConfirmDialog.askConfirmation(options);
    }

    goToWelcomePage() {
        this.workspaceManager.showWelcomePage(this.workspaceManager);
    }

    updateUndoRedoMenus() {
        // undo manager for current tab
        const activeTab = this.app.tabController.getActiveTab();

        const undoMenuItem = this.app.menuBar.getMenuItemByID('edit.undo');
        const redoMenuItem = this.app.menuBar.getMenuItemByID('edit.redo');

        if (activeTab instanceof FileTab) {
            const undoManager = activeTab.getUndoManager();
            if (undoManager.hasUndo()) {
                undoMenuItem.enable();
                undoMenuItem.addLabelSuffix(
                    undoManager.undoStackTop().getTitle());
            } else {
                undoMenuItem.disable();
                undoMenuItem.clearLabelSuffix();
            }
            if (undoManager.hasRedo()) {
                redoMenuItem.enable();
                redoMenuItem.addLabelSuffix(
                    undoManager.redoStackTop().getTitle());
            } else {
                redoMenuItem.disable();
                redoMenuItem.clearLabelSuffix();
            }
        } else {
            undoMenuItem.disable();
            undoMenuItem.clearLabelSuffix();
            redoMenuItem.disable();
            redoMenuItem.clearLabelSuffix();
        }
    }

    updateMenuItems() {
        this.updateUndoRedoMenus();
        this.updateSaveMenuItem();
        this.updateCodeFormatMenu();
    }

    updateSaveMenuItem() {
        const activeTab = this.app.tabController.getActiveTab();
        const saveMenuItem = this.app.menuBar.getMenuItemByID('file.save');
        const saveAsMenuItem = this.app.menuBar.getMenuItemByID('file.saveAs');
        if (activeTab instanceof FileTab) {
            const file = activeTab.getFile();
            if (file.isDirty()) {
                saveMenuItem.enable();
                saveAsMenuItem.enable();
            } else {
                saveMenuItem.disable();
            }
        } else {
            saveMenuItem.disable();
            saveAsMenuItem.disable();
        }
    }

    updateCodeFormatMenu() {
        const activeTab = this.app.tabController.getActiveTab();
        const codeFormatMenuItem = this.app.menuBar.getMenuItemByID('code.format');
        if (activeTab instanceof FileTab) {
            const fileEditor = activeTab.getBallerinaFileEditor();
            if (!_.isNil(fileEditor) && fileEditor.isInSourceView()) {
                codeFormatMenuItem.enable();
            } else {
                codeFormatMenuItem.disable();
            }
        } else {
            codeFormatMenuItem.disable();
        }
    }

    handleUndo() {
        // undo manager for current tab
        const undoManager = this.app.tabController.getActiveTab().getUndoManager();
        if (undoManager.hasUndo()) {
            undoManager.undo();
        }
        this.updateUndoRedoMenus();
    }

    handleRedo() {
        // undo manager for current tab
        const undoManager = this.app.tabController.getActiveTab().getUndoManager();
        if (undoManager.hasRedo()) {
            undoManager.redo();
        }
        this.updateUndoRedoMenus();
    }

    handleSave(options) {
        const activeTab = this.app.tabController.getActiveTab();
        if (activeTab instanceof FileTab) {
            const file = activeTab.getFile();
            if (file.isPersisted()) {
                if (file.isDirty()) {
                    const response = this._serviceClient.writeFile(file);
                    if (response.error) {
                        alerts.error(response.message);
                        return;
                    }
                }
                if (!_.isNil(options) && _.isFunction(options.callback)) {
                    options.callback(true);
                }
            } else {
                this.app.commandManager.dispatch('open-file-save-dialog', options);
            }
        }
    }

    showAboutDialog() {
        const aboutDialog = $(_.get(this, 'config.about_dialog.selector'));
        aboutDialog.modal('show');
    }

    showUserGuide() {
        window.open(this.app.config.menu_bar.help_urls.user_guide_url);
    }

    showReportIssue() {
        window.open(this.app.config.menu_bar.help_urls.report_issue_url);
    }

    handleCreateNewItemAtPath(data) {
        if (_.isNil(this._newItemDialog)) {
            this._newItemDialog = new NewItemDialog({application: this.app});
            this._newItemDialog.render();
        }
        this._newItemDialog.displayWizard(data);
    }

    handleRemoveFromDisk(data) {
        if (_.isNil(this._deleteItemWizard)) {
            this._deleteItemWizard = new DeleteItemDialog({application: this.app});
            this._deleteItemWizard.render();
        }
        this._deleteItemWizard.displayWizard(data);
    }

    /**
     * On click event which open the swagger import modal/dialog.
     *
     * @memberof WorkspaceManager
     */
    importSwaggerDefinition() {
        if (_.isNil(this._openSwaggerImportDialog)) {
            this._openSwaggerImportDialog = new SwaggerImportDialog(this.app);
        }
        this._openSwaggerImportDialog.render();
        this._openSwaggerImportDialog.show();
    }
}

export default WorkspaceManager;
