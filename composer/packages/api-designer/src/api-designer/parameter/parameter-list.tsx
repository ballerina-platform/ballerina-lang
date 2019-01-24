import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Table } from "semantic-ui-react";

export interface OpenApiParameterListProps {
    parameterList: Swagger.ParameterObject[];
}

class OpenApiParameterList extends React.Component<OpenApiParameterListProps, any> {
    constructor(props: OpenApiParameterListProps) {
        super(props);
    }

    public render() {
        const { parameterList } = this.props;

        return (
            <Table celled>
                <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>Name</Table.HeaderCell>
                    <Table.HeaderCell>Description</Table.HeaderCell>
                </Table.Row>
                </Table.Header>
                <Table.Body>
                    {parameterList.map((param: Swagger.ParameterObject, index: number) => {
                        return (
                            <Table.Row>
                                <Table.Cell className="parameter-name-cell">
                                    <div className="parameter__name required">
                                        {param.name}
                                    </div>
                                    <div className="parameter__type">
                                        {param.in &&
                                            <p><em>({param.in})</em></p>
                                        }
                                    </div>
                                </Table.Cell>
                                <Table.Cell className="parameter-desc-cell">
                                    <div className="markdown">
                                        {param.description}
                                    </div>
                                </Table.Cell>
                            </Table.Row>
                        );
                    })}
                </Table.Body>
            </Table>
        );
    }
}

export default OpenApiParameterList;
