/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the 'License"); you may not use this file except
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

import 'brace';
import { invokeTryIt } from 'api-client/api-client';
import cn from 'classnames';
import AceEditor from 'react-ace';
import { DropdownButton, MenuItem } from 'react-bootstrap';
import AutoSuggest from 'ballerina/diagram2/views/default/components/decorators/autosuggest-html';
import PropTypes from 'prop-types';
import React from 'react';
import CompilationUnitTreeNode from 'ballerina/model/tree/compilation-unit-node';
import uuid from 'uuid/v1';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/html';
import 'brace/theme/monokai';

import './http-client.scss';

/**
 * Http try-it client component
 * @class HttpClient
 * @extends {React.Component}
 */
class HttpClient extends React.Component {

    /**
     * Creates an instance of HttpClient.
     * @param {Object} props React properites.
     * @memberof HttpClient
     */
    constructor(props) {
        super(props);
        this.state = {
            httpMethod: 'GET',
            httpMethods: this.getHttpMethods(),
            appendUrl: this.compileURL(),
            contentType: 'application/json',
            responseBody: '',
            responseCode: '',
            responseHeaders: '',
            requestUrl: '',
            requestBody: '',
            requestHeaders: [],
            returnedRequestHeaders: '',
            timeConsumed: '0',
            waitingForResponse: false,
            selectedService: '',
            selectedResource: '',
        };

        this.onAddNewHeader = this.onAddNewHeader.bind(this);
        this.onAppendUrlChange = this.onAppendUrlChange.bind(this);
        this.onContentTypeChange = this.onContentTypeChange.bind(this);
        this.onHeaderDelete = this.onHeaderDelete.bind(this);
        this.onHeaderKeyChange = this.onHeaderKeyChange.bind(this);
        this.onHeaderValueChange = this.onHeaderValueChange.bind(this);
        this.onHeaderValueKeyDown = this.onHeaderValueKeyDown.bind(this);
        this.onHttpMethodSelected = this.onHttpMethodSelected.bind(this);
        this.onInvoke = this.onInvoke.bind(this);
        this.onInvokeCancel = this.onInvokeCancel.bind(this);
        this.onRequestBodyChange = this.onRequestBodyChange.bind(this);
        this.onServiceSelected = this.onServiceSelected.bind(this);
        this.onResourceSelected = this.onResourceSelected.bind(this);

        this.headerKey = undefined;
        this.focusOnHeaderKey = false;
    }

    /**
     * On component is mount for the first time.
     * @memberof HttpClient
     */
    componentDidMount() {
        this.onAddNewHeader(false);
    }

    /**
     * On component did update event.
     * @memberof HttpClient
     */
    componentDidUpdate() {
        if (this.headerKey && this.focusOnHeaderKey) {
            this.headerKey.focus();
        }
        this.focusOnHeaderKey = false;
    }

    /**
     * Event handler when a new header is added.
     * @param {boolean} [focus=true] True to focus on empty header key.
     * @memberof HttpClient
     */
    onAddNewHeader(focus = true) {
        const emptyHeaderIndex = this.state.requestHeaders.findIndex((header) => {
            return header.key.trim() === '' && header.value.trim() === '';
        });

        if (emptyHeaderIndex === -1) {
            const headerClone = JSON.parse(JSON.stringify(this.state.requestHeaders));
            headerClone.push({ id: uuid(), key: '', value: '' });
            this.setState({
                requestHeaders: headerClone,
            });
        }

        if (focus === true) {
            this.focusOnHeaderKey = true;
            if (this.headerKey) {
                this.headerKey.focus();
            }
        }
    }

    /**
     * Event handler when the url path is changed.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onAppendUrlChange(event) {
        this.setState({
            appendUrl: event.target.value,
        });
    }

    /**
     * Event handler when the content type text box is changed.
     * @param {Event} event The change event.
     * @memberof HttpClient
     */
    onContentTypeChange(event) {
        this.setState({
            contentType: event.target.value,
        });
    }

    /**
     * Event handler when a header is deleted/removed.
     * @param {string} headerKey They key or the name of the header.
     * @memberof HttpClient
     */
    onHeaderDelete(headerKey) {
        const newHeaders = this.state.requestHeaders.filter((header) => {
            return header.key !== headerKey;
        });
        this.setState({
            requestHeaders: newHeaders,
        });
    }

