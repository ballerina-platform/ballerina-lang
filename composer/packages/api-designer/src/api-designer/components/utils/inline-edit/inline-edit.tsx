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

import ComponentFocusDetector from "../component-focus-detection";

import InlineEditContact from "./inline-edit-contact";
import InlineEditLicense from "./inline-edit-license";
import InlineEditParagraph from "./inline-edit-paragraph";
import InlineEditString from "./inline-edit-string";
import InlineEditURL from "./inline-edit-url";

export interface OpenApiInlineEditProps {
    editableObject: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    markdown?: boolean;
    paragraph?: boolean;
    placeholderText?: string;
    onValueChange: (openApiJson: Swagger.OpenAPIObject) => void;
    changeModel: Swagger.OpenAPIObject;
    changeAttribute: AttributeObject;
    isMarkdown?: boolean;
    isTermsOfService?: boolean;
}

export interface OpenApiInlineEditState {
    editText: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    initialValue: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    isEditing: boolean;
    valueChanged: boolean;
}

export interface URL {
    urlPath: string;
    urlText: string;
}

export interface AttributeObject {
    key: string;
    path?: string;
    changeValue: string;
}

class OpenApiInlineEdit extends React.Component<OpenApiInlineEditProps, OpenApiInlineEditState> {

    /**
     * Inline edit constructor
     * @param props
     */
    constructor(props: OpenApiInlineEditProps) {
        super(props);

        this.state = {
            editText: typeof this.props.editableObject !== "string" ?
                { ...this.props.editableObject } : this.props.editableObject,
            initialValue: typeof this.props.editableObject !== "string" ?
                { ...this.props.editableObject } : this.props.editableObject,
            isEditing: false,
            valueChanged: false
        };

        this.onEnableEditing = this.onEnableEditing.bind(this);
        this.onExitEditing = this.onExitEditing.bind(this);
        this.onKeyDownEvent = this.onKeyDownEvent.bind(this);
        this.onTextValueChange = this.onTextValueChange.bind(this);
        this.onCancelEditing = this.onCancelEditing.bind(this);
        this.onFocusOut = this.onFocusOut.bind(this);
    }

    public componentWillReceiveProps(nextProps: OpenApiInlineEditProps) {
        this.setState({
            editText: typeof this.props.editableObject !== "string" ?
                { ...this.props.editableObject } : this.props.editableObject,
            initialValue: typeof this.props.editableObject !== "string" ?
                { ...this.props.editableObject } : this.props.editableObject
        });
    }

    /**
     * Inline edit render function
     */
    public render() {

        const { isEditing, editText } = this.state;
        const { paragraph, placeholderText, isMarkdown, isTermsOfService } = this.props;

        if (typeof editText === "string" && paragraph && isMarkdown) {
            return (
                <InlineEditParagraph isMarkdown editableString={editText} isEditing={isEditing}
                    onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                    onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                />
            );
        }

        if (typeof editText === "string" && isTermsOfService) {
            return (
                <InlineEditString isTermsOfService editableString={editText} isEditing={isEditing}
                    onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                    onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                    onKeyDownEvent={this.onKeyDownEvent}
                />
            );
        }

        if (typeof editText === "string") {
            return (
                <InlineEditString editableString={editText} isEditing={isEditing}
                    onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                    onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                    onKeyDownEvent={this.onKeyDownEvent}
                />
            );
        }

        if (this.isContactObj(editText)) {
            return (
                <ComponentFocusDetector onClickedOut={this.onFocusOut}>
                    <InlineEditContact editableString={editText} isEditing={isEditing}
                        onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                        onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                        onCancelEditing={this.onCancelEditing} />
                </ComponentFocusDetector>
            );
        }

        if (this.isLicenseObj(editText)) {
            return (
                <ComponentFocusDetector onClickedOut={this.onFocusOut}>
                    <InlineEditLicense editableString={editText} isEditing={isEditing}
                        onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                        onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                        onCancelEditing={this.onCancelEditing} />
                </ComponentFocusDetector>
            );
        }

        if (this.isURLObj(editText)) {
            return (
                <ComponentFocusDetector onClickedOut={this.onFocusOut}>
                    <InlineEditURL editableString={editText} isEditing={isEditing}
                        onEnableEditing={this.onEnableEditing} placeholderText={placeholderText}
                        onTextValueChange={this.onTextValueChange} onExitEditing={this.onExitEditing}
                        onCancelEditing={this.onCancelEditing} />
                </ComponentFocusDetector>
            );
        }

        return (
            <div></div>
        );
    }

