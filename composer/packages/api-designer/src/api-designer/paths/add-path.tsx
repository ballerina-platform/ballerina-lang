import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Form, Input } from "semantic-ui-react";

export interface AddOpenApiPathProps {
    onAddOpenApiPath: (resource: Swagger.PathItemObject) => void;
}

export interface AddOpenApiPathState {
    openApiResourceObj: OpenApiResource;
    operationMethods: OpenApiOperationMethod[];
}

export interface OpenApiResource {
    name: string;
    methods: string[];
}

export interface OpenApiOperationMethod {
    text: string;
    value: string;
}

class AddOpenApiPath extends React.Component<AddOpenApiPathProps, AddOpenApiPathState> {

    constructor(props: AddOpenApiPathProps) {
        super(props);

        this.state = {
            openApiResourceObj: {
                methods: [],
                name: ""
            },
            operationMethods: []
        };

        this.clearFields = this.clearFields.bind(this);

    }

    public componentDidMount() {
        this.populateOperationMethods();
    }

    public populateOperationMethods() {
        const methodOpts: OpenApiOperationMethod[] = [];

        const availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"];

        availableMethods.forEach((method) => {
            methodOpts.push({
                text: method,
                value: method.toLowerCase(),
            });
        });

        this.setState({
            operationMethods: methodOpts,
        });
    }

    public render() {
        const { operationMethods } = this.state;
        const { onAddOpenApiPath } = this.props;

        return (
            <Form size="mini" className="add-resource">
                <Form.Field>
                    <label>Resource Name</label>
                    <Input label="/"
                        placeholder="Example: users/{userId}"
                    value={this.state.openApiResourceObj.name}
                        onChange={(e) => {
                            this.setState({
                                openApiResourceObj: {
                                    ...this.state.openApiResourceObj,
                                    name: e.target.value.replace(/^\//, "").trim()
                                }
                            });
                        }}
                    />
                </Form.Field>
                <Form.Group inline>
                    <label>Methods</label>
                    {operationMethods.map((method) => {
                        return (
                            <Form.Checkbox
                                size="mini"
                                label={method.text}
                                value={method.value}
                                onChange={(e: React.SyntheticEvent, data: any) => {
                                    if (data.checked) {
                                        this.setState({
                                            openApiResourceObj: {
                                                ...this.state.openApiResourceObj,
                                                methods: [...this.state.openApiResourceObj.methods, data.label],
                                            }
                                        });
                                    }
                                }}
                            />
                        );
                    })}
                </Form.Group>
                <Button size="mini" onClick={() => {
                    onAddOpenApiPath(this.state.openApiResourceObj);
                    this.clearFields();
                }}>Add Resource</Button>
            </Form>
        );
    }

    private clearFields() {
        this.setState({
            openApiResourceObj: {
                methods: [],
                name: ""
            },
            operationMethods: []
        }, () => {
            this.populateOperationMethods();
        });
    }
}

export default AddOpenApiPath;
