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
import AceEditor from 'react-ace';
import AutoSuggest from 'ballerina/diagram/views/default/components/utils/autosuggest-html';
// import PropTypes from 'prop-types';
import React from 'react';
// import ServiceDefinition from 'ballerina/ast/service-definition';
import uuid from 'uuid/v1';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/html';
import 'brace/theme/monokai';

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
            appendUrl: this.getAppendUrl(),
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
        };

        this.onAddNewHeader = this.onAddNewHeader.bind(this);
        this.onAppendUrlChange = this.onAppendUrlChange.bind(this);
        this.onContentTypeChange = this.onContentTypeChange.bind(this);
        this.onHeaderDelete = this.onHeaderDelete.bind(this);
        this.onHeaderKeyChange = this.onHeaderKeyChange.bind(this);
        this.onHeaderValueChange = this.onHeaderValueChange.bind(this);
        this.onHttpMethodSelected = this.onHttpMethodSelected.bind(this);
        this.onInvoke = this.onInvoke.bind(this);
        this.onInvokeCancel = this.onInvokeCancel.bind(this);
        this.onRequestBodyChange = this.onRequestBodyChange.bind(this);
    }

    /**
     * Event handler when a new header is added.
     * @memberof HttpClient
     */
    onAddNewHeader() {
        const emptyHeaderIndex = this.state.requestHeaders.findIndex((header) => {
            return header.key === '' && header.value === '';
        });

        if (emptyHeaderIndex === -1) {
            const headerClone = JSON.parse(JSON.stringify(this.state.requestHeaders));
            headerClone.splice(0, 0, { id: uuid(), key: '', value: '' });
            this.setState({
                requestHeaders: headerClone,
            });
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
            if (header.value === headerValue) {
                headers[index].key = event.currentTarget.value;
            }
        });

        this.setState({
            requestHeaders: headerClone,
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
            if (header.key === headerKey) {
                headers[index].value = event.currentTarget.value;
            }
        });

        this.setState({
            requestHeaders: headerClone,
        });
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
     * @param {string} { suggestionValue } The selected value.
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
     * The initial value for the url path.
     * @returns {string} The url path.
     * @memberof HttpClient
     */
    getAppendUrl() {
        return '/';
    }

    /**
     * Gets the supported HTTP methods.
     * @returns {string[]} Http methods.
     * @memberof HttpClient
     */
    getHttpMethods() {
        return ['GET', 'POST', 'PUT', 'HEAD', 'PATCH'];
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
            }
        }

        return 'text';
    }

    /**
     * Renders the view of the headers.
     * @returns {ReactElement} The view of the headers.
     * @memberof HttpClient
     */
    renderHeaders() {
        return this.state.requestHeaders.map((header) => {
            return (<div key={`${header.id}`}>
                <input
                    key={`key-${header.id}`}
                    type='text'
                    value={header.key}
                    onChange={e => this.onHeaderKeyChange(header.value, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                />
                :
                <input
                    key={`value-${header.id}`}
                    type='text'
                    value={header.value}
                    onChange={e => this.onHeaderValueChange(header.key, e)}
                    onBlur={() => { this.focusTarget = undefined; }}
                />
                <i className='fw fw-delete' onClick={() => this.onHeaderDelete(header.key)} />
            </div>);
        });
    }

    /**
     * Renders the send and cancel based on 'waitingForResponse' state.
     * @returns {ReactElement} The button view.
     * @memberof HttpClient
     */
    renderSendOrCancelButton() {
        if (this.state.waitingForResponse === false) {
            return (<button onClick={this.onInvoke} className='btn btn-success'>SEND</button>);
        } else {
            return (<button onClick={this.onInvokeCancel} className='btn btn-danger'>
                <i className="fw fw-loader5 fw-spin fw-1x" />
                CANCEL
                </button>);
        }
    }

    /**
     * Renders the view of the http client.
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    render() {
        const headers = this.renderHeaders();
        const sendOrCancelButton = this.renderSendOrCancelButton();
        return (<div className='http-client-wrapper'>
            <div className='http-client-request'>
                Request
                <hr />
                <div className='http-client-action-wrapper'>
                    <AutoSuggest
                        items={this.getHttpMethods()}
                        onSuggestionSelected={this.onHttpMethodSelected}
                        disableAutoFocus
                        initialValue={this.state.httpMethod}
                        showAllAtStart
                    />
                    <input
                        className='http-client-path'
                        placeholder='/v1/pets?id=5'
                        type='text'
                        value={this.state.appendUrl}
                        onChange={this.onAppendUrlChange}
                    />
                    {sendOrCancelButton}
                </div>
                <div className='http-client-content-type-wrapper'>
                    Content-Type
                    <input
                        className='http-client-content-type'
                        placeholder='application/json'
                        type='text'
                        value={this.state.contentType}
                        onChange={this.onContentTypeChange}
                    />
                </div>
                <div className='http-client-headers-wrapper'>
                    Headers
                    <i className='fw fw-add' onClick={this.onAddNewHeader} />
                    <hr />
                    <div className='current-headers'>
                        {headers}
                    </div>
                </div>
                <div className='http-client-body-wrapper'>
                    Body
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
                        />
                    </div>
                </div>
            </div>
            <div className='http-client-response'>
                Response
                <hr />
                <div className='http-client-response-attributes'>
                    Reponse Code: {this.state.responseCode}
                    <br />
                    Time Consumed: {this.state.timeConsumed} milliseconds
                    <br />
                    Request URL: {this.state.requestUrl}
                </div>
                <div className='http-client-response-content'>
                    <ul className='nav nav-pills'>
                        <li role='presentation' className='active'>
                            <a href='#headers' aria-controls="home" role="tab" data-toggle="tab">Headers</a>
                        </li>
                        <li role='presentation'>
                            <a href='#body' aria-controls="profile" role="tab" data-toggle="tab">Body</a>
                        </li>
                    </ul>
                    <div className="tab-content">
                        <div role="tabpanel" className="tab-pane active" id="headers">
                            <div className='header-content'>
                                <div className='response-headers'>
                                    Response Headers
                                    <hr />
                                    {this.state.responseHeaders}
                                </div>
                                <div className='request-headers'>
                                    Request Headers
                                    <hr />
                                    {this.state.returnedRequestHeaders}
                                </div>
                            </div>
                        </div>
                        <div role="tabpanel" className="tab-pane" id="body">
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
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>);
    }
}

// HttpClient.propTypes = {
//     serviceDefinition: PropTypes.instanceOf(ServiceDefinition).isRequired,
// };

export default HttpClient;