    /**
     * Event handler when the name or key of the header is changed.
     * @param {string} headerValue Value of the header.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onHeaderKeyChange(headerValue, event) {
        const headerClone = JSON.parse(JSON.stringify(this.state.requestHeaders));
        headerClone.forEach((header, index, headers) => {
            if (header.id === event.currentTarget.id) {
                headers[index].key = event.currentTarget.value;
            }
        });

        this.setState({
            requestHeaders: headerClone,
        }, function () {
            if (headerClone[headerClone.length - 1].value === '' && headerClone[headerClone.length - 1].key !== '') {
                this.onAddNewHeader(false);
            }
        });
    }

    /**
     * Event handler when the value of the header is changed.
     * @param {string} headerKey The name or key of the header.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onHeaderValueChange(headerKey, event) {
        const headerClone = JSON.parse(JSON.stringify(this.state.requestHeaders));
        headerClone.forEach((header, index, headers) => {
            if (header.id === event.currentTarget.id) {
                headers[index].value = event.currentTarget.value;
            }
        });

        this.setState({
            requestHeaders: headerClone,
        });
    }

    /**
     * Event handler when a key is pressed in a header value.
     * @param {Object} e The keypress event.
     * @memberof HttpClient
     */
    onHeaderValueKeyDown(e) {
        if (e.charCode === 13 || e.key === 'Enter') {
            this.onAddNewHeader(true);
        }
    }

    /**
     * Event handler when the body of the request is changed.
     * @param {string} newValue The new content.
     * @memberof HttpClient
     */
    onRequestBodyChange(newValue) {
        this.setState({
            requestBody: newValue,
        });
    }

    /**
     * Event handler when an http method is selected.
     * @param {Object} event The select event.
     * @param {string} suggestionValue The selected value.
     * @memberof HttpClient
     */
    onHttpMethodSelected(event, { suggestionValue }) {
        this.setState({
            httpMethod: suggestionValue,
        });
    }

    /**
     * Event handler when a request is sent.
     * @memberof HttpClient
     */
    onInvoke() {
        this.setState({
            waitingForResponse: true,
        });
        invokeTryIt(this.state, 'http')
            .then((response) => {
                if (this.state.waitingForResponse === true) {
                    this.setState({
                        requestUrl: response.requestUrl,
                        responseCode: response.responseCode,
                        responseHeaders: JSON.stringify(response.responseHeaders, null, 2),
                        responseBody: response.responseBody,
                        returnedRequestHeaders: JSON.stringify(response.returnedRequestHeaders, null, 2),
                        timeConsumed: response.timeConsumed,
                        waitingForResponse: false,
                    });
                }
            }).catch((error) => {
                console.log(error);
                this.setState({
                    waitingForResponse: false,
                });
            });
    }

    /**
     * Event handler for cancelling the request.
     * @memberof HttpClient
     */
    onInvokeCancel() {
        this.setState({
            waitingForResponse: false,
        });
    }

    /**
     * Event handler when a service is selected.
     * @param {string} eventKey The selected value.
     * @param {Object} event The select event.
     * @memberof HttpClient
     */
    onServiceSelected(eventKey) {
        this.setState({
            selectedService: eventKey,
        });
    }

    /**
     * Event handler when a resource is selected.
     * @param {string} eventKey The selected value.
     * @param {Object} event The select event.
     * @memberof HttpClient
     */
    onResourceSelected(eventKey) {
        const appendUrl = this.compileURL(this.state.selectedService, eventKey);
        this.setState({
            selectedResource: eventKey,
            appendUrl,
            httpMethods: this.getHttpMethods(this.state.selectedService, eventKey),
        });
    }

    /**
     * Gets the supported HTTP methods.
     * @param {string} serviceID The ID of the service.
     * @param {string} resourceID The ID of the resource.
     * @returns {string[]} Http methods.
     * @memberof HttpClient
     */
    getHttpMethods(serviceID, resourceID) {
        return ['GET', 'POST', 'PUT', 'HEAD', 'PATCH'];
        // if (serviceID !== undefined && resourceID !== undefined) {
        //     httpMethods = this.getResourceNode(serviceID, resourceID).getHttpMethodValues().map((method) => {
        //         return _.trim(method, '"');
        //     });
        // }
        // if (httpMethods.length === 0) {
        //     return ['GET'];
        // } else {
        // }
    }

    /**
     * Gets the mode for the ace editor depending on content type.
     * @returns {string} ace mode.
     * @memberof HttpClient
     */
    getRequestBodyMode() {
        if (this.state.contentType.includes('json')) {
            return 'json';
        } else if (this.state.contentType.includes('xml')) {
            return 'xml';
        } else {
            return 'text';
        }
    }

