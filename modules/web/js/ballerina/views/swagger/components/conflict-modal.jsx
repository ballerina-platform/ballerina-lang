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
import { Button, Modal } from 'react-bootstrap';
import ServiceDefinitionAST from './../../../ast/service-definition';
import ResourceDefinitionAST from './../../../ast/resource-definition';
import SwaggerParser from '../../../../swagger-parser/swagger-parser';

/**
 * React component for conflict indicating dialog.
 * @class ConflictModal
 * @extends {React.Component}
 */
class ConflictModal extends React.Component {

    /**
     * Creates an instance of ConflictModal.
     * @param {Object} props React properties.
     * @memberof ConflictModal
     */
    constructor(props) {
        super(props);
        this.state = {
            showModal: true,
        };

        this.onDeleteResources = this.onDeleteResources.bind(this);
        this.onKeepResources = this.onKeepResources.bind(this);
        this.onMoreOptions = this.onMoreOptions.bind(this);
        this.hideModal = this.hideModal.bind(this);
    }

    /**
     * Deletes all resources which does not have a mapping.
     * @memberof ConflictModal
     */
    onDeleteResources() {
        this.props.swaggerParser.mergeToService(this.props.originalServiceDef);

        // Removing the missing resource.
        this.props.missingOriginalResourceDefs.forEach((originalResourceDef) => {
            originalResourceDef.getParent().removeChild(originalResourceDef);
        });
        this.hideModal();
    }

    /**
     * Keeps the unmapped resources as it is without deleting.
     * @memberof ConflictModal
     */
    onKeepResources() {
        this.props.swaggerParser.mergeToService(this.props.originalServiceDef);
        this.hideModal();
    }

    /**
     * Shows the merge conflict modal.
     * @memberof ConflictModal
     */
    onMoreOptions() {
        this.hideModal();
        this.props.mergeResourcesFunc();
    }

    /**
     * Hides the modal.
     * @memberof ConflictModal
     */
    hideModal() {
        this.setState({ showModal: false });
    }

    /**
     * Shows the modal.
     * @memberof ConflictModal
     */
    showModal() {
        this.setState({ showModal: true });
    }

    /**
     * Renders the Conflict modal.
     * @returns {ReactElement} The view.
     * @memberof ConflictModal
     */
    render() {
        const resourceNameComponents = this.props.missingOriginalResourceDefs.map(
            resourceDef => <li key={resourceDef.getID()}>{resourceDef.getResourceName()}</li>);

        return (<Modal show={this.state.showModal} onHide={this.hideModal}>
            <Modal.Header closeButton>
                <Modal.Title>Following resources have be deleted. How would you like to continue ?</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <ul>
                    {resourceNameComponents}
                </ul>
            </Modal.Body>
            <Modal.Footer>
                <Button bsStyle="primary" onClick={this.onDeleteResources}>Delete All</Button>
                <Button bsStyle="default" onClick={this.onKeepResources}>Keep All</Button>
                <Button bsStyle="default" onClick={this.onMoreOptions}>More Options</Button>
            </Modal.Footer>
        </Modal>);
    }
}

ConflictModal.propTypes = {
    missingOriginalResourceDefs: PropTypes.arrayOf(PropTypes.instanceOf(ResourceDefinitionAST)).isRequired,
    originalServiceDef: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
    mergeResourcesFunc: PropTypes.func.isRequired,
    swaggerParser: PropTypes.instanceOf(SwaggerParser).isRequired,
};

export default ConflictModal;
