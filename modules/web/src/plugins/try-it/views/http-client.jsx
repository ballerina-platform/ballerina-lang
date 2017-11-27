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
import _ from 'lodash';
import { invokeTryIt, getTryItUrl } from 'api-client/api-client';
import cn from 'classnames';
import AceEditor from 'react-ace';
import { DropdownButton, MenuItem } from 'react-bootstrap';
import copy from 'copy-to-clipboard';
import AutoSuggest from 'plugins/ballerina/diagram/views/default/components/decorators/autosuggest-html';
import PropTypes from 'prop-types';
import React from 'react';
import ServiceTreeNode from 'plugins/ballerina/model/tree/service-node';
import uuid from 'uuid/v4';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/html';
import 'brace/theme/monokai';

import './http-client.scss';

const CONTENT_TYPES = [
    'text/css',
    'text/csv',
    'text/html',
    'application/json',
    'application/xml',
];

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
            baseUrl: 'localhost:9090',
            appendUrl: this.compileURL(),
            contentType: '',
            responseBody: '',
            responseCode: '',
            responseHeaders: '',
            responseHttpMethod: '',
            requestUrl: '',
            requestBody: '',
            requestHeaders: [],
            returnedRequestHeaders: '',
            timeConsumed: '0',
            waitingForResponse: false,
            selectedService: undefined,
            selectedResource: undefined,
            showCopyUrlNotification: false,
        };

        this.onAddNewHeader = this.onAddNewHeader.bind(this);
        this.onAppendUrlChange = this.onAppendUrlChange.bind(this);
        this.onContentTypeChange = this.onContentTypeChange.bind(this);
        this.onContentTypeSelected = this.onContentTypeSelected.bind(this);
        this.onHeaderDelete = this.onHeaderDelete.bind(this);
        this.onHeaderKeyChange = this.onHeaderKeyChange.bind(this);
        this.onHeaderValueChange = this.onHeaderValueChange.bind(this);
        this.onHeaderValueKeyDown = this.onHeaderValueKeyDown.bind(this);
        this.onHttpMethodChanged = this.onHttpMethodChanged.bind(this);
        this.onHttpMethodSelected = this.onHttpMethodSelected.bind(this);
        this.onInvoke = this.onInvoke.bind(this);
        this.onInvokeCancel = this.onInvokeCancel.bind(this);
        this.onRequestBodyChange = this.onRequestBodyChange.bind(this);
        this.onServiceSelected = this.onServiceSelected.bind(this);
        this.onResourceSelected = this.onResourceSelected.bind(this);
        this.renderInputComponent = this.renderInputComponent.bind(this);

        this.headerKey = undefined;
        this.focusOnHeaderKey = false;
    }

    /**
     * On component is mount for the first time.
     * @memberof HttpClient
     */
    componentDidMount() {
        getTryItUrl()
            .then((baseUrl) => {
                this.setState({
                    baseUrl,
                });
            }).catch(() => {
            });
        this.onAddNewHeader(false);
    }

    /**
     * On new props are recieved.
     * @param {Object} nextProps Properties
     * @memberof HttpClient
     */
    componentWillReceiveProps(nextProps) {
        if (nextProps.serviceNodes.length > 0) {
            let selectedService;
            let selectedResource;
            let selectedContentType = '';
            if (nextProps.serviceNodes.length === 1) {
                selectedService = nextProps.serviceNodes[0];
                if (selectedService.getResources().length === 1) {
                    selectedResource = selectedService.getResources()[0];
                    if (selectedResource.getConsumeTypes().length === 1) {
                        selectedContentType = selectedResource.getConsumeTypes()[0];
                    }
                }
            }
            this.setState({
                selectedService,
                selectedResource,
                appendUrl: this.compileURL(selectedResource),
                contentType: selectedContentType,
            });
        }
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
     * Event handler on content type is selected.
     * @param {any} event The event
     * @param {any} suggestionValue The selected value.
     * @memberof HttpClient
     */
    onContentTypeSelected(event, { suggestionValue }) {
        this.setState({
            contentType: suggestionValue,
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
     * Event handler when the http method is changed in the textbox.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onHttpMethodChanged(event) {
        this.setState({
            httpMethod: event.target.value,
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
        const tryItPayload = _.cloneDeep(this.state);
        delete tryItPayload.selectedService;
        delete tryItPayload.selectedResource;
        invokeTryIt(tryItPayload, 'http')
            .then((response) => {
                if (this.state.waitingForResponse === true) {
                    this.setState({
                        requestUrl: response.requestUrl,
                        responseCode: response.responseCode,
                        responseHeaders: JSON.stringify(response.responseHeaders, null, 2),
                        responseHttpMethod: this.state.httpMethod,
                        responseBody: response.responseBody,
                        returnedRequestHeaders: JSON.stringify(response.returnedRequestHeaders, null, 2),
                        timeConsumed: response.timeConsumed,
                        waitingForResponse: false,
                    });
                }
            }).catch(() => {
                if (this.state.waitingForResponse === true) {
                    this.context.alert.showError(`Unexpected error occurred while sending request.
                                                Make sure you have entered valid request details.`);
                }
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
            selectedResource: undefined,
        });
    }

    /**
     * Event handler when a resource is selected.
     * @param {string} eventKey The selected resource node.
     * @memberof HttpClient
     */
    onResourceSelected(eventKey) {
        const appendUrl = this.compileURL(eventKey);
        let contentType = '';
        if (eventKey.getConsumeTypes().length === 1) {
            contentType = eventKey.getConsumeTypes()[0];
        }
        this.setState({
            selectedResource: eventKey,
            appendUrl,
            httpMethods: this.getHttpMethods(eventKey),
            contentType,
        });
    }

    /**
     * Gets the supported HTTP methods.
     * @param {ResourceNode} resourceNode The resource node.
     * @returns {string[]} Http methods.
     * @memberof HttpClient
     */
    getHttpMethods() {
        return ['GET', 'POST', 'DELETE', 'PUT', 'OPTIONS', 'HEAD'];
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
            } else {
                const responseHeaders = JSON.parse(this.state.responseHeaders);
                const contentType = responseHeaders['Content-Type'];
                if (contentType.includes('json')) {
                    return 'json';
                } else if (contentType.includes('xml')) {
                    return 'xml';
                } else if (contentType.includes('html')) {
                    return 'html';
                }
            }
        }

        return 'text';
    }

    /**
     * Gets the css class depending on the status code.
     * @param {string} httpCode The http code.
     * @returns {string} The name of the css class.
     * @memberof HttpClient
     */
    getStatusCodeClass(httpCode) {
        if (/1\d\d\b/g.test(httpCode)) {
            return 'http-status-code-1__';
        } else if (/2\d\d\b/g.test(httpCode)) {
            return 'http-status-code-2__';
        } else if (/3\d\d\b/g.test(httpCode)) {
            return 'http-status-code-3__';
        } else if (/4\d\d\b/g.test(httpCode)) {
            return 'http-status-code-4__';
        } else if (/5\d\d\b/g.test(httpCode)) {
            return 'http-status-code-5__';
        }
        return '';
    }

    /**
     * Gets the url for invoking a resrouce.
     * @param {ResourceNode} resourceNode The resourceNode.
     * @returns {string} The url.
     * @memberof HttpClient
     */
    compileURL(resourceNode) {
        let url = '/';
        if (resourceNode !== undefined) {
            url = resourceNode.compileURL();
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
            return (<div key={`${header.id}`} className='form-inline'>
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
                    className='request-header-input form-control'
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
                    className='request-header-input form-control'
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
     * Renders the input view of the http method selection dropdown's input box.
     * @param {any} inputProps The input properties.
     * @returns {React.Element} The view.
     * @memberof HttpClient
     */
    renderInputComponent(inputProps) {
        return (<div className='inputContainer'>
            <input {...inputProps} />
            <i
                className='fw fw-down'
                onClick={(e) => {
                    e.currentTarget.previousElementSibling.focus();
                }}
            />
        </div>);
    }

    /**
     * Rendering the services dropdown.
     * @returns {React.Element} The dropdown view.
     * @memberof HttpClient
     */
    renderServicesDropdown() {
        const serviceItems = this.props.serviceNodes.map((serviceNode) => {
            return (<MenuItem
                key={serviceNode.getID()}
                eventKey={serviceNode}
            >
                {serviceNode.getName().getValue()}
            </MenuItem>);
        });

        // Compiling selected value
        let dropdownTitle;
        if (this.state.selectedService === undefined) {
            dropdownTitle = 'Select Service';
        } else {
            dropdownTitle = this.state.selectedService.getName().getValue();
        }

        return (
            <div className='dropdown-wrapper'>
                <DropdownButton
                    id='services-dropdown'
                    title={dropdownTitle}
                    key='try-it-service-dropdown'
                    onSelect={this.onServiceSelected}
                    noCaret
                >
                    {serviceItems}
                </DropdownButton>
                <i
                    className='fw fw-down'
                    onClick={(e) => {
                        e.currentTarget.previousElementSibling.children[0].click();
                    }}
                />
            </div>
        );
    }

    /**
     * Rendering the resource dropdown.
     * @returns {React.Element} The dropdown view.
     * @memberof HttpClient
     */
    renderResourcesDropdown() {
        let resourceItems = [];
        if (this.state.selectedService) {
            resourceItems = this.state.selectedService.getResources().map((resourceNode) => {
                return (<MenuItem key={resourceNode.getID()} eventKey={resourceNode}>
                    {resourceNode.getName().getValue()}
                </MenuItem>);
            });
        }

        // Compiling selected value
        let dropdownTitle;
        if (this.state.selectedResource === undefined) {
            dropdownTitle = 'Select Resource';
        } else {
            dropdownTitle = this.state.selectedResource.getName().getValue();
        }

        return (<div className='dropdown-wrapper'>
            <DropdownButton
                id='resource-dropdown'
                title={dropdownTitle}
                key='try-it-resource-dropdown'
                onSelect={this.onResourceSelected}
                noCaret
            >
                {resourceItems}
            </DropdownButton>
            <i
                className='fw fw-down'
                onClick={(e) => {
                    e.currentTarget.previousElementSibling.children[0].click();
                }}
            />
        </div>);
    }

    /**
     * Renders the service and resource selection component;
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    renderMainControlComponent() {
        if (this.props.serviceNodes.length > 0) {
            const httpBaseUrl = `http://${this.state.baseUrl}`;
            const sendOrCancelButton = this.renderSendOrCancelButton();

            // Getting service name views
            const servicesDropdown = this.renderServicesDropdown();
            const resourceDropdown = this.renderResourcesDropdown();

            return (<div className='row http-client-main-wrapper'>
                <div className='main-wrapper-top'>
                    <div className='http-client-request-method-wrapper'>
                        <AutoSuggest
                            items={this.state.httpMethods}
                            onSuggestionSelected={this.onHttpMethodSelected}
                            onChange={this.onHttpMethodChanged}
                            disableAutoFocus
                            initialValue={this.state.httpMethod}
                            showAllAtStart
                            alwaysRenderSuggestions
                            renderInputComponent={this.renderInputComponent}
                        />
                    </div>
                    <span className='http-method-separator'>|</span>
                    <div className='http-client-request-url'>
                        {httpBaseUrl}
                    </div>
                    <span className='url-separator'>/</span>
                    <div className='selectors service-selector'>
                        {servicesDropdown}
                    </div>
                    <span className='url-separator'>/</span>
                    <div className='selectors resource-selector'>
                        {resourceDropdown}
                    </div>
                </div>
                <div className='main-wrapper-bottom'>
                    <div className='http-client-request-url'>
                        {httpBaseUrl}
                    </div>
                    <div className='input-group'>
                        <input
                            className='http-client-path form-control'
                            type='text'
                            value={this.state.appendUrl}
                            onChange={this.onAppendUrlChange}
                        />
                        <span className='input-group-btn'> {sendOrCancelButton} </span>
                        <div
                            className='copy-url-wrapper'
                            title='Copy URL'
                            onClick={() => {
                                copy(`${httpBaseUrl}${this.state.appendUrl}`);
                                this.setState({
                                    showCopyUrlNotification: true,
                                });
                            }}
                            onMouseLeave={() => {
                                this.setState({
                                    showCopyUrlNotification: false,
                                });
                            }}
                        >
                            <i className='fw fw-copy-link' />
                            <div
                                className={cn('copy-url-notification', { hide: !this.state.showCopyUrlNotification })}
                            >
                                URL Copied !
                            </div>
                        </div>
                    </div>
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
            return (<button onClick={this.onInvoke} className='btn btn-success' type='button'>Send</button>);
        } else {
            return (<button onClick={this.onInvokeCancel} className='btn btn-cancel' type='button'>
                <i className='fw fw-loader5 fw-spin fw-1x' />
                <span>Cancel</span>
            </button>);
        }
    }

    /**
     * Rendering the request headers.
     * @returns {ReactElement[]} The request header views.
     * @memberof HttpClient
     */
    renderRequestHeaders() {
        return this.state.requestHeaders.map((header) => {
            return (<div key={`${header.id}`} className='form-inline'>
                <input
                    key={`key-${header.id}`}
                    ref={(ref) => {
                        if (header.key === '' && header.value === '') {
                            this.headerKey = ref;
                        }
                    }}
                    placeholder='Key'
                    type='text'
                    className='header-input form-control'
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
                    className='header-input form-control'
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
     * Rendering dropdown for content types.
     * @returns {React.Element} The dropdown view.
     * @memberof HttpClient
     */
    renderContentTypes() {
        let contentTypes = CONTENT_TYPES;
        if (this.state.selectedResource && this.state.selectedResource.getConsumeTypes().length > 0) {
            contentTypes = this.state.selectedResource.getConsumeTypes();
        }

        return (<AutoSuggest
            items={contentTypes}
            onSuggestionSelected={this.onContentTypeSelected}
            onChange={this.onContentTypeChange}
            disableAutoFocus
            initialValue={this.state.contentType}
            showAllAtStart
            alwaysRenderSuggestions
            renderInputComponent={this.renderInputComponent}
        />);
    }

    /**
     * Renders the view of the http client.
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    render() {
        const requestHeaders = this.renderRequestHeaders();
        const mainControlComponent = this.renderMainControlComponent();
        const contentTypesControl = this.renderContentTypes();
        return (<div className='container-fluid'>
            {mainControlComponent}
            <div className='row http-client-wrapper'>
                <div className='http-client-request'>
                    <h3>Request</h3>
                    <hr />
                    <div className='form-horizontal'>
                        <div className='form-group http-client-content-type-wrapper'>
                            <span className='col-sm-2 control-label'>Content-Type : </span>
                            <div className='col-sm-10'>
                                {contentTypesControl}
                            </div>
                        </div>
                        <div className='http-client-headers-wrapper'>
                            <span className='section-header'>Headers</span>
                            <hr />
                            <div className='current-headers'>
                                {requestHeaders}
                            </div>
                        </div>
                        <div className='http-client-body-wrapper'>
                            <span className='section-header'>Body</span>
                            <hr />
                            <div className='ACE-editor-wrapper'>
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
                <div className='http-client-response'>
                    <h3>Response</h3>
                    <hr />
                    <div className='http-client-response-attributes'>
                        <strong>Request URL :
                            <span className={cn('attribute-value', this.getStatusCodeClass(this.state.responseCode))}>
                                {this.state.requestUrl}
                            </span>
                        </strong>
                        <br />
                        <strong>Reponse Code :
                            <span className={cn('attribute-value', this.getStatusCodeClass(this.state.responseCode))}>
                                {this.state.responseCode}
                            </span>
                        </strong>
                        <br />
                        <strong>Request HTTP Method :
                            <span className={cn('attribute-value', this.getStatusCodeClass(this.state.responseCode))}>
                                {this.state.responseHttpMethod}
                            </span>
                        </strong>
                        <br />
                        <strong>Time Consumed :
                            <span className={cn('attribute-value', this.getStatusCodeClass(this.state.responseCode))}>
                                {this.state.timeConsumed} ms
                            </span>
                        </strong>
                    </div>
                    <div className='http-client-response-content'>
                        <div className='header-wrapper'>
                            <div className='header-title section-header'>Headers</div>
                            <div className='header-content'>
                                <div className='response-headers'>
                                    <span className='response-header-title'>Response Headers</span>
                                    {this.state.responseHeaders.length > 0 ? (
                                        <div>
                                            {
                                        Object.entries(JSON.parse(this.state.responseHeaders)).map(([key, value]) => {
                                            return (<div className='header-attribute' key={`response-${key}`}>
                                                <div className='key'>{key}</div>
                                                    :
                                                <div className='value'>{value}</div>
                                            </div>);
                                        })}
                                        </div>
                                    ) : (
                                        <div className='try-it-message message message-warning'>
                                            <p> <i className='icon fw fw-warning' />
                                                Hit the send button to see the headers. </p>
                                        </div>
                                        )}

                                </div>
                                <div className='request-headers'>
                                    <span className='request-headers-title'>Request Headers</span>
                                    {this.state.returnedRequestHeaders.length > 0 ? (
                                        <div>
                                            {
                                Object.entries(JSON.parse(this.state.returnedRequestHeaders)).map(([key, value]) => {
                                    return (<div className='header-attribute' key={`returned-request-${key}`}>
                                        <div className='key'>{key}</div>
                                            :
                                        <div className='value'>{value}</div>
                                    </div>);
                                })}
                                        </div>
                                    ) : (
                                        <div className='try-it-message message message-warning'>
                                            <p><i className='icon fw fw-warning' />
                                                Hit the send button to see the headers.</p>
                                        </div>
                                        )}
                                </div>
                            </div>
                        </div>
                        <div className='response-body-wrapper'>
                            <div className='body-title section-header'>Body</div>
                            <hr />
                            <div className='body-content'>
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
                                    readOnly
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
    serviceNodes: PropTypes.arrayOf(PropTypes.instanceOf(ServiceTreeNode)),
};

HttpClient.defaultProps = {
    serviceNodes: [],
};

HttpClient.contextTypes = {
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
    }).isRequired,
};

export default HttpClient;
