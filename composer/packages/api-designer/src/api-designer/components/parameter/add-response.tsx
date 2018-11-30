/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import * as React from "react";
import { Button, Form, Select } from "semantic-ui-react";

import { OpenApiResponse } from "../../components/parameter/add-response";

export interface OpenApiAddResponseProps {
    openApiJson: any;
    onAddResponse: (response: OpenApiResponse) => void;
    operation: string;
    resourcePath: string;
    handleClose: (isClose: boolean) => void;
}

export interface OpenApiAddResponseState {
    responseStatus: OpenApiResponseCode[];
    responseObject: OpenApiResponse;
}

export interface OpenApiResponse {
    status: string;
    description: string;
    resourcePath: string;
    operation: string;
}

export interface OpenApiResponseCode {
    key: string;
    text: string;
    value: string;
}

class OpenApiAddResponse extends React.Component<OpenApiAddResponseProps, OpenApiAddResponseState> {
    constructor(props: OpenApiAddResponseProps) {
        super(props);

        this.state = {
            responseObject: {
                description: "",
                operation: this.props.operation,
                resourcePath: this.props.resourcePath,
                status: "",
            },
            responseStatus: [],
        };
    }

    public componentDidMount() {
        this.generateResponseStatusCodes();
    }

    public generateResponseStatusCodes() {
        const responseCodes = ["100 - Continue", "101 - Switching Protocols",
        "102 - Processing", "200 - OK", "201 - Created", "202 - Accepted",
        "203 - Non-Authoritative Information", "204 - No Content", "205 - Reset Content",
        "206 - Partial Content", "207 - Multi-Status",
        "208 - Already Reported", "226 - IM Used", "300 - Multiple Choices",
        "301 - Moved Permanently", "302 - Found", "303 - See Other",
        "304 - Not Modified", "305 - Use Proxy", "307 - Temporary Redirect",
        "308 - Permanent Redirect", "400 - Bad Request", "401 - Unauthorized",
        "402 - Payment Required", "403 - Forbidden", "404 - Not Found", "405 - Method Not Allowed",
        "406 - Not Acceptable", "407 - Proxy Authentication Required",
        "408 - Request Timeout", "409 - Conflict", "410 - Gone", "411 - Length Required",
        "412 - Precondition Failed", "413 - Payload Too Large", "414 - URI Too Long",
        "415 - Unsupported Media Type", "416 - Range Not Satisfiable", "417 - Expectation Failed",
        "421 - Misdirected Request", "422 - Unprocessable Entity",
        "423 - Locked", "424 - Failed Dependency", "426 - Upgrade Required", "428 - Precondition Required",
        "429 - Too Many Requests", "431 - Request Header Fields Too Large",
        "500 - Internal Server Error", "501 - Not Implemented", "502 - Bad Gateway", "503 - Service Unavailable",
        "504 - Gateway Timeout", "505 - HTTP Version Not Supported",
        "506 - Variant Also Negotiates", "507 - Insufficient Storage", "508 - Loop Detected",
        "510 - Not Extended", "511 - Network Authentication Required"];
        const responseCodeList: OpenApiResponseCode[] = [];

        responseCodes.forEach((response) => {
            responseCodeList.push({
                key: response.trim().split(" - ")[0],
                text: response,
                value: response.trim().split(" - ")[0]
            });
        });

        this.setState({
            responseStatus: responseCodeList,
        });

    }

    public render() {
        const { onAddResponse, handleClose } = this.props;

        return (
            <Form size="mini" className="add-operation">
                <Form.Field>
                    <Form.Field
                        control={Select}
                        label="Response Status"
                        options={this.state.responseStatus}
                        placeholder="Method"
                        onChange={(e: any, data: any) => {
                        this.setState({
                            responseObject: {
                                ...this.state.responseObject,
                                status: data.value
                            }
                        });
                    }}/>
                </Form.Field>
                <Form.Field>
                    <Form.TextArea
                        label="Description"
                        placeholder="Tell us more about..."
                        onChange={(e) => this.setState({
                            responseObject: {
                                ...this.state.responseObject,
                                description: e.currentTarget.value
                            }
                        })}
                    />
                </Form.Field>
                <Button size="mini" onClick={() => {
                    onAddResponse(this.state.responseObject);
                    handleClose(true);
                }}>Save</Button>
            </Form>
        );
    }
}

export default OpenApiAddResponse;
