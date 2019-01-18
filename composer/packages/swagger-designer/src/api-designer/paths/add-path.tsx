import * as React from "react";
import { Button, Form } from "semantic-ui-react";

export interface AddOpenApiPathProps {
    path: string;
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

        return (
            <Form size="mini" className="add-resource">
                <Form.Field>
                    <label>Resource Name</label>
                    <input placeholder="Example: /users/{userId}" />
                </Form.Field>
                <Form.Group inline>
                    <label>Methods</label>
                    {operationMethods.map((method) => {
                        return (
                            <Form.Checkbox
                                size="mini"
                                label={method.text}
                                value={method.value}
                            />
                        );
                    })}
                </Form.Group>
                <Button size="tiny">Save Resource</Button>
            </Form>
        );
    }
}

export default AddOpenApiPath;
