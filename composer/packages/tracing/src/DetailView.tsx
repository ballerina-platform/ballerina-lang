/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the ''License''); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * ''AS IS'' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import * as React from "react";
import ReactJson from "react-json-view";
import { Segment } from "semantic-ui-react";

function isJson(text: string) {
    if (typeof text !== "string") {
        return false;
    }
    try {
        if (typeof JSON.parse(text) === "string") {
            return false;
        }
        return true;
    } catch (error) {
        return false;
    }
}

export interface DetailViewProps {
    trace: any;
}

export default class DetailView extends React.Component<DetailViewProps> {
    constructor(props: DetailViewProps) {
        super(props);
    }
    public renderPayload(trace: any) {
        const payload = trace.message.payload;
        const contentType = trace.message.contentType;
        if (contentType.indexOf("application/json") > 0 && isJson(payload)) {
            return (
                <ReactJson
                    src={JSON.parse(payload)}
                    theme="eighties"
                    name={false}
                    displayDataTypes={false}
                    collapsed={1}
                    displayObjectSize={false}
                    style={{ marginTop: 10, background: "inherit" }}
                />
            );
        } else if (contentType.indexOf("application/octet-stream") > 0) {
            return (
                <code><pre>Unable to parse Payload : application/octet-stream </pre></code>
            );
        } else {
            return (
                <code><pre>{trace.message.payload}</pre></code>
            );
        }
    }
    public render() {
        const trace = this.props.trace;
        const headers = trace.message.headers || "";
        const headersArray = headers.split("\n");
        const payload = trace.message.payload;
        if (payload.trim() ===  "" && headers.trim() === "") {
            return (
                <code>
                    <pre>{trace.rawMessage}</pre>
                </code>
            );
        }
        return (
            <Segment inverted padded compact>
                {
                    headersArray.length && <code>
                    <pre>
                        {headersArray.map((header: string, index: number) => {
                            const endChar = headersArray.length - 1 === index ? "" : "\n";
                            const splitIndex = header.indexOf(":");
                            if (splitIndex !== -1) {
                                return ([
                                    <strong>
                                        {header.substring(0, splitIndex)}
                                    </strong>,
                                    `${header.substring(splitIndex)}${endChar}`,
                                ]
                                );
                            } else {
                                return (`${header}${endChar}`);
                            }
                        })}
                    </pre>
                </code>
                }
                <div className="payload">
                    {
                        this.renderPayload(trace)
                    }
                </div>
            </Segment>
        );
    }
}
