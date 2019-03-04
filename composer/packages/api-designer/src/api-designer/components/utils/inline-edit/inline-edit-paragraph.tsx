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
import ReactMarkdown from "react-markdown";

export interface InlineEditParagraphProps {
    editableString: string;
    isEditing: boolean;
    onEnableEditing: (e: React.MouseEvent<HTMLElement>) => void;
    onTextValueChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => void;
    onExitEditing: () => void;
    placeholderText?: string;
    isMarkdown?: boolean;
}

class InlineEditLicense extends React.Component<InlineEditParagraphProps, any> {
    constructor(props: InlineEditParagraphProps) {
        super(props);
    }

    public render() {

        const { onEnableEditing, placeholderText, isEditing,
                editableString, onTextValueChange, onExitEditing } = this.props;

        if (isEditing) {
            return (
                <div className="inline-editor editing">
                    <textarea className="inline-editor-textarea"
                        autoFocus
                        placeholder={placeholderText}
                        onBlur={onExitEditing}
                        onChange={onTextValueChange}
                        id="paragraph"
                    >
                        {editableString}
                    </textarea>
                </div>
            );
        }

        if (editableString === "") {
            return (
                <div className="inline-editor paragraph markdown" >
                    <span className="inline-editor-span" onClick={onEnableEditing}>
                        {placeholderText}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        }
        return (
            <div className="inline-editor paragraph markdown" onClick={onEnableEditing}>
                <ReactMarkdown escapeHtml={false} source={editableString} />
            </div>
        );
    }
}

export default InlineEditLicense;
