/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Header } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextConsumer } from "../context/open-api-context";

import InlineEdit from "../utils/inline-edit/inline-edit";
import OpenApiContact from "./contact";
import OpenApiLicense from "./license";

export interface OpenApiInfoProps {
    info: Swagger.InfoObject;
}

class OpenApiInfo extends React.Component<OpenApiInfoProps, any> {
    constructor(props: OpenApiInfoProps) {
        super(props);
    }

    public render() {
        const { info } = this.props;

        return(
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    return (
                        <React.Fragment>
                            <Header as="h1">
                                {info.title}
                                <Header.Subheader>
                                    <InlineEdit
                                        changeAttribute={{key: "info.version", changeValue: ""}}
                                        editableObject={info.version}
                                        changeModel={context!.openApiJson}
                                        paragraph
                                        placeholderText=" + Version"
                                        onValueChange={context!.onInlineValueChange}
                                    />
                                </Header.Subheader>
                            </Header>
                            <InlineEdit
                                changeAttribute={{key: "info.description", changeValue: ""}}
                                editableObject={info.description ? info.description : ""}
                                isMarkdown
                                paragraph
                                changeModel={context!.openApiJson}
                                placeholderText="+ Description"
                                onValueChange={context!.onInlineValueChange}
                            />
                            <InlineEdit
                                editableObject={info.termsOfService ? info.termsOfService : ""}
                                isTermsOfService
                                changeAttribute={{key: "info.termsOfService", changeValue: ""}}
                                changeModel={context!.openApiJson}
                                placeholderText="+ Terms of service link"
                                onValueChange={context!.onInlineValueChange}
                            />
                            <OpenApiContact contact={info.contact} />
                            <OpenApiLicense license={info.license} />
                        </React.Fragment>
                    );
                }}
            </OpenApiContextConsumer>
        );
    }
}

export default OpenApiInfo;
