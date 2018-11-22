
export interface DimentionConfig {
    width: number;
    height: number;
    padding: {
        top: number;
        right: number;
        bottom: number;
        left: number;
    };
    margin: {
        top: number;
        right: number;
        bottom: number;
        left: number;
    };
    maxWidth: number;
    minWdith: number;
}

export interface DiagramConfig {
    statement: DimentionConfig;
    canvas: DimentionConfig;
    panel: DimentionConfig;
}

function initDimentionConfig(): DimentionConfig {
    return {
        height: 0,
        margin: {
            bottom: 0,
            left: 0,
            right: 0,
            top: 0,
        },
        maxWidth: 0,
        minWdith: 0,
        padding: {
            bottom: 0,
            left: 0,
            right: 0,
            top: 0,
        },
        width: 0,
    };
}

export function initDiagramConfig(): DiagramConfig {
    return {
        canvas: initDimentionConfig(),
        panel: initDimentionConfig(),
        statement: initDimentionConfig()
    };
}

// Here we declare the default config.
export const DefaultConfig: DiagramConfig = initDiagramConfig();

DefaultConfig.statement = {
    height: 20,
    margin: {
        bottom: 0,
        left: 0,
        right: 0,
        top: 0,
    },
    maxWidth: 200,
    minWdith: 100,
    padding: {
        bottom: 5,
        left: 5,
        right: 5,
        top: 5,
    },
    width: 120,
};

DefaultConfig.canvas.padding = {
    bottom: 100,
    left: 50,
    right: 50,
    top: 50
};

// default width and height for the diagram
DefaultConfig.canvas.width = 500;
DefaultConfig.canvas.height = 500;

DefaultConfig.panel = {
    height: 20,
    margin: {
        bottom: 30,
        left: 30,
        right: 30,
        top: 30,
    },
    maxWidth: 0,
    minWdith: 0,
    padding: {
        bottom: 40,
        left: 40,
        right: 40,
        top: 40,
    },
    width: 0,
};