    /**
     * Finds the matching resource for a given service ID and resource ID;
     * @param {string} serviceID The ID of the service.
     * @param {string} resourceID The ID of the resource.
     * @returns {ResourceNode} The matching resource node.
     * @memberof HttpClient
     */
    getResourceNode(serviceID, resourceID) {
        const serviceNodes = this.props.compilationUnit.filterTopLevelNodes({ kind: 'Service' });
        const selectedServiceNode = serviceNodes.filter((serviceNode) => {
            return serviceNode.getID() === serviceID;
        })[0];

        return selectedServiceNode.getResources().filter((resourceNode) => {
            return resourceNode.getID() === resourceID;
        })[0];
    }

    /**
     * Gets the mode for the ace editor depending on accept header in the request.
     * @returns {string} ace mode.
     * @memberof HttpClient
     */
    getResponseBodyMode() {
        if (this.state.returnedRequestHeaders) {
            const requestHeaders = JSON.parse(this.state.returnedRequestHeaders);
            const mimeType = requestHeaders.Accept;
            if (mimeType) {
                if (mimeType.includes('json')) {
                    return 'json';
                } else if (mimeType.includes('xml')) {
                    return 'xml';
                } else if (mimeType.includes('html')) {
                    return 'html';
                }
            }
        }

        return 'text';
    }

    /**
     * Gets the url for invoking a resrouce.
     * @param {string} serviceID The ID of the service.
     * @param {string} resourceID The ID of the resource.
     * @returns {string} The url.
     * @memberof HttpClient
     */
    compileURL(serviceID, resourceID) {
        let url = '/';
        if (serviceID !== undefined && resourceID !== undefined) {
            url = this.getResourceNode(serviceID, resourceID).compileURL();
        }

        return url;
    }

