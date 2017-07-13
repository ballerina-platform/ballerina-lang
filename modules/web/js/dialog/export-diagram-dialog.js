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
import _ from 'lodash';
import $ from 'jquery';
import log from 'log';
import Backbone from 'backbone';
import FileBrowser from 'file_browser';
import 'bootstrap';
import './dialog.css';

/**
 * Class for export diagram diagram.
 *
 * @class ExportDiagramDialog
 * */
const ExportDiagramDialog = Backbone.View.extend({
    /**
     * @augments Backbone.View
     * @constructs
     * @class SaveToFileDialog
     * @param {Object} options configuration options for the SaveToFileDialog
     */
    initialize(options) {
        this.app = options;
        this.dialogContainer = _.get(options.config.dialog, "container");
        this.notificationContainer = _.get(options.config.tab_controller.tabs.tab.ballerina_editor.notifications,
            'container');
    },

    /**
     * Show export diagram modal.
     * */
    show() {
        this._exportDiagramModal.modal('show').on('shown.bs.modal', () => {
            this.trigger('loaded');
        });
        this._exportDiagramModal.on('hidden.bs.shown', () => {
            this.trigger('unloaded');
        });
    },

    /**
     * Set diagram export path.
     *
     * @param {object} activeTab - active tab object.
     * @param {string} path - active file path.
     * @param {fileName} fileName - file name.
     * */
    setDiagramPath(activeTab, path, fileName) {
        this._fileBrowser.select(path);
        this._activeTabId = activeTab.id;
        if (!_.isNil(this._configNameInput)) {
            if (fileName.endsWith('.bal')) {
                fileName = fileName.replace('.bal', '');
            }
            this._configNameInput.val(fileName);
        }
    },

    /**
     * Render the export modal.
     * */
    render() {
        const self = this;
        let fileBrowser;
        const app = this.app;
        const notificationContainer = this.notificationContainer;
        const css = this.css;
        if (!_.isNil(this._exportDiagramModal)) {
            this._exportDiagramModal.remove();
        }

        const fileSave = $(
            "<div class='modal fade' id='saveConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
            "<div class='modal-dialog file-dialog' role='document'>" +
            "<div class='modal-content'>" +
            "<div class='modal-header'>" +
            "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
            "<span aria-hidden='true'>&times;</span>" +
            '</button>' +
            "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Export Diagram</h4>" +
            "<hr class='style1'>" +
            '</div>' +
            "<div class='modal-body'>" +
            "<div class='container-fluid'>" +
            "<form class='form-horizontal' onsubmit='return false'>" +
            "<div class='form-group'>" +
            "<label for='configName' class='col-sm-2 file-dialog-label'>File Name :</label>" +
            "<div class='col-sm-9'>" +
            "<input class='file-dialog-form-control' id='configName' placeholder='eg: diagram.svg'>" +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<label for='location' class='col-sm-2 file-dialog-label'>Location :</label>" +
            "<div class='col-sm-9'>" +
            "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-diagrams'>" +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<div class='file-dialog-form-scrollable-block'>" +
            "<div id='fileTree'>" +
            '</div>' +
            "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
            '</div>' +
            '</div>' +
            '</div>' +
            '<div class="form-group">' +
            '<div class="file-type-selector">' +
            '<select id="fileType" class="file-type-list btn btn-default">' +
            '<option class="file-type-item">SVG</option>' +
            '<option class="file-type-item">PNG</option>' +
            '</select>' +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<div class='file-dialog-form-btn'>" +
            "<button id='saveButton' type='button' class='btn btn-primary'>Export" +
            '</button>' +
            "<div class='divider'/>" +
            "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
            '</div>' +
            '</div>' +
            '</form>' +
            "<div id='newWizardError' class='alert alert-danger'>" +
            '<strong>Error!</strong> Something went wrong.' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>',
        );

        const successNotification = $(
            "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>" +
            "<span class='notification'>" +
            'Diagram successfully exported !' +
            '</span>' +
            '</div>');

        function getErrorNotification(detailedErrorMsg) {
            let errorMsg = 'Error While Exporting Diagram';
            if (!_.isEmpty(detailedErrorMsg)) {
                errorMsg += (`: ${ detailedErrorMsg}`);
            }
            return $(
                `${"<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                "<span class='notification'>"}${
                    errorMsg
                    }</span>` +
                '</div>'
            );
        }

        const exportDiagramModal = fileSave.filter('#saveConfigModal');
        const newWizardError = fileSave.find('#newWizardError');
        const location = fileSave.find('input').filter('#location');
        const configName = fileSave.find('input').filter('#configName');
        const fileType = fileSave.find('select').filter('#fileType');

        const treeContainer = fileSave.find('div').filter('#fileTree');
        fileBrowser = new FileBrowser({
            container: treeContainer,
            application: app,
            fetchFiles: false
        });

        fileBrowser.render();
        this._fileBrowser = fileBrowser;
        this._configNameInput = configName;

        this.listenTo(fileBrowser, 'selected', (selectedLocation) => {
            if (selectedLocation) {
                location.val(selectedLocation);
            }
        });

        function handleExport() {
            const _location = location.val();
            let _configName = configName.val();
            let _fileType = fileType.val();

            if (_.isEmpty(_location)) {
                newWizardError.text('Please enter a valid file export location');
                newWizardError.show();
                return;
            }

            if (_.isEmpty(_configName)) {
                newWizardError.text('Please enter a valid file name');
                newWizardError.show();
                return;
            }

            if (_configName.endsWith('.bal')) {
                _configName = _configName.replace('.bal', '');
            }

            if (!_configName.endsWith('.svg') && !_configName.endsWith('.png')) {
                if (_fileType === 'SVG') {
                    _configName += '.svg';
                    fileType.val('SVG');
                } else if (_fileType === 'PNG') {
                    _configName += '.png';
                    fileType.val('PNG');
                }
            } else {
                if (_configName.endsWith('.svg')) {
                    fileType.val('SVG');
                } else if (_configName.endsWith('.png')) {
                    fileType.val('PNG');
                }
            }

            const callback = function (isSaved) {
                self.trigger('save-completed', isSaved);
                if (isSaved) {
                    exportDiagramModal.modal('hide');
                }
            };

            const client = self.app.workspaceManager.getServiceClient();
            const path = `${_location}/${_configName}`;
            const existsResponse = client.exists(path);

            if (existsResponse.exists) {
                const replaceConfirmCb = function (confirmed) {
                    if (confirmed) {
                        exportDiagram({
                            location: _location,
                            configName: _configName,
                            fileType: _fileType,
                        }, callback);
                    } else {
                        callback(false);
                    }
                };

                const options = {
                    path,
                    handleConfirm: replaceConfirmCb,
                };

                self.app.commandManager.dispatch('open-replace-file-confirm-dialog', options);
            } else {
                exportDiagram({
                    location: _location,
                    configName: _configName,
                    fileType: _fileType,
                }, callback);
            }
        }

        fileSave.find('button').filter('#saveButton').click(() => {
            handleExport();
        });

        function handleKeyPress(e) {
            if (e.keyCode === 13 || e.which === 13) {
                e.stopPropagation();
                e.preventDefault();
                handleExport();
            }
        }

        location.keypress(handleKeyPress);
        configName.keypress(handleKeyPress);

        $(this.dialogContainer).append(fileSave);
        newWizardError.hide();
        this._exportDiagramModal = fileSave;
        function alertSuccess() {
            $(notificationContainer).append(successNotification);
            successNotification.fadeTo(2000, 200).slideUp(1000, () => {
                successNotification.slideUp(1000);
            });
        }

        function alertError(errorMessage) {
            const errorNotification = getErrorNotification(errorMessage);
            $(notificationContainer).append(errorNotification);
            errorNotification.fadeTo(2000, 200).slideUp(1000, () => {
                errorNotification.slideUp(1000);
            });
        }

        /**
         * Get the diagram SVG to export.
         *
         * @return {element} svg.
         * */
        function getSVG() {
            let tab = $("#" + app.tabController.activeTab.id);
            let svgElement = tab.find('.svg-container');
            let svgElementClone = tab.find('.svg-container').clone(true);

            let svg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xml:space="preserve" 
                style='fill:rgb(255,255,255);font-family:"Roboto",Arial,Helvetica,
                sans-serif;font-size:14px;' width="${svgElement.width()}" height="${svgElement.height()}"><rect
                style="fill: rgb(255,255,255);fill-opacity: 1;" width="${svgElement.width()}"
                height="${svgElement.height()}" x="0" y="0"></rect>`;
            elementIterator(svgElement, svgElementClone);
            svg += svgElementClone.html();
            svg += "</svg>";
            return svg;
        }

        /**
         * filter style properties to be applied to svg.
         *
         * @param {string} property - name of the property.
         * @return {boolean} can apply.
         * */
        function filterStyleProperties(property) {
            let canApply = false;
            switch (property) {
                case "height":
                    canApply = true;
                    break;
                case "width":
                    canApply = true;
                    break;
                case "fill":
                    canApply = true;
                    break;
                case "fillOpacity":
                    canApply = true;
                    break;
                case "fillRule":
                    canApply = true;
                    break;
                case "marker":
                    canApply = true;
                    break;
                case "markerStart":
                    canApply = true;
                    break;
                case "markerMid":
                    canApply = true;
                    break;
                case "markerEnd":
                    canApply = true;
                    break;
                case "stroke":
                    canApply = true;
                    break;
                case "strokeDasharray":
                    canApply = true;
                    break;
                case "strokeDashoffset":
                    canApply = true;
                    break;
                case "strokeLinecap":
                    canApply = true;
                    break;
                case "strokeMiterlimit":
                    canApply = true;
                    break;
                case "strokeOpacity":
                    canApply = true;
                    break;
                case "strokeWidth":
                    canApply = true;
                    break;
                case "textRendering":
                    canApply = true;
                    break;
                case "textAnchor":
                    canApply = true;
                    break;
                case "alignmentBaseline":
                    canApply = true;
                    break;
                case "baselineShift":
                    canApply = true;
                    break;
                case "dominantBaseline":
                    canApply = true;
                    break;
                case "glyph-orientation-horizontal":
                    canApply = true;
                    break;
                case "glyph-orientation-vertical":
                    canApply = true;
                    break;
                case "kerning":
                    canApply = true;
                    break;
                case "stopColor":
                    canApply = true;
                    break;
                case "stopOpacity":
                    canApply = true;
                    break;
                case "visibility":
                    canApply = true;
                    break;
                default:
                    canApply = false;
                    break;
            }
            return canApply;
        }

        /**
         * allocate style from diagram to exporting html.
         *
         * @param {element} from - element to get the style from.
         * @param {element} to - element to set the style.
         * */
        function styleAllocator(from, to) {
            let computed_style_object = false;
            //trying to figure out which style object we need to use depense on the browser support
            //so we try until we have one
            if (typeof from !== 'number') {
                computed_style_object = from.currentStyle || document.defaultView.getComputedStyle(from, null);
            }

            //if the browser dose not support both methods we will return null
            if (!computed_style_object) return null;

            let stylePropertyValid = function (name, value) {
                //checking that the value is not a undefined
                return typeof value !== 'undefined' &&
                    //checking that the value is not a object
                    typeof value !== 'object' &&
                    //checking that the value is not a function
                    typeof value !== 'function' &&
                    //checking that we dosent have empty string
                    value.length > 0 &&
                    //checking that the property is not int index ( happens on some browser
                    value !== parseInt(value)
            };

            //we iterating the computed style object and compy the style props and the values
            for (let property in computed_style_object) {
                //checking if the property and value we get are valid sinse browser have different implementations
                if (stylePropertyValid(property, computed_style_object[property])) {
                    if (filterStyleProperties(property)) {
                        if (property === "width") {
                            to.style[property] = from.width ? ("" + from.width.baseVal.value) : "auto";
                        } else if (property === "height") {
                            to.style[property] = from.height ? ("" + from.height.baseVal.value) : "auto";
                        } else if (property === "visibility") {
                            if (computed_style_object[property] === "hidden") {
                                to.style[property] = computed_style_object[property];
                            }
                        } else {
                            //applying the style property to the target element
                            to.style[property] = computed_style_object[property];
                        }
                    }
                }
            }
        }

        /**
         * Iterate through styles.
         *
         * @param {element} svgOri - original svg.
         * @param {element} svgClone - cloned svg.
         * */
        function elementIterator(svgOri, svgClone) {
            if (svgOri.children().length !== 0) {
                for (let i = 0; i < svgOri.children().length; i++) {
                    elementIterator($(svgOri.children()[i]), $(svgClone.children()[i]));
                }
            }
            styleAllocator(svgOri[0], svgClone[0]);
            $(svgClone).removeClass();
        }

        /**
         * check whether string is a json string.
         *
         * @param {string} str - json string.
         * @return {boolean} true or false.
         * */
        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        /**
         * send the payload to the backend.
         *
         * @param {string} location - location to save the file.
         * @param {string} configName - file name.
         * @param {string} fileType - file type.
         * @param {function} callServer - call back to server.
         * */
        function sendPayload(location, configName, fileType, callServer) {
            let payload = '';
            const config = getSVG();
            if (fileType === "SVG") {
                payload = `location=${  btoa(location)  }&configName=${  btoa(configName)
                    }&config=${  encodeURIComponent(config)}`;
                callServer(payload);
            } else if (fileType === "PNG") {
                let canvas = $("<canvas width='1541' height='1005'/>")[0];
                let ctx = canvas.getContext("2d");
                let image = new Image();
                image.onload = function load() {
                    ctx.drawImage(image, 0, 0);
                    let png = canvas.toDataURL("image/png");
                    let img = png.replace('data:image/png;base64,', '');
                    img = img.replace(' ', '+');
                    payload = `location=${  btoa(location)  }&configName=${  btoa(configName)
                        }&imageFile=true&config=${encodeURIComponent(img)}`;
                    callServer(payload);
                    $(".diagram").append(`<img src="${png}"/>`);
                };
                image.src = 'data:image/svg+xml;charset-utf-8,' + encodeURIComponent(config);
            }
        }

        /**
         * export the diagram.
         *
         * @param {object} options - options to be passed to backend service.
         * @param {function} callback - call back to be called on success or failure.
         * */
        function exportDiagram(options, callback) {
            const workspaceServiceURL = app.config.services.workspace.endpoint;
            const saveServiceURL = `${workspaceServiceURL}/write`;

            // Ajax call to send the payload to the server.
            let callServer = function (payload) {
                $.ajax({
                    url: saveServiceURL,
                    type: 'POST',
                    data: payload,
                    contentType: 'text/plain; charset=utf-8',
                    async: false,
                    success(data, textStatus, xhr) {
                        if (xhr.status === 200) {
                            exportDiagramModal.modal('hide');
                            log.debug('Diagram successfully exported');
                            callback(true);
                        } else {
                            newWizardError.text(data.Error);
                            newWizardError.show();
                            callback(false);
                        }
                    },
                    error(res, errorCode, error) {
                        let msg = _.isString(error) ? error : res.statusText;
                        if (isJsonString(res.responseText)) {
                            let resObj = JSON.parse(res.responseText);
                            if (_.has(resObj, 'Error')) {
                                msg = _.get(resObj, 'Error');
                            }
                        }
                        newWizardError.text(msg);
                        newWizardError.show();
                        callback(false);
                    },
                });
            };

            // Send the payload as to the file type.
            sendPayload(options.location, options.configName, options.fileType, callServer);
        }
    },
});

export default ExportDiagramDialog;