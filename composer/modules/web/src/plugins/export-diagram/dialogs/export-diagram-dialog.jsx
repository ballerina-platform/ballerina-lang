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

import React from 'react';
import _ from 'lodash';
import $ from 'jquery';
import { getPathSeperator, getUserHome } from 'api-client/api-client';
import PropTypes from 'prop-types';
import { Button, Form, Select, Input } from 'semantic-ui-react';
import Dialog from 'core/view/Dialog';
import FileTree from 'core/view/tree-view/FileTree';
import { createOrUpdate, exists as checkFileExists } from 'core/workspace/fs-util';
import { DIALOGS } from 'core/workspace/constants';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import ScrollBarsWithContextAPI from 'core/view/scroll-bars/ScrollBarsWithContextAPI';

const FILE_TYPE = 'file';
const HISTORY_LAST_ACTIVE_PATH = 'composer.history.workspace.export-diagram-dialog.last-active-path';

/**
 * File Save Wizard Dialog
 * @extends React.Component
 */
class ExportDiagramDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = props.appContext.pref;
        const filePath = history.get(HISTORY_LAST_ACTIVE_PATH) || '';
        this.state = {
            error: '',
            filePath,
            fileName: '',
            fileType: '',
            showDialog: true,
        };
        this.onFileSave = this.onFileSave.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * @inheritDoc
     * */
    componentDidMount() {
        if (!this.state.filePath) {
            getUserHome()
                .then((userHome) => {
                    this.setState({
                        filePath: userHome,
                    });
                });
        }
    }

    /**
     * Called when user clicks open
     */
    onFileSave() {
        const { filePath, fileName, fileType } = this.state;
        if (fileName.trim() === '') {
            this.setState({
                error: 'File name cannot be empty',
            });
            return;
        }
        if (filePath.trim() === '') {
            this.setState({
                error: 'File path cannot be empty',
            });
            return;
        }
        const derivedFileType = !_.endsWith(fileName, '.svg') && !_.endsWith(fileName, '.png')
            ? (!_.isEmpty(fileType) ? fileType : 'SVG')
            : (_.endsWith(fileName, '.svg') ? 'SVG' : 'PNG');
        const derivedFilePath = !_.endsWith(filePath, getPathSeperator())
            ? filePath + getPathSeperator() : filePath;
        const derivedFileName = !_.endsWith(fileName, '.svg') && !_.endsWith(fileName, '.png')
            ? (`${fileName}.${derivedFileType.toLowerCase()}`)
            : fileName;

        const saveFile = (content) => {
            createOrUpdate(derivedFilePath, derivedFileName, content, true)
                .then((success) => {
                    const { history } = this.props.appContext.pref;
                    history.put(HISTORY_LAST_ACTIVE_PATH, filePath);
                    this.setState({
                        error: '',
                        filePath,
                        showDialog: false,
                    });
                    this.props.onSaveSuccess();
                })
                .catch((error) => {
                    this.setState({
                        error: error.message,
                    });
                    this.props.onSaveFail();
                });
        };

        checkFileExists(derivedFilePath + derivedFileName)
            .then(({ exists }) => {
                if (!exists) {
                    this.sendPayload(derivedFilePath, derivedFileName, derivedFileType, saveFile);
                } else {
                    this.props.command.dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id: DIALOGS.REPLACE_FILE_CONFIRM,
                        additionalProps: {
                            filePath: derivedFilePath + derivedFileName,
                            onConfirm: () => {
                                this.sendPayload(derivedFilePath, derivedFileName, derivedFileType, saveFile);
                            },
                            onCancel: () => {
                                this.props.onSaveFail();
                            },
                        },
                    });
                }
            })
            .catch((error) => {
                this.setState({
                    error: error.message,
                });
                this.props.onSaveFail();
            });
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            error: '',
            showDialog: false,
        });
    }

    /**
     * Get the diagram SVG to export.
     *
     * @return {string} svg.
     * */
    getSVG() {
        const tab = $(('#bal-file-editor-' + this.props.file.id).replace(/(:|\.|\[|\]|\/|,|=)/g, '\\$1'));
        const svgElement = tab.find('.svg-container');
        const svgElementClone = tab.find('.svg-container').clone(true);

        // Create a svg element with actual height and width of the diagram.
        let svg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xml:space="preserve" 
                style='fill:rgb(255,255,255);font-family:"Roboto",Arial,Helvetica,sans-serif;
                font-size:14px;' width="${svgElement.width()}" height="${svgElement.height()}">
                <rect style="fill: rgb(255,255,255);fill-opacity: 1;" width="${svgElement.width()}"
                height="${svgElement.height()}" x="0" y="0"></rect>`;

        this.elementIterator(svgElement, svgElementClone);
        svg += svgElementClone.html();
        svg += '</svg>';
        return svg;
    }

    /**
     * allocate style from diagram to exporting html.
     *
     * @param {element} from - element to get the style from.
     * @param {element} to - element to set the style.
     * @return {null} nullable value.
     * */
    styleAllocator(from, to) {
        let computedStyleObject = false;
        // trying to figure out which style object we need to use depense on the browser support
        // so we try until we have one
        if (typeof from !== 'number') {
            computedStyleObject = from.currentStyle || document.defaultView.getComputedStyle(from, null);
        }

        // if the browser dose not support both methods we will return null
        if (!computedStyleObject) return null;

        const stylePropertyValid = function (name, value) {
            // checking that the value is not a undefined
            return typeof value !== 'undefined' &&
                // checking that the value is not a object
                typeof value !== 'object' &&
                // checking that the value is not a function
                typeof value !== 'function' &&
                // checking that we dosent have empty string
                value.length > 0 &&
                // checking that the property is not int index ( happens on some browser
                value !== parseInt(value);
        };

        // we iterating the computed style object and copy the style props and the values
        for (const property in computedStyleObject) {
            // checking if the property and value we get are valid sinse browser have different implementations
            if (stylePropertyValid(property, computedStyleObject[property])) {
                if (this.filterStyleProperties(property)) {
                    if (property === 'width') {
                        to.style[property] = from.width ? ('' + from.width.baseVal.value) : 'auto';
                    } else if (property === 'height') {
                        to.style[property] = from.height ? ('' + from.height.baseVal.value) : 'auto';
                    } else if (property === 'visibility') {
                        if (computedStyleObject[property] === 'hidden') {
                            to.style[property] = computedStyleObject[property];
                        }
                    } else {
                        // applying the style property to the target element
                        to.style[property] = computedStyleObject[property];
                    }
                }
            }
        }

        return null;
    }

    /**
     * filter style properties to be applied to svg.
     *
     * @param {string} property - name of the property.
     * @return {boolean} can apply.
     * */
    filterStyleProperties(property) {
        let canApply = false;
        switch (property) {
            case 'height':
                canApply = true;
                break;
            case 'width':
                canApply = true;
                break;
            case 'fill':
                canApply = true;
                break;
            case 'fillOpacity':
                canApply = true;
                break;
            case 'fillRule':
                canApply = true;
                break;
            case 'marker':
                canApply = true;
                break;
            case 'markerStart':
                canApply = true;
                break;
            case 'markerMid':
                canApply = true;
                break;
            case 'markerEnd':
                canApply = true;
                break;
            case 'stroke':
                canApply = true;
                break;
            case 'strokeDasharray':
                canApply = true;
                break;
            case 'strokeDashoffset':
                canApply = true;
                break;
            case 'strokeLinecap':
                canApply = true;
                break;
            case 'strokeMiterlimit':
                canApply = true;
                break;
            case 'strokeOpacity':
                canApply = true;
                break;
            case 'strokeWidth':
                canApply = true;
                break;
            case 'textRendering':
                canApply = true;
                break;
            case 'textAnchor':
                canApply = true;
                break;
            case 'alignmentBaseline':
                canApply = true;
                break;
            case 'baselineShift':
                canApply = true;
                break;
            case 'dominantBaseline':
                canApply = true;
                break;
            case 'glyph-orientation-horizontal':
                canApply = true;
                break;
            case 'glyph-orientation-vertical':
                canApply = true;
                break;
            case 'kerning':
                canApply = true;
                break;
            case 'stopColor':
                canApply = true;
                break;
            case 'stopOpacity':
                canApply = true;
                break;
            case 'visibility':
                canApply = true;
                break;
            default:
                canApply = false;
                break;
        }
        return canApply;
    }

    /**
     * Iterate through styles.
     *
     * @param {element} svgOri - original svg.
     * @param {element} svgClone - cloned svg.
     * */
    elementIterator(svgOri, svgClone) {
        if (svgOri.children().length !== 0) {
            for (let i = 0; i < svgOri.children().length; i++) {
                this.elementIterator($(svgOri.children()[i]), $(svgClone.children()[i]));
            }
        }
        this.styleAllocator(svgOri[0], svgClone[0]);
        $(svgClone).removeClass();
    }

    /**
     * send the payload to the backend.
     *
     * @param {string} location - location to save the file.
     * @param {string} configName - file name.
     * @param {string} fileType - file type.
     * @param {function} callServer - call back to server.
     * */
    sendPayload(location, configName, fileType, callServer) {
        const svgContent = this.getSVG();
        if (fileType === 'SVG') {
            callServer(btoa(svgContent));
        } else if (fileType === 'PNG') {
            const tab = $(('#bal-file-editor-' + this.props.file.id).replace(/(:|\.|\[|\]|\/|,|=)/g, '\\$1'));
            const svgElement = tab.find('.svg-container');
            const canvas = $(`<canvas width='${svgElement.width()}' height='${svgElement.height()}'/>`)[0];
            const ctx = canvas.getContext('2d');
            const image = new Image();
            image.onload = function load() {
                ctx.drawImage(image, 0, 0);
                const png = canvas.toDataURL('image/png');
                let img = png.replace('data:image/png;base64,', '');
                img = img.replace(' ', '+');
                callServer(img);
            };
            image.src = 'data:image/svg+xml;charset-utf-8,' + svgContent;
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <Dialog
                    show={this.state.showDialog}
                    title='Export Diagram'
                    actions={
                        <Button
                            primary
                            onClick={this.onFileSave}
                            disabled={this.state.filePath === '' && this.state.fileName === ''}
                        >
                            Export
                        </Button>
                    }
                    closeDialog
                    onHide={this.onDialogHide}
                    error={this.state.error}
                >
                    <Form
                        widths='equal'
                        onSubmit={(e) => {
                            this.onFileSave();
                        }}
                    >
                        <Form.Group controlId='filePath'>
                            <Form.Input
                                fluid
                                className='inverted'
                                label='File Path'
                                placeholder='eg: /home/user/diagrams'
                                value={this.state.filePath}
                                onChange={(evt) => {
                                    this.setState({
                                        error: '',
                                        filePath: evt.target.value,
                                    });
                                }}
                            />
                        </Form.Group>
                        <Form.Group controlId='fileName' inline className='inverted'>
                            <Form.Field width={3} htmlFor='fileName'>
                                <label>File Name</label>
                            </Form.Field>
                            <Form.Field width={10} className='inverted'>
                                <Input
                                    type='text'
                                    value={this.state.fileName}
                                    onChange={(evt) => {
                                        this.setState({
                                            error: '',
                                            fileName: evt.target.value,
                                        });
                                    }}
                                />
                            </Form.Field>
                            <Form.Select
                                placeholder='Type'
                                className='inverted'
                                width={3}
                                compact
                                options={[{ text: 'SVG', value: 'SVG' }, { text: 'PNG', value: 'PNG' }]}
                            />

                        </Form.Group>
                    </Form>
                    <ScrollBarsWithContextAPI
                        style={{
                            height: 300,
                        }}
                        autoHide
                    >
                        <FileTree
                            activeKey={this.state.filePath}
                            onSelect={
                                (node) => {
                                    let filePath = node.id;
                                    let fileName = this.state.fileName;
                                    if (node.type === FILE_TYPE) {
                                        filePath = node.filePath;
                                        fileName = node.fileName + '.' + node.extension;
                                    }
                                    const { history } = this.props.appContext.pref;
                                    history.put(HISTORY_LAST_ACTIVE_PATH, filePath);
                                    this.setState({
                                        error: '',
                                        filePath,
                                        fileName,
                                    });
                                }
                            }
                        />
                    </ScrollBarsWithContextAPI>
                </Dialog>
            </div>
        );
    }
}

ExportDiagramDialog.propTypes = {
    appContext: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.objectOf(Object).isRequired,
    onSaveSuccess: PropTypes.func,
    onSaveFail: PropTypes.func,
    command: PropTypes.objectOf(Object).isRequired,
};

ExportDiagramDialog.defaultProps = {
    onSaveSuccess: () => {
    },
    onSaveFail: () => {
    },
};

export default ExportDiagramDialog;