    /**
     * Renders the view of the headers.
     * @returns {ReactElement} The view of the headers.
     * @memberof HttpClient
     */
    renderHeaders() {
        return this.state.requestHeaders.map((header, index) => {
            let removeButton;
            // Remove button is not included for new header fields
            if (index !== this.state.requestHeaders.length - 1) {
                removeButton = <i className='fw fw-delete' onClick={() => this.onHeaderDelete(header.key)} />;
            }
            return (<div key={`${header.id}`} className="form-inline">
                <input
                    id={header.id}
                    key={`key-${header.id}`}
                    ref={(ref) => {
                        if (header.key === '' && header.value === '') {
                            this.headerKey = ref;
                        }
                    }}
                    placeholder='Key'
                    type='text'
                    className="response-header-input form-control"
                    value={header.key}
                    onChange={e => this.onHeaderKeyChange(header.key, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                />
                :
                <input
                    id={header.id}
                    key={`value-${header.id}`}
                    placeholder='Value'
                    type='text'
                    className="response-header-input form-control"
                    value={header.value}
                    onChange={e => this.onHeaderValueChange(header.value, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                    onKeyDown={this.onHeaderValueKeyDown}
                />
                {removeButton}
            </div>);
        });
    }

    /**
     * Renders the service and resource selection component;
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    renderSelectServiceComponent() {
        if (this.props.compilationUnit) {
            const serviceNodes = this.props.compilationUnit.filterTopLevelNodes({ kind: 'Service' });
            const serviceItems = serviceNodes.map((serviceNode) => {
                return (<MenuItem eventKey={serviceNode.getID()}>{serviceNode.getName().getValue()}</MenuItem>);
            });
            let resourceItems = [];
            if (this.state.selectedService && this.state.selectedService.trim() !== '') {
                const selectedServiceNode = serviceNodes.filter((serviceNode) => {
                    return serviceNode.getID() === this.state.selectedService;
                });
                if (selectedServiceNode.length > 0) {
                    resourceItems = selectedServiceNode[0].getResources().map((resourceNode) => {
                        return (<MenuItem eventKey={resourceNode.getID()}>
                            {resourceNode.getName().getValue()}
                        </MenuItem>);
                    });
                }
            }
            return (<div className='row http-client-select-service-wrapper'>
                <div className='selectors service-selector'>
                    <DropdownButton
                        title={this.state.selectedService === '' ? 'Select Service' :
                                    serviceNodes.filter((serviceNode) => {
                                        return serviceNode.getID() === this.state.selectedService;
                                    })[0].getName().getValue()}
                        key='try-it-service-dropdown'
                        onSelect={this.onServiceSelected}
                        eventKey={this.state.selectedService}
                    >
                        {serviceItems}
                    </DropdownButton>
                </div>
                <div
                    className={cn('selectors resource-selector',
                        { hide: this.state.selectedService === undefined || this.state.selectedService.trim() === '' })}
                >
                    <DropdownButton
                        title={this.state.selectedResource === '' ? 'Select Resource' :
                            serviceNodes.filter((serviceNode) => {
                                return serviceNode.getID() === this.state.selectedService;
                            })[0].getResources().filter((resourceNode) => {
                                return resourceNode.getID() === this.state.selectedResource;
                            })[0].getName().getValue()}
                        key='try-it-resource-dropdown'
                        onSelect={this.onResourceSelected}
                    >
                        {resourceItems}
                    </DropdownButton>
                </div>
            </div>);
        } else {
            return (null);
        }
    }

    /**
     * Renders the send and cancel based on 'waitingForResponse' state.
     * @returns {ReactElement} The button view.
     * @memberof HttpClient
     */
    renderSendOrCancelButton() {
        if (this.state.waitingForResponse === false) {
            return (<button onClick={this.onInvoke} className='btn btn-success' type="button">SEND</button>);
        } else {
            return (<button onClick={this.onInvokeCancel} className='btn btn-cancel' type="button">
                <i className="fw fw-loader5 fw-spin fw-1x" />
                CANCEL
                </button>);
        }
    }

    renderResponseHeaders(responseHeaders) {
        if (this.state.responseHeaders !== '') {
            const responseHeaderList = [];

            for (const key in responseHeaders) {
                if (responseHeaders.hasOwnProperty(key)) {
                    responseHeaderList.push({ name: key, value: responseHeaders[key] });
                }
            }

            return responseHeaderList.map((header) => {
                return (<div key={`${header.name}`} className="form-inline">
                    <input
                        key={`key-${header.name}`}
                        ref={(ref) => {
                            if (header.name === '' && header.value === '') {
                                this.headerKey = ref;
                            }
                        }}
                        placeholder='Key'
                        type='text'
                        className="response-header-input form-control"
                        value={header.name}
                        onChange={e => this.onHeaderKeyChange(header.value, e)}
                        onBlur={() => { this.focusTarget = undefined; }}
                    />
                    :
                    <input
                        key={`value-${header.id}`}
                        placeholder='Value'
                        type='text'
                        className="response-header-input form-control"
                        value={header.value}
                        onChange={e => this.onHeaderValueChange(header.name, e)}
                        onBlur={() => { this.focusTarget = undefined; }}
                        onKeyDown={this.onHeaderValueKeyDown}
                        readOnly
                    />
                </div>);
            });
        }

        return this.state.requestHeaders.map((header) => {
            return (<div key={`${header.id}`} className="form-inline">
                <input
                    key={`key-${header.id}`}
                    ref={(ref) => {
                        if (header.key === '' && header.value === '') {
                            this.headerKey = ref;
                        }
                    }}
                    placeholder='Key'
                    type='text'
                    className="header-input form-control"
                    value={header.key}
                    onChange={e => this.onHeaderKeyChange(header.value, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                    readOnly
                />
                :
                <input
                    key={`value-${header.id}`}
                    placeholder='Value'
                    type='text'
                    className="header-input form-control"
                    value={header.value}
                    onChange={e => this.onHeaderValueChange(header.key, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                    onKeyDown={this.onHeaderValueKeyDown}
                />
                <i className='fw fw-delete' onClick={() => this.onHeaderDelete(header.key)} />
            </div>);
        });
    }

    /**
     * Renders the view of the http client.
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    render() {
        const headers = this.renderHeaders();
        const sendOrCancelButton = this.renderSendOrCancelButton();
        const selectServiceComponent = this.renderSelectServiceComponent();
        return (<div className="container-fluid">
            {selectServiceComponent}
            <div className="row http-client-wrapper">
                <div className='col-md-6 http-client-request'>
                    <h3>Request</h3>
                    <hr />
                    <div className="form-horizontal">
                        <div className='http-client-action-wrapper'>
                            <AutoSuggest
                                items={this.state.httpMethods}
                                onSuggestionSelected={this.onHttpMethodSelected}
                                disableAutoFocus
                                initialValue={this.state.httpMethod}
                                showAllAtStart
                                alwaysRenderSuggestions
                            />
                            <div className="input-group">
                                <input
                                    className='http-client-path form-control'
                                    placeholder='/v1/pets?id=5'
                                    type='text'
                                    value={this.state.appendUrl}
                                    onChange={this.onAppendUrlChange}
                                />
                                <span className="input-group-btn">
                                        {sendOrCancelButton}
                                </span>
                            </div>

                        </div>
                        <div className='form-group http-client-content-type-wrapper'>
                            <label className="col-sm-2 control-label">Content-Type : </label>
                            <div className="col-sm-10">
                                <input
                                    className='http-client-content-type form-control'
                                    placeholder='application/json'
                                    type='text'
                                    value={this.state.contentType}
                                    onChange={this.onContentTypeChange}
                                />
                            </div>
                        </div>
                        <div className='http-client-headers-wrapper'>
                            <span className="section-header">Headers</span>
                            <hr />
                            <div className='current-headers'>
                                {headers}
                            </div>
                        </div>
                        <div className='http-client-body-wrapper'>
                            <span className="section-header">Body</span>
                            <hr />
                            <div>
                                <AceEditor
                                    mode={this.getRequestBodyMode()}
                                    theme='monokai'
                                    onChange={this.onRequestBodyChange}
                                    value={this.state.requestBody}
                                    name='RequestBody'
                                    editorProps={{
                                        $blockScrolling: Infinity,
                                    }}
                                    setOptions={{
                                        showLineNumbers: false,
                                    }}
                                    maxLines={Infinity}
                                    minLines={10}
                                    width='auto'
                                    showPrintMargin={false}
                                />
                            </div>
                        </div>
                    </div>
                </div>
                <div className='col-md-6 http-client-response'>
                    <h3>Response</h3>
                    <hr />
                    <div className='http-client-response-attributes'>
                        <strong>Reponse Code</strong> : <span>{this.state.responseCode} </span>
                        <br />
                        <strong>Time Consumed</strong> : <span>{this.state.timeConsumed} ms</span>
                        <br />
                        <strong>Request URL</strong> : <span>{this.state.requestUrl}</span>
                    </div>
                    <div className='http-client-response-content'>
                        <ul className="nav nav-tabs" role="tablist">
                            <li role="presentation" className="active"><a href="#headers" aria-controls="headers" role="tab" data-toggle="tab">Headers</a></li>
                            <li role="presentation"><a href="#body" aria-controls="body" role="tab" data-toggle="tab">Body</a></li>
                        </ul>
                        <div className="tab-content">
                            <div role="tabpanel" className="tab-pane active fade in" id="headers">
                                <div className='header-content'>
                                    <div className='response-headers'>
                                        <span className="section-header">Response Headers</span>
                                        <hr />
                                        {this.state.responseHeaders.length > 0 ? (
                                            <div>
                                                {this.renderResponseHeaders(JSON.parse(this.state.responseHeaders))}
                                            </div>
                                        ) : (
                                            <div className="try-it-message message message-warning">
                                                <p> <i className="icon fw fw-warning"></i>
                                                    Hit the send button to see the headers. </p>
                                            </div>
                                        )}

                                    </div>
                                    <div className='request-headers'>
                                        <span className="section-header">Request Headers</span>
                                        <hr />
                                        {this.state.returnedRequestHeaders.length > 0 ? (
                                            <div>
                                                {this.renderResponseHeaders(
                                                                    JSON.parse(this.state.returnedRequestHeaders))}
                                            </div>
                                        ) : (
                                            <div className="try-it-message message message-warning">
                                                <p><i className="icon fw fw-warning"></i>
                                                    Hit the send button to see the headers.</p>

                                            </div>
                                        )}

                                    </div>
                                </div>
                            </div>
                            <div role="tabpanel" className="tab-pane fade" id="body">
                                <AceEditor
                                    mode={this.getResponseBodyMode()}
                                    theme='monokai'
                                    name='ResponseBody'
                                    value={this.state.responseBody}
                                    editorProps={{
                                        $blockScrolling: Infinity,
                                    }}
                                    setOptions={{
                                        showLineNumbers: false,
                                    }}
                                    maxLines={Infinity}
                                    minLines={10}
                                    readOnly='true'
                                    width='auto'
                                    showPrintMargin={false}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>);
    }
}

HttpClient.propTypes = {
    compilationUnit: PropTypes.instanceOf(CompilationUnitTreeNode),
};

HttpClient.defaultProps = {
    compilationUnit: undefined,
};

export default HttpClient;
