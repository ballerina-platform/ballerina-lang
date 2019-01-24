import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Checkbox, Form, Header, Icon, Select } from "semantic-ui-react";

export interface OpenApiAddParameterProps {
    openApiJson: Swagger.OpenAPIObject;
    onAddParameter: (parameter: Swagger.ParameterObject) => void;
    operation: string;
    resourcePath: string;
}

export interface OpenApiAddParameterState {
    parameterIn: OpenApiParameterIn[];
    parameterType: OpenApiParameterType[];
    parameterObj: Swagger.ParameterObject;
}

export interface OpenApiParameterIn {
    key: string;
    text: string;
    value: string;
}

export interface OpenApiParameterType {
    key: string;
    text: string;
    value: string;
}

class OpenApiAddParameter extends React.Component<OpenApiAddParameterProps, OpenApiAddParameterState> {
    constructor(props: OpenApiAddParameterProps) {
        super(props);

        this.state = {
            parameterIn: [],
            parameterObj: {
                allowedEmptyValues: false,
                description: "",
                in: "path",
                isRequired: false,
                name: "",
                operation: this.props.operation,
                parameterIn: "",
                resourcePath: this.props.resourcePath,
                type: "",
            },
            parameterType: []
        };
    }

    public componentDidMount() {
        this.populateParameterInField();
        this.populateParamTypeField();
    }

    public render() {
        const { parameterIn, parameterType } = this.state;
        const { onAddParameter } = this.props;

        return (
            <Form size="mini" className="add-operation">
                <div className="form-box">
                    <Header floated="left" as="h3">Add Parameter</Header>
                    <Icon circular className="fw fw-delete" />
                </div>
                <Form.Field>
                    <label>Name</label>
                    <input placeholder="Parameter Name" onChange={(e) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            name: e.target.value
                        }
                    })} />
                </Form.Field>
                <Form.Field>
                    <Form.Field
                        control={Select}
                        label="Parameter In"
                        options={parameterIn}
                        placeholder="Where parameter appears"
                        onChange={(e: any, data: any) => {
                        this.setState({
                            parameterObj: {
                                ...this.state.parameterObj,
                                parameterIn: data.value
                            }
                        });
                    }}/>
                </Form.Field>
                <Form.Field>
                    <Form.Field
                        control={Select}
                        label="Parameter Type"
                        options={parameterType}
                        onChange={(e: any, data: any) => {
                        this.setState({
                            parameterObj: {
                                ...this.state.parameterObj,
                                type: data.value
                            }
                        });
                    }}/>
                </Form.Field>
                <Form.Field>
                    <Form.TextArea
                        label="Description"
                        placeholder="Tell us more about..."
                        onChange={(e) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            description: e.currentTarget.value
                        }
                    })}/>
                </Form.Field>
                <Form.Field>
                    <Checkbox label="Property is required" onChange={(e, { checked }) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            isRequired: checked ? true : false
                        }
                    })} />
                </Form.Field>
                <Form.Field>
                    <Checkbox label="Allowed empty values" onChange={(e, { checked }) => this.setState({
                        parameterObj: {
                            ...this.state.parameterObj,
                            allowedEmptyValues: checked ? true : false
                        }
                    })} />
                </Form.Field>
                <Button size="mini" onClick={() => {
                    onAddParameter(this.state.parameterObj);
                }}>Save</Button>
            </Form>
        );
    }

    private populateParameterInField() {
        const paramInDefaults = ["Path", "Query"];
        const paramInList: OpenApiParameterIn[] = [];

        paramInDefaults.forEach((response) => {
            paramInList.push({
                key: response.toLowerCase(),
                text: response,
                value: response.toLowerCase(),
            });
        });

        this.setState({
            parameterIn: paramInList
        });
    }

    private populateParamTypeField() {
        const typeInDefaults = ["String", "Number", "Boolean"];
        const typeList: OpenApiParameterType[] = [];

        typeInDefaults.forEach((response) => {
            typeList.push({
                key: response.toLowerCase(),
                text: response,
                value: response.toLowerCase(),
            });
        });

        this.setState({
            parameterType: typeList
        });
    }
}

export default OpenApiAddParameter;
