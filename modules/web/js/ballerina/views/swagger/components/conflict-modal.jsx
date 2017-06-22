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

        this.onDeleteResources = this.onDeleteResources.bind(this);
        this.onKeepResources = this.onKeepResources.bind(this);
        this.onMoreOptions = this.onMoreOptions.bind(this);
        this.hideModal = this.hideModal.bind(this);
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
        this.props.keepResourcesFunc();
        this.hideModal();
    }

    /**
     * Shows the merge conflict modal.
     * @memberof ConflictModal
     */
    onMoreOptions() {
        this.hideModal();
        this.props.moreOptionsFunc();
    }

    /**
     * Hides the modal.
     * @memberof ConflictModal
     */
    hideModal() {
        $(this.modal).modal('hide');
    }

    /**
     * Renders the Conflict modal.
     * @returns {ReactElement} The view.
     * @memberof ConflictModal
     */
    render() {
        const resourceNameComponents = this.props.missingOriginalResourceDefs.map(
            resourceDef => <li key={resourceDef.getID()}>{resourceDef.getResourceName()}</li>);

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
                                            <ul>
                                                {resourceNameComponents}
                                            </ul>
                                        </div>
                                        <div className="form-group">
                                            <div className="folder-dialog-form-btn">
                                                <button
                                                    type="button"
                                                    className="btn btn-primary open-button"
                                                    onClick={this.onDeleteResources}
                                                >Delete All</button>
                                                <button
                                                    type="button"
                                                    className="btn btn-default"
                                                    data-dismiss="modal"
                                                    onClick={this.onKeepResources}
                                                >Keep All</button>
                                                <button
                                                    type="button"
                                                    className="btn btn-default"
                                                    data-dismiss="modal"
                                                    onClick={this.onMoreOptions}
                                                >More Options</button>
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

ConflictModal.propTypes = {
    missingOriginalResourceDefs: PropTypes.arrayOf(PropTypes.instanceOf(ResourceDefinitionAST)).isRequired,
    originalServiceDef: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
    keepResourcesFunc: PropTypes.func.isRequired,
    moreOptionsFunc: PropTypes.func.isRequired,
    swaggerParser: PropTypes.instanceOf(SwaggerParser).isRequired,
};

export default ConflictModal;
