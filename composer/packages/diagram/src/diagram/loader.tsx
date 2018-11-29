import React from "react";
import { Dimmer, Loader as SemanticUILoader } from "semantic-ui-react";

export const Loader: React.StatelessComponent<{}> = ({ children }) => {
    return <Dimmer active inverted>
        <SemanticUILoader size="large" />
        {children}
    </Dimmer>;
};
