
export class DiagramConfig {
    public statement = {
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

    public canvas = {
        padding: {
            bottom: 0,
            top: 40,
        },
        // tslint:disable-next-line:object-literal-sort-keys
        height: 500,
        width: 500,
    };

    public panel = {
        gutter: {
            h: 30,
            v: 30
        },
        padding: {
            bottom: 40,
            left: 40,
            right: 40,
            top: 40,
        },
    };

    public panelHeading = {
        height: 25,
        padding: {
            bottom: 0,
            left: 5,
            right: 5,
            top: 0,
        },
        title: {
            margin: {
                left: 25
            }
        }
    };

    public lifeLine = {
        footer: {
            height: 30,
        },
        gutter: {
            h: 50,
        },
        header: {
            height: 30,
        },
        line: {
            height: 135,
        },
        width: 120,
    };
}

export const DefaultConfig = new DiagramConfig();
