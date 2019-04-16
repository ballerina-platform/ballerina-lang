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
import { Form, Input } from "semantic-ui-react";

export interface InlineEditStringProps {
    editableString: string;
    isEditing: boolean;
    onEnableEditing: (e: React.MouseEvent<HTMLElement>) => void;
    onTextValueChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => void;
    onExitEditing: () => void;
    onKeyDownEvent: (e: React.KeyboardEvent) => void;
    placeholderText?: string;
    isTermsOfService?: boolean;
}

class InlineEditString extends React.Component<InlineEditStringProps, any> {

    constructor(props: InlineEditStringProps) {
        super(props);
    }

    public render() {

        const { onEnableEditing, placeholderText, isEditing,
                editableString, onTextValueChange, onExitEditing, onKeyDownEvent, isTermsOfService } = this.props;

        if (isEditing) {
            return (
                <div className="inline-editor string editing">
                    <Form>
                        <Input
                            transparent
                            autoFocus
                            placeholder={placeholderText}
                            value={editableString}
                            onBlur={onExitEditing}
                            onChange={onTextValueChange}
                            onClick={this.handleOnClick}
                            onKeyDown={onKeyDownEvent}
                        />
                    </Form>
                </div>
            );
        }

        if (editableString === "") {
            return (
                <div className="inline-editor string">
                    <span className="inline-editor-span" onClick={onEnableEditing}>
                        {placeholderText}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        }

        if (isTermsOfService) {
            return (
                <div className="inline-editor url">
                    <span className="inline-editor-span" onClick={onEnableEditing}>
                        + Terms of Service
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                    <a className="activate-edit inline-editor-span" href={editableString} target="_blank">
                        <i className="fw fw-link"></i>
                    </a>
                </div>
            );
        }

        return (
            <div className="inline-editor string" onClick={onEnableEditing}>
                <span className="inline-editor-span">{editableString}
                    <i className="fw fw-edit edit-icon"></i>
                </span>
            </div>
        );
    }

    private handleOnClick(e: any) {
        e.stopPropagation();
    }

}

export default InlineEditString;
