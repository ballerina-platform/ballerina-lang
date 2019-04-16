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
import * as React from "react";
import { Form } from "semantic-ui-react";

import { URL } from "./inline-edit";

export interface InlineEditURLProps {
    editableString: URL;
    isEditing: boolean;
    onEnableEditing: (e: React.MouseEvent<HTMLElement>) => void;
    onTextValueChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => void;
    onExitEditing: () => void;
    onCancelEditing: () => void;
    placeholderText?: string;
}

class InlineEditURL extends React.Component<InlineEditURLProps, any> {
    constructor(props: InlineEditURLProps) {
        super(props);
    }

    public render() {

        const { onEnableEditing, placeholderText, isEditing,
                editableString, onTextValueChange, onExitEditing, onCancelEditing } = this.props;

        if (isEditing) {
            return (
                <div className={"inline-editor editing url"}>
                    <Form>
                        <Form.Group>
                            <Form.Input
                                id="name"
                                size="mini"
                                placeholder="Name"
                                value={editableString.urlText}
                                onChange={onTextValueChange}
                            />
                            <Form.Input
                                id="url"
                                size="mini"
                                placeholder="URL"
                                value={editableString.urlPath}
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

        if (editableString.urlPath === "" && editableString.urlText === "") {
            return (
                <div className="inline-editor url" onClick={onEnableEditing}>
                    <span className="inline-editor-span" onClick={onEnableEditing}>
                        {placeholderText}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        }

        return (
            <div className="inline-editor url" onClick={onEnableEditing}>
                <span className="inline-editor-span" onClick={onEnableEditing}>
                    {editableString.urlText}
                    <i className="fw fw-edit edit-icon"></i>
                </span>
                <a className="activate-edit inline-editor-span" href={editableString.urlPath} target="_blank">
                    <i className="fw fw-link"></i>
                </a>
            </div>
        );
    }
}

export default InlineEditURL;
