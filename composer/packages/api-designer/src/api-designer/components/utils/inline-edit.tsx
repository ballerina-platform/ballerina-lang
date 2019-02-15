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
import ReactMarkdown from "react-markdown";
import { Form, Input } from "semantic-ui-react";

export interface InlineEditProps {
    text?: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    isMarkdown?: boolean;
    placeholderText?: string;
    isParagraph?: boolean;
    changeModel: Swagger.OpenAPIObject;
    changeAttribute: AttributeObject;
    onInlineValueChange: (openApiJson: Swagger.OpenAPIObject) => void;
}

export interface URL {
    link?: string;
    urlText: string;
    type: string;
}

export interface InlineEditState {
    text: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    isEditing: boolean;
    valueChanged: boolean;
}

export interface AttributeObject {
    key: string;
    path?: string;
    changeValue: string;
}

class InlineEdit extends React.Component<InlineEditProps, InlineEditState> {
    constructor(props: InlineEditProps) {
        super(props);

        this.state = {
            isEditing: false,
            text: this.props.text ? this.props.text : "",
            valueChanged: false
        };

        this.enableEditing = this.enableEditing.bind(this);
        this.cancelEditing = this.cancelEditing.bind(this);
        this.onValueChange = this.onValueChange.bind(this);
        this.onDoneEditing = this.onDoneEditing.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
    }

    public componentWillReceiveProps(nextProps: InlineEditProps) {
        const { text } = nextProps;

        if (text && text !== this.state.text) {
            this.setState({
                text
            });
        }
    }

