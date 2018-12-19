import * as Swagger from "openapi3-ts";
import * as React from "react";

export interface InlineEditProps {
    text: string | Swagger.LicenseObject | Swagger.ContactObject | undefined ;
    isMarkdown?: boolean;
    placeholderText: string;
}

export interface InlineEditState {
    text: string | Swagger.LicenseObject | Swagger.ContactObject | undefined;
    isEditing: boolean;
}

class InlineEdit extends React.Component<InlineEditProps, InlineEditState> {
    constructor(props: InlineEditProps) {
        super(props);
    }

    public render() {
        const { isEditing } = this.state;

        if (!isEditing) {
            return (
                <div className="editable-span">

                </div>
            )
        } 
        return (
            <div></div>
        );
    }
}

export default InlineEdit;
