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
import { Button, Modal, Table, Checkbox } from 'react-bootstrap';
import ServiceDefinitionAST from './../../../ast/service-definition';
import ResourceDefinitionAST from './../../../ast/resource-definition';
import SwaggerParser from '../../../../swagger-parser/swagger-parser';

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
            showModal: true,
            mergeData: this.props.missingOriginalResourceDefs.map((resourceDef) => {
                return {
                    resourceDef,
                    delete: true,
                };
            }),
        };
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

        this.hideModal();
    }

    /**
     * Hides the modal.
     * @memberof ConflictMergeModal
     */
    hideModal() {
        this.setState({ showModal: false });
    }

    /**
     * Show the modal.
     * @memberof ConflictMergeModal
     */
    showModal() {
        this.setState({ showModal: true });
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
            let keepCheckBox = (<Checkbox
                inline
                onChange={this.changeDeleteOption(mergeInstance)}
            >Keep as it is</Checkbox>);

            if (mergeInstance.delete) {
                keepCheckBox = (<Checkbox
                    checked
                    inline
                    onChange={this.changeDeleteOption(mergeInstance)}
                >Keep as it is</Checkbox>);
            }

            return (<tr key={mergeInstance.resourceDef.getID()}>
                <td>{mergeInstance.resourceDef.getResourceName()}</td>
                <td>{keepCheckBox}</td>
            </tr>);
        });

        return (<Modal show={this.state.showModal} onHide={this.hideModal}>
            <Modal.Header closeButton>
                <Modal.Title>Following resources have be deleted. How would you like to continue ?</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Table bsClass="table table-inverse" striped bordered condensed hover>
                    <thead>
                        <tr>
                            <th>Resource</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {resourcesToUpdate}
                    </tbody>
                </Table>
            </Modal.Body>
            <Modal.Footer>
                <Button bsStyle="primary" onClick={this.onDeleteResources}>Done</Button>
            </Modal.Footer>
        </Modal>);
    }
}

ConflictMergeModal.propTypes = {
    missingOriginalResourceDefs: PropTypes.arrayOf(PropTypes.instanceOf(ResourceDefinitionAST)).isRequired,
    originalServiceDef: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
    swaggerParser: PropTypes.instanceOf(SwaggerParser).isRequired,
};

export default ConflictMergeModal;