    public render() {
        const { text, isEditing } = this.state;
        const { isMarkdown, isParagraph, placeholderText } = this.props;

        if (this.isContactObj(text)) {
            if (isEditing) {
                return this.getEditorForm("contact");
            }

            if (this.isObjectEmpty(text)) {
                return (
                    <div className="inline-editor contact" >
                        <span className="inline-editor-span" onClick={this.enableEditing}>{placeholderText}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    </div>
                );
            }

            return (
                <div className="inline-editor contact">
                    {text.email &&
                        <span className="email inline-editor-span" onClick={this.enableEditing}>{text.email}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    }
                    {text.url &&
                        <div className="contact-link">
                            <span className="inline-editor-span" onClick={this.enableEditing}>
                                {text.name} - Website
                                <i className="fw fw-edit edit-icon"></i>
                            </span>
                            <a className="activate-edit" href={text.url} target="_blank">
                                <i className="fw fw-link"></i>
                            </a>
                        </div>
                    }
                </div>
            );
        }

        if (this.isLicenseObj(text)) {
            if (isEditing) {
                return this.getEditorForm();
            }

            if (this.isObjectEmpty(text)) {
                return (
                    <div className="inline-editor license" onClick={this.enableEditing}>
                        <span className="inline-editor-span" onClick={this.enableEditing}>
                            {placeholderText}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    </div>
                );
            }

            return (
                <div className="inline-editor license">
                    <span className="inline-editor-span" onClick={this.enableEditing}>
                        {text.name}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                    <a className="activate-edit" href={text.url} target="_blank">
                        <i className="fw fw-link"></i>
                    </a>
                </div>
            );
        }

        if (this.isURLObj(text)) {
            if (isEditing) {
                return this.getEditorForm(text.type === "tos" ? "tos" : undefined);
            }

            if (this.isObjectEmpty(text)) {
                return (
                    <div className="inline-editor url" onClick={this.enableEditing}>
                        <span className="inline-editor-span" onClick={this.enableEditing}>
                            {placeholderText}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    </div>
                );
            }

            return (
                <div className="inline-editor url" onClick={this.enableEditing}>
                    <span className="inline-editor-span" onClick={this.enableEditing}>
                        {text.urlText}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                    <a className="activate-edit inline-editor-span" href={text.link} target="_blank">
                        <i className="fw fw-link"></i>
                    </a>
                </div>
            );
        }

        if (typeof text === "string" && isParagraph && isMarkdown) {

            if (isEditing) {
                return (
                    <div className="inline-editor editing">
                        <textarea className="inline-editor-textarea"
                            autoFocus
                            placeholder={placeholderText}
                            onBlur={this.cancelEditing}
                            onChange={this.onValueChange}
                            onKeyDown={this.onKeyDown}
                            id="paragraph"
                        >
                            {text}
                        </textarea>
                    </div>
                );
            }

            if (text === "") {
                return (
                    <div className="inline-editor paragraph markdown" >
                        <span className="inline-editor-span" onClick={this.enableEditing}>
                            {placeholderText}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    </div>
                );
            }
            return (
                <div className="inline-editor paragraph markdown" onClick={this.enableEditing}>
                    <ReactMarkdown escapeHtml={false} source={text} />
                </div>
            );
        } else if (typeof text === "string" && isParagraph) {
            return (
                <div className="inline-editor paragraph" onClick={this.enableEditing}>
                    <span className="inline-editor-span">paragraph
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        } else if (typeof text === "string") {

            if (isEditing) {
                return (
                    <div className="inline-editor string editing">
                        <Form>
                            <Input
                                transparent
                                autoFocus
                                placeholder={placeholderText}
                                value={text}
                                onBlur={this.cancelEditing}
                                onChange={this.onValueChange}
                                onClick={(e: any) => { e.stopPropagation(); }}
                                onKeyDown={this.onKeyDown}
                            />
                        </Form>
                    </div>
                );
            }

            if (text === "") {
                return (
                    <div className="inline-editor string" onClick={this.enableEditing}>
                        <span className="inline-editor-span" onClick={this.enableEditing}>
                            {placeholderText}
                            <i className="fw fw-edit edit-icon"></i>
                        </span>
                    </div>
                );
            }

            return (
                <div className="inline-editor string" onClick={this.enableEditing}>
                    <span className="inline-editor-span">{text}
                        <i className="fw fw-edit edit-icon"></i>
                    </span>
                </div>
            );
        }

        return (
            <div></div>
        );

    }

    private enableEditing(e: React.MouseEvent<HTMLSpanElement>) {
        e.stopPropagation();
        this.setState({
            isEditing: true,
        });
    }

    private cancelEditing() {
        this.setState({
            isEditing: false
        }, () => {
            const { changeModel, changeAttribute } = this.props;
            const { valueChanged } = this.state;

            if (valueChanged) {
                this.persistValueChange(changeModel, changeAttribute);
                this.setState({
                    valueChanged: false
                });
            }
        });
    }

    private onDoneEditing() {
        this.setState({
            isEditing: false
        }, () => {
            const { changeModel, changeAttribute } = this.props;
            const { valueChanged } = this.state;

            if (valueChanged) {
                this.persistValueChange(changeModel, changeAttribute);
                this.setState({
                    valueChanged: false
                });
            }
        });
    }

    private onKeyDown(e: React.KeyboardEvent) {
        if (e.keyCode === 13 && e.currentTarget.id !== "paragraph") {
            this.onDoneEditing();
        }
    }

    private getEditorForm(type?: string) {
        return (
            <div className={"inline-editor editing " + type}>
                <Form>
                    <Form.Group>
                        {type !== "tos" &&
                            <Form.Input
                                id="name"
                                size="mini"
                                placeholder="Name"
                                onChange={this.onValueChange}
                            />
                        }
                        <Form.Input
                            id="url"
                            size="mini"
                            placeholder="URL"
                            onChange={this.onValueChange}
                        />
                        {type && type === "contact" &&
                            <Form.Input
                                id="email"
                                size="mini"
                                placeholder="Email"
                                onChange={this.onValueChange}
                            />
                        }
                        <Form.Button size="mini"
                            onClick={this.onDoneEditing} >
                            <i className="fw fw-check"></i>
                            </Form.Button>
                        <Form.Button size="mini"
                            onClick={this.cancelEditing} >
                            <i className="fw fw-close"></i>
                            </Form.Button>
                    </Form.Group>
                </Form>
            </div>
        );
    }

    private onValueChange(e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) {
        let { text } = this.state;
        const { changeAttribute } = this.props;
        let changeDone = false;

        if (this.isContactObj(text)) {
            switch (e.target.id) {
                case "name":
                    text.name = e.target.value;
                    break;
                case "url":
                    text.url = e.target.value;
                    break;
                case "email":
                    text.email = e.target.value;
                    break;
                default:
                    break;
            }
            changeDone = true;
        } else if (this.isLicenseObj(text)) {
            switch (e.target.id) {
                case "name":
                    text.name = e.target.value;
                    break;
                case "url":
                    text.url = e.target.value;
                    break;
                default:
                    break;
            }
            changeDone = true;
        } else if (this.isURLObj(text)) {
            if (text.type && text.type === "tos") {
                text.link = e.target.value;
            } else {
                switch (e.target.id) {
                    case "name":
                        text.urlText = e.target.value;
                        break;
                    case "url":
                        text.link = e.target.value;
                        break;
                    default:
                        break;
                }
            }
            changeDone = true;
        } else {
            if (changeAttribute.key === "resource.name") {
                text = e.target.value.trim();
                changeDone = true;
            } else {
                text = e.target.value;
                changeDone = true;
            }
        }

        this.setState({
            text,
            valueChanged: changeDone
        });
    }

    private persistValueChange(model: Swagger.OpenAPIObject, attribute: AttributeObject) {
        const { text } = this.state;

        switch (attribute.key) {
            case "info.version":
                const version = text as string;
                model.info.version = version;
                break;
            case "info.description":
                const description = text as string;
                model.info.description = description;
                break;
            case "info.termsOfService":
                const termsObj = text as URL;
                model.info.termsOfService = termsObj.link;
                break;
            case "info.license":
                const licenseObj = text as Swagger.LicenseObject;
                model.info.license = {
                    name: licenseObj.name,
                    url: licenseObj.url
                };
                break;
            case "info.contact":
                const contactObj = text as Swagger.ContactObject;
                model.info.contact = {
                    email: contactObj.email,
                    name: contactObj.name,
                    url: contactObj.url
                };
                break;
            case "resource.name":
                const pathName = text as string;
                if (pathName !== "" && model.paths) {
                    model.paths[pathName] = model.paths[attribute.changeValue];
                    delete model.paths[attribute.changeValue];
                }
                break;
            case "operation.description":
                const opDescription = text as string;
                if (opDescription !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].description = opDescription;
                }
                break;
            case "operation.summary":
                const opSummary = text as string;
                if (opSummary !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].summary = opSummary;
                }
                break;
            default:
                break;
        }

        this.props.onInlineValueChange(model);
    }

    private isObjectEmpty(arg: Swagger.ContactObject | Swagger.LicenseObject | URL) {
        if (this.isContactObj(arg)) {
            return (!arg.email || arg.email === "") && (!arg.name || arg.name === "") && (!arg.url || arg.url === "");
        } else if (this.isLicenseObj(arg)) {
            return (!arg.name || arg.name === "") && (!arg.url || arg.url === "");
        } else if (this.isURLObj(arg)) {
            return (!arg.link || arg.link === "") || (!arg.urlText || arg.urlText === "");
        } else {
            return true;
        }
    }

    private isContactObj(arg: any): arg is Swagger.ContactObject {
        return arg && arg.hasOwnProperty("name") && arg.hasOwnProperty("url") && arg.hasOwnProperty("email");
    }

    private isLicenseObj(arg: any): arg is Swagger.LicenseObject {
        return arg && arg.hasOwnProperty("url") && arg.hasOwnProperty("name");
    }

    private isURLObj(arg: any): arg is URL {
        return arg && arg.hasOwnProperty("link") && arg.hasOwnProperty("urlText") && arg.hasOwnProperty("type");
    }
}

export default InlineEdit;
