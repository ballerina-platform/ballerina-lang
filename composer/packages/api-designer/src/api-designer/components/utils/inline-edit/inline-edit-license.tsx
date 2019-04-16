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
import { Form } from "semantic-ui-react";

export interface InlineEditLicenceProps {
    editableString: Swagger.LicenseObject;
    isEditing: boolean;
    onEnableEditing: (e: React.MouseEvent<HTMLElement>) => void;
    onTextValueChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => void;
    onExitEditing: () => void;
    onCancelEditing: () => void;
    placeholderText?: string;
}

class InlineEditLicense extends React.Component<InlineEditLicenceProps, any> {

    constructor(props: InlineEditLicenceProps) {
        super(props);
    }

    public render() {

        const { onEnableEditing, placeholderText, isEditing,
                editableString, onExitEditing, onTextValueChange, onCancelEditing} = this.props;

        if (isEditing) {
            return (
                <div className={"inline-editor editing "} >
                    <Form>
                        <Form.Group>
                            <Form.Input
                                id="name"
                                size="mini"
                                placeholder="Name"
                                value={editableString.name}
                                onChange={onTextValueChange}
                            />
                            <Form.Input
                                id="url"
                                size="mini"
                                placeholder="URL"
                                value={editableString.url}
                                onChange={onTextValueChange}
                            />
                            <Form.Button size="mini"
                                onClick={onExitEditing} >
                                <i className="fw fw-check"></i>
                                </Form.Button>
                            <Form.Button size="mini"
                                onClick={onCancelEditing} >
                                <i className="fw fw-close"></i>
                                </Form.Button>
                        </Form.Group>
                    </Form>
                </div>
            );
        }

        if (editableString.name === "" && editableString.url === "") {
            return (
                <div className="inline-editor license">
                    <span className="inline-editor-span" onClick={onEnableEditing}>
                        {placeholderText}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        }

        return (
            <div className="inline-editor license">
                <span className="inline-editor-span" onClick={onEnableEditing}>
                    {editableString.name}
                    <i className="fw fw-edit edit-icon"></i>
                </span>
                <a className="activate-edit" href={editableString.url} target="_blank">
                    <i className="fw fw-link"></i>
                </a>
            </div>
        );
    }
}

export default InlineEditLicense;