    private onEnableEditing(e: React.MouseEvent<HTMLElement>) {
        e.stopPropagation();
        this.setState({
            isEditing: true,
        });
    }

    private onFocusOut() {
        this.setState({
            isEditing: false
        });
    }

    private onCancelEditing() {
        const initialValue = typeof this.state.initialValue !== "string" ?
            { ...this.state.initialValue } : this.state.initialValue;
        this.setState({
            editText: initialValue,
            isEditing: false
        });
    }

    private onExitEditing(isCancel?: boolean) {
        this.setState({
            isEditing: false
        }, () => {
            const { changeModel, changeAttribute } = this.props;
            const { editText, initialValue } = this.state;

            if (editText !== initialValue) {
                this.persistUpdatedValue(changeModel, changeAttribute);
                this.setState({
                    valueChanged: false
                });
            }
        });
    }

    private onTextValueChange(e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) {
        let { editText } = this.state;
        const { changeAttribute } = this.props;
        let changeDone = false;

        if (this.isContactObj(editText)) {
            switch (e.target.id) {
                case "name":
                    editText.name = e.target.value;
                    break;
                case "url":
                    editText.url = e.target.value;
                    break;
                case "email":
                    editText.email = e.target.value;
                    break;
                default:
                    break;
            }
            changeDone = true;
        } else if (this.isLicenseObj(editText)) {
            switch (e.target.id) {
                case "name":
                    editText.name = e.target.value;
                    break;
                case "url":
                    editText.url = e.target.value;
                    break;
                default:
                    break;
            }
            changeDone = true;
        } else if (this.isURLObj(editText)) {
            switch (e.target.id) {
                case "name":
                    editText.urlText = e.target.value;
                    break;
                case "url":
                    editText.urlPath = e.target.value;
                    break;
                default:
                    break;
            }
            changeDone = true;
        } else {
            if (changeAttribute.key === "resource.name") {
                editText = e.target.value.trim();
                changeDone = true;
            } else {
                editText = e.target.value;
                changeDone = true;
            }
        }

        this.setState({
            editText,
            valueChanged: changeDone
        });
    }

    private onKeyDownEvent(e: React.KeyboardEvent) {
        if (e.keyCode === 13 && e.currentTarget.id !== "paragraph") {
            this.onExitEditing();
        }
    }

    private persistUpdatedValue(model: Swagger.OpenAPIObject, attribute: AttributeObject) {
        const { editText } = this.state;

        switch (attribute.key) {
            case "info.version":
                const version = editText as string;
                model.info.version = version;
                break;
            case "info.description":
                const description = editText as string;
                model.info.description = description;
                break;
            case "info.termsOfService":
                const termsOfService = editText as string;
                model.info.termsOfService = termsOfService;
                break;
            case "info.license":
                const licenseObj = editText as Swagger.LicenseObject;
                model.info.license = {
                    name: licenseObj.name,
                    url: licenseObj.url
                };
                break;
            case "info.contact":
                const contactObj = editText as Swagger.ContactObject;
                model.info.contact = {
                    email: contactObj.email,
                    name: contactObj.name,
                    url: contactObj.url
                };
                break;
            case "resource.name":
                const pathName = editText as string;
                if (pathName !== "" && model.paths) {
                    model.paths[pathName] = model.paths[attribute.changeValue];
                    delete model.paths[attribute.changeValue];
                }
                break;
            case "operation.description":
                const opDescription = editText as string;
                if (opDescription !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].description = opDescription;
                }
                break;
            case "operation.summary":
                const opSummary = editText as string;
                if (opSummary !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].summary = opSummary;
                }
                break;
            default:
                break;
        }

        // this.props.onValueChange(model);
    }

    private isContactObj(arg: any): arg is Swagger.ContactObject {
        return arg && arg.hasOwnProperty("name") && arg.hasOwnProperty("url") && arg.hasOwnProperty("email");
    }

    private isLicenseObj(arg: any): arg is Swagger.LicenseObject {
        return arg && arg.hasOwnProperty("url") && arg.hasOwnProperty("name");
    }

    private isURLObj(arg: any): arg is URL {
        return arg && arg.hasOwnProperty("urlPath") && arg.hasOwnProperty("urlText");
    }

}

export default OpenApiInlineEdit;
