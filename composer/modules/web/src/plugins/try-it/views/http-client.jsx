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
/* eslint max-len: 0 */
import 'brace';
import _ from 'lodash';
import { invokeTryIt, getTryItUrl } from 'api-client/api-client';
import cn from 'classnames';
import AceEditor from 'react-ace';
import { Loader, Container, Accordion, Form, Button, Divider, Segment, Icon, Select } from 'semantic-ui-react';
import copy from 'copy-to-clipboard';
import PropTypes from 'prop-types';
import React from 'react';
import ServiceTreeNode from 'plugins/ballerina/model/tree/service-node';
import uuid from 'uuid/v4';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/html';
import 'brace/theme/monokai';
import RequestHeaderItem from './request-header-item';

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

        const selectedServiceObj = this.getSelectedService(props);

        this.state = {
            httpMethod: 'GET',
            httpMethods: this.getHttpMethods(),
            baseUrls: [],
            baseUrl: '',
            pathUrls: [],
            appendUrl: selectedServiceObj.appendUrl,
            contentType: selectedServiceObj.contentType,
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
            activeIndex: 0,
        };

        this.handleAccordionClick = this.handleAccordionClick.bind(this);
        this.onAddNewHeader = this.onAddNewHeader.bind(this);
        this.onAppendUrlChange = this.onAppendUrlChange.bind(this);
        this.onContentTypeChange = this.onContentTypeChange.bind(this);
        this.onContentTypeSelected = this.onContentTypeSelected.bind(this);
        this.onHeaderDelete = this.onHeaderDelete.bind(this);
        this.onChangeHeader = this.onChangeHeader.bind(this);
        this.onHttpMethodChanged = this.onHttpMethodChanged.bind(this);
        this.onHttpMethodSelected = this.onHttpMethodSelected.bind(this);
        this.onInvoke = this.onInvoke.bind(this);
        this.onInvokeCancel = this.onInvokeCancel.bind(this);
        this.onRequestBodyChange = this.onRequestBodyChange.bind(this);
        this.onServiceSelected = this.onServiceSelected.bind(this);
        this.onResourceSelected = this.onResourceSelected.bind(this);
        this.onChangeUrl = this.onChangeUrl.bind(this);
        this.headerKey = undefined;
        this.handlePathAdditions = this.handlePathAdditions.bind(this);
    }

    /**
     * On component is mount for the first time.
     * @memberof HttpClient
     */
    componentDidMount() {
        getTryItUrl()
            .then((baseUrls) => {
                this.setState({
                    baseUrls: baseUrls || [],
                    baseUrl: baseUrls[0] || '',
                });
            }).catch(() => {
            });
        this.onAddNewHeader(false);
        this.renderPathsDropdown();
    }

    /**
     * On new props are recieved.
     * @param {Object} nextProps Properties
     * @memberof HttpClient
     */
    componentWillReceiveProps(nextProps) {
        const selectedServiceObj = this.getSelectedService(nextProps);
        this.setState({
            selectedService: selectedServiceObj.selectedService,
            selectedResource: selectedServiceObj.selectedResource,
            appendUrl: selectedServiceObj.appendUrl,
            contentType: selectedServiceObj.contentType,
        });
    }

    getSelectedService(props) {
        if (props.serviceNodes.length > 0) {
            let selectedService;
            let selectedResource;
            let selectedContentType = '';
            if (props.serviceNodes.length === 1) {
                selectedService = props.serviceNodes[0];
                if (selectedService.getResources().length === 1) {
                    selectedResource = selectedService.getResources()[0];
                    if (selectedResource.getConsumeTypes().length === 1) {
                        selectedContentType = selectedResource.getConsumeTypes()[0];
                    }
                }
            }
            return { 
                selectedService,
                selectedResource,
                appendUrl: this.compileURL(selectedResource),
                contentType: selectedContentType,
            };
        }
    }

    /**
     * Event handler when a new header is added.
     * @param {boolean} [focus=true] True to focus on empty header key.
     * @memberof HttpClient
     */
    onAddNewHeader() {
        const emptyHeaderIndex = this.state.requestHeaders.findIndex((header) => {
            return header.key.trim() === '' && header.value.trim() === '';
        });
        if (emptyHeaderIndex === -1) {
            this.setState({
                requestHeaders: [...this.state.requestHeaders, { id: uuid(), key: '', value: '' }],
            });
        }
    }

    onChangeUrl(e, data) {
        this.setState({
            baseUrl: data.value,
        });
    }

    /**
     * Event handler when the url path is changed.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onAppendUrlChange(event, data) {
        this.setState({
            appendUrl: data.value,
        });
    }

    /**
     * Event handler when the content type text box is changed.
     * @param {Event} event The change event.
     * @memberof HttpClient
     */
    onContentTypeChange(event, data) {
        this.setState({
            contentType: data.value,
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
    onHeaderDelete(id) {
        const newHeaders = this.state.requestHeaders.filter((header) => {
            return header.id !== id;
        });
        if (newHeaders === undefined || newHeaders.length === 0) {
            this.setState({
                requestHeaders: [{ id: uuid(), key: '', value: '' }],
            });
        } else {
            this.setState({
                requestHeaders: newHeaders,
            });
        }
    }

    onChangeHeader(i, header) {
        const newHeaders = [...this.state.requestHeaders];
        newHeaders[i] = header;
        this.setState({
            requestHeaders: newHeaders,
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
     * Event handler when the http method is changed in the textbox.
     * @param {Object} event The change event.
     * @memberof HttpClient
     */
    onHttpMethodChanged(event, data) {
        this.setState({
            httpMethod: data.value,
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
            activeIndex: 1, // switch accordion active item for response.
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
    onServiceSelected(event, data) {
        this.setState({
            selectedService: data.value,
            selectedResource: undefined,
        });
    }

    /**
     * Event handler when a resource is selected.
     * @param {string} value The selected resource node.
     * @memberof HttpClient
     */
    onResourceSelected(event, { value }) {
        const appendUrl = this.compileURL(value);
        let contentType = '';
        if (value.getConsumeTypes().length === 1) {
            contentType = value.getConsumeTypes()[0];
        }
        this.setState({
            selectedResource: value,
            appendUrl,
            httpMethods: this.getHttpMethods(value),
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
        return [
            { text: 'GET', value: 'GET' },
            { text: 'POST', value: 'POST' },
            { text: 'DELETE', value: 'DELETE' },
            { text: 'PUT', value: 'PUT' },
            { text: 'OPTIONS', value: 'OPTIONS' },
            { text: 'HEAD', value: 'HEAD' },
        ];
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
                const contentType = responseHeaders['Content-Type'] || [];
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

    handleAccordionClick(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;

        this.setState({ activeIndex: newIndex });
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
     * Rendering the services dropdown.
     * @returns {React.Element} The dropdown view.
     * @memberof HttpClient
     */
    renderPathsDropdown() {
        const urlItems = [];
        this.props.serviceNodes.forEach((serviceNode) => {
            serviceNode.getResources().forEach((resourceNode) => {
                const url = resourceNode.compileURL();
                const description = resourceNode.getName().getValue();
                const dropdownItem = (<div>
                    <div className='path'>{url}</div>
                    <div className='description'>{description}</div>
                </div>);
                urlItems.push({
                    text: dropdownItem,
                    value: url,
                });
            });
        });

        this.setState({
            pathUrls: urlItems,
        });
    }

    handlePathAdditions(e, { value }) {
        this.setState({
            pathUrls: [{ text: value, value }, ...this.state.pathUrls],
        });
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
                return ({
                    key: resourceNode.getID(),
                    text: resourceNode.getName().getValue(),
                    value: resourceNode,
                });
            });
        }

        return (
            <Select
                search
                selection
                placeholder='Select Resource'
                options={resourceItems}
                value={this.state.selectedResource}
                onChange={this.onResourceSelected}
            />
        );
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

            return (
                <div
                    className='http-client-main-wrapper inverted'
                >
                    <Form
                        inverted
                        widths='equal'
                    >
                        <Form.Group inline>
                            <Form.Field>
                                <Form.Input type='text' fluid>
                                    <Select
                                        search
                                        selection
                                        options={this.state.httpMethods}
                                        onChange={this.onHttpMethodChanged}
                                        value={this.state.httpMethod}
                                        className='select-method'
                                        defaultValue={this.state.httpMethod}
                                    />
                                    <Select
                                        search
                                        selection
                                        placeholder='Select Url'
                                        options={this.state.baseUrls.map(
                                            (url) => {
                                                return { text: url, value: url };
                                            })
                                        }
                                        onChange={this.onChangeUrl}
                                        value={this.state.baseUrl}
                                    />
                                    <Select
                                        search
                                        allowAdditions
                                        selection
                                        placeholder='Select path'
                                        options={this.state.pathUrls}
                                        value={this.state.appendUrl}
                                        onChange={this.onAppendUrlChange}
                                        onAddItem={this.handlePathAdditions}
                                        className='paths-dropdown'
                                    />
                                    {sendOrCancelButton}
                                    <Button
                                        title='Copy URL'
                                        onClick={() => {
                                            copy(`${httpBaseUrl}${this.state.appendUrl}`);
                                        }}
                                    >
                                        <Icon name='copy' />
                                    </Button>
                                </Form.Input>
                            </Form.Field>

                        </Form.Group>
                    </Form>
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
            return (
                <Button primary className='send-request' onClick={this.onInvoke} >
                    Send
                </Button>
            );
        } else {
            return (
                <Button primary className='cancel-request' onClick={this.onInvokeCancel} >
                    <Loader active inline size='mini' />
                    <span>&nbsp;Cancel</span>
                </Button>
            );
        }
    }

    /**
     * Rendering the request headers.
     * @returns {ReactElement[]} The request header views.
     * @memberof HttpClient
     */
    renderRequestHeaders() {
        return this.state.requestHeaders.map((header) => {
            return (<RequestHeaderItem header={header} key={header.id} />);
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
        const contentTypesItems = contentTypes.map(type => ({ key: type, text: type, value: type }));
        return (
            <Form.Select
                search
                selection
                options={contentTypesItems}
                value={this.state.contentType}
                onChange={this.onContentTypeChange}
                defaultValue={this.state.contentType}
                placeholder='Content Type'
            />
        );
    }

    /**
     * Renders the view of the http client.
     * @returns {ReactElement} The view.
     * @memberof HttpClient
     */
    render() {
        const { requestHeaders, activeIndex } = this.state;
        const mainControlComponent = this.renderMainControlComponent();
        const contentTypesControl = this.renderContentTypes();
        return (
            <Container fluid>
                {mainControlComponent}
                <div className='http-client-wrapper'>
                    <Segment inverted>
                        <Accordion>
                            <Accordion.Title active={activeIndex === 0} index={0} onClick={this.handleAccordionClick}>
                                <Segment inverted>
                                    <h3 className='request-title'>
                                        <Icon name='dropdown' />
                                        Request
                                    </h3>
                                </Segment>
                            </Accordion.Title>
                            <Accordion.Content active={activeIndex === 0} >
                                <div className='http-client-request'>
                                    <Form inverted>
                                        <Form.Group inline>
                                            <label htmlFor='content-types'>Content-Type</label>
                                            {contentTypesControl}
                                        </Form.Group>
                                    </Form>
                                    <Form.Field>
                                        <label htmlFor='headers'>Headers</label>
                                        <div className='current-headers'>
                                            {
                                                requestHeaders.map((header, i) => {
                                                    return (<RequestHeaderItem
                                                        header={header}
                                                        onChange={newHeader => this.onChangeHeader(i, newHeader)}
                                                        onAddNew={this.onAddNewHeader}
                                                        key={header.id}
                                                        onDelete={() => this.onHeaderDelete(header.id)}
                                                    />);
                                                })
                                            }
                                        </div>
                                    </Form.Field>
                                    <Form.Field className='http-client-body-wrapper'>
                                        <label htmlFor='http-body'>Request Body</label>
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
                                    </Form.Field>
                                </div>
                            </Accordion.Content>

                            <Accordion.Title active={activeIndex === 1} index={1} onClick={this.handleAccordionClick}>
                                <Segment inverted>
                                    <h3 className='request-title'>
                                        <Icon name='dropdown' />
                                        Response
                                    </h3>
                                </Segment>
                            </Accordion.Title>
                            <Accordion.Content active={activeIndex === 1}>
                                <div className='http-client-request'>
                                    <Segment inverted>
                                        {
                                            this.state.responseHeaders.length > 0 ? (
                                                <Form>
                                                    <div className='http-client-response-attributes'>
                                                        <p>Request URL :
                                                            <span
                                                                className={cn('attribute-value',
                                                                this.getStatusCodeClass(this.state.responseCode))}
                                                            >
                                                                {this.state.requestUrl}
                                                            </span>
                                                        </p>
                                                        <p>Reponse Code :
                                                            <span
                                                                className={cn('attribute-value',
                                                                    this.getStatusCodeClass(
                                                                        this.state.responseCode))}
                                                            >
                                                                {this.state.responseCode}
                                                            </span>
                                                        </p>
                                                        <p>Request HTTP Method :
                                                            <span
                                                                className={cn('attribute-value',
                                                                    this.getStatusCodeClass(
                                                                        this.state.responseCode))}
                                                            >
                                                                {this.state.responseHttpMethod}
                                                            </span>
                                                        </p>
                                                        <p>Time Consumed :
                                                            <span
                                                                className={cn('attribute-value',
                                                                    this.getStatusCodeClass(
                                                                        this.state.responseCode))}
                                                            >
                                                                {this.state.timeConsumed} ms
                                                            </span>
                                                        </p>
                                                    </div>
                                                    <Form.Group>
                                                        <Form.Field width={16}>
                                                            <div className='header-content'>
                                                                <div
                                                                    className='response-headers'
                                                                >
                                                                    <label htmlFor='reponse-headers'>Response Headers </label>
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

                                                                </div>
                                                                <label htmlFor='response-body'>Response Body</label>
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
                                                                <Divider />
                                                                <div className='request-headers'>
                                                                    <label htmlFor='response-headers'>Request Headers</label>
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
                                                                </div>
                                                            </div>
                                                        </Form.Field>
                                                    </Form.Group>
                                                </Form>
                                            ) : (
                                                <Segment inverted loading={this.state.waitingForResponse} className='response-empty-segment'>
                                                    <p className='response-empty'>
                                                        Click Send button to send a request.
                                                    </p>
                                                </Segment>
                                            )
                                        }

                                    </Segment>
                                </div>
                            </Accordion.Content>
                        </Accordion>
                    </Segment>
                </div>


            </Container >);
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
