import React from 'react';

const SwaggerAppContext = React.createContext({
    oasJson: {},
    onAddResource: () => {},
    onAddParameter: () => {},
    onAddOperation: () => {},
    onDeleteOperation: () => {},
});

export default SwaggerAppContext;
