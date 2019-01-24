import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Form } from "semantic-ui-react";

export interface OpenApiAddOperationProps {
    openApiJson: Swagger.OpenAPIObject;
    onAddOperation: (operation: Swagger.OperationObject) => void ;
    resourcePath: string;
}

export interface OpenApiAddOperationState {
    operationObject: OpenApiOperation;
    operationMethods: OpenApiOperationMethod[];
}

export interface OpenApiOperationMethod {
    text: string;
    value: string;
    checked: boolean;
}

export interface OpenApiOperation {
    id: string;
    name: string;
    description: string;
    method: string[];
    path: string;
    responses: string[];
}

class AddOpenApiOperation extends React.Component<OpenApiAddOperationProps, OpenApiAddOperationState> {

    constructor(props: any) {
        super(props);

        this.state = {
            operationMethods: [],
            operationObject: {
                description: "",
                id: "",
                method: [],
                name: "",
                path: this.props.resourcePath,
                responses: []
            }
        };

        this.handleCheckboxCheck = this.handleCheckboxCheck.bind(this);
    }

    public componentWillReceiveProps(nextProps: OpenApiAddOperationProps) {
        const { resourcePath, openApiJson } = nextProps;
        this.populateOperationMethods(resourcePath, openApiJson);
    }

    public componentDidMount() {
        const { resourcePath, openApiJson } = this.props;
        this.populateOperationMethods(resourcePath, openApiJson);
    }

    public render() {
        const { operationMethods } = this.state;
        const { onAddOperation } = this.props;

        return (
            <Form className="add-operation">
                <Form.Group inline>
                    <label>Methods</label>
                    {operationMethods.sort().map((method) => {
                        return (
                            <Form.Checkbox
                                size="mini"
                                label={method.text}
                                defaultChecked={method.checked}
                                disabled={method.checked}
                                onChange={this.handleCheckboxCheck}
                            />
                        );
                    })}
                </Form.Group>
                <Button size="mini" onClick={() => {
                    onAddOperation(this.state.operationObject);
                    this.setState({
                        operationMethods: []
                    });
                }}>Add</Button>
            </Form>
        );
    }

    private handleCheckboxCheck(e: React.SyntheticEvent, data: any) {
        if (data.checked) {
            this.setState({
                operationObject: {
                    ...this.state.operationObject,
                    method:  [...this.state.operationObject.method, data.label],
                }
            });
        } else {
            const methods = this.state.operationObject.method.filter((item) =>
                    item !== data.label);
            this.setState({
                operationObject: {
                    ...this.state.operationObject,
                    method: methods,
                }
            });
        }
    }

    private populateOperationMethods(resourcePath: string, openApiJson: Swagger.OpenAPIObject) {
        const methodOpts: OpenApiOperationMethod[] = [];

        const availableMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"];

        availableMethods.forEach((method) => {
            methodOpts.push({
                checked: Object.keys(openApiJson.paths[resourcePath]).includes(method.toLowerCase()) ? true : false,
                text: method,
                value: method.toLowerCase()
            });
        });

        this.setState({
            operationMethods: methodOpts,
        });
    }
}

export default AddOpenApiOperation;
