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

import { OpenApiContext, OpenApiContextConsumer } from "../context/open-api-context";

import InlineEdit from "../utils/inline-edit";

export interface OpenApiContactProps {
    contact?: Swagger.ContactObject;
}

class OpenApiContact extends React.Component<OpenApiContactProps, any> {
    constructor(props: OpenApiContactProps) {
        super(props);
    }

    public render() {
        const { contact } = this.props;

        return (
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    if (contact) {
                        return (
                            <InlineEdit
                                text={contact}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.contact", changeValue: ""}}
                                placeholderText="+ Contact information"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    } else {
                        return (
                            <InlineEdit
                                text={{
                                    email: "",
                                    name: "",
                                    url: "",
                                }}
                                changeModel={context!.openApiJson}
                                changeAttribute={{key: "info.contact", changeValue: ""}}
                                placeholderText="+ Contact information"
                                onInlineValueChange={context!.onInlineValueChange}
                            />
                        );
                    }
                }}
            </OpenApiContextConsumer>
        );
    }
}

export default OpenApiContact;
