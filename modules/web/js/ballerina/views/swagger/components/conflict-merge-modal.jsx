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
import PropTypes from 'prop-types';
import $ from 'jquery';
import ServiceDefinitionAST from './../../../ast/service-definition';
import ResourceDefinitionAST from './../../../ast/resource-definition';
import SwaggerParser from '../../../../swagger-parser/swagger-parser';
import SwaggerJsonVisitor from './../../../visitors/swagger-json-gen/service-definition-visitor';
import SwaggerView from './../swagger-view';

/**
 * Modal for resolving merge conflicts.
 *
 * @class ConflictMergeModal
 * @extends {React.Component}
 */
class ConflictMergeModal extends React.Component {

    /**
     * Creates an instance of ConflictMergeModal.
     * @param {any} props React properties.
     * @memberof ConflictMergeModal
     */
    constructor(props) {
        super(props);
        this.state = {
            mergeData: this.props.missingOriginalResourceDefs.map((resourceDef) => {
                return {
                    resourceDef,
                    delete: true,
                };
            }),
        };

        this.onDeleteResources = this.onDeleteResources.bind(this);
    }

    /**
     * Show the modal on mount.
     *
     * @memberof ConflictModal
     */
    componentDidMount() {
        $(this.modal).modal('show');
    }

    /**
     * Deletes the resource which were selected to be deleted.
     * @memberof ConflictMergeModal
     */
    onDeleteResources() {
        this.state.mergeData.forEach((mergeInstance) => {
            this.props.swaggerParser.mergeToService(this.props.originalServiceDef);
            if (mergeInstance.delete) {
                this.props.originalServiceDef.getResourceDefinitions().forEach((resourceDef) => {
                    if (resourceDef.getResourceName() === mergeInstance.resourceDef.getResourceName()) {
                        this.props.originalServiceDef.removeChild(mergeInstance.resourceDef);
                    }
                });
            }
        });

        const swaggerJsonVisitor = new SwaggerJsonVisitor();
        this.props.originalServiceDef.accept(swaggerJsonVisitor);
        this.props.swaggerView.getSwaggerData().swagger = swaggerJsonVisitor.getSwaggerJson();
        this.props.swaggerView.updateSpecView(this.props.swaggerView.getSwaggerData().swagger);

        this.hideModal();
    }

    /**
     * Hides the modal.
     * @memberof ConflictModal
     */
    hideModal() {
        $(this.modal).modal('hide');
    }

    /**
     * Changes the delete option to the opposite.
     * @param {Object} mergeInstance An instance of a merge object. See constructor.
     * @memberof ConflictMergeModal
     */
    changeDeleteOption(mergeInstance) {
        mergeInstance.delete = !mergeInstance.delete;
    }

    /**
     * Renders the modal for merging conflicts.
     * @returns {ReactElement} The view.
     * @memberof ConflictMergeModal
     */
    render() {
        const resourcesToUpdate = this.state.mergeData.map((mergeInstance) => {
            const keepCheckBox = (<span>
                <input
                    type="checkbox"
                    defaultChecked={mergeInstance.delete}
                    onChange={() => this.changeDeleteOption(mergeInstance)}
                /> Delete
                </span>);

            return (<tr key={mergeInstance.resourceDef.getID()}>
                <td>{mergeInstance.resourceDef.getResourceName()}</td>
                <td>{keepCheckBox}</td>
            </tr>);
        });

        return (<div
            className="modal fade"
            id="swagger-conflict-modal"
            tabIndex="-1"
            role="dialog"
            aria-hidden="true"
            ref={(ref) => { this.modal = ref; }}
        >
            <div className="modal-dialog swagger-conflict-modal-dialog" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h4 className="modal-title swagger-conflict-dialog-title">Following resources have be deleted.
                            How would you like to continue ?</h4>
                        <hr className="style1" />
                    </div>
                    <div className="modal-body">
                        <div className="container-fluid">
                            <div className="modal-body">
                                <div className="container-fluid">
                                    <form className="form-horizontal">
                                        <div className="form-group">
                                            <table className="table table-inverse">
                                                <thead>
                                                    <tr>
                                                        <th>Resource</th>
                                                        <th>Action</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {resourcesToUpdate}
                                                </tbody>
                                            </table>
                                            <div className="form-group">
                                                <div className="folder-dialog-form-btn">
                                                    <button
                                                        type="button"
                                                        className="btn btn-primary"
                                                        onClick={this.onDeleteResources}
                                                    >Done</button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>);
    }
}

ConflictMergeModal.propTypes = {
    missingOriginalResourceDefs: PropTypes.arrayOf(PropTypes.instanceOf(ResourceDefinitionAST)).isRequired,
    originalServiceDef: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
    swaggerParser: PropTypes.instanceOf(SwaggerParser).isRequired,
    swaggerView: PropTypes.instanceOf(SwaggerView).isRequired,
};

export default ConflictMergeModal;
