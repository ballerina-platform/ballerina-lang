
const STATEMENT_HEIGHT = 20;

export class DiagramConfig {
    public canvas = {
        padding: {
            bottom: 10,
            top: 10,
        },
        // tslint:disable-next-line:object-literal-sort-keys
        height: 500,
        width: 500,
    };

    public statement = {
        actionHeight: STATEMENT_HEIGHT + 15,
        height: STATEMENT_HEIGHT,
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

    public panel = {
        gutter: {
            h: 30,
            v: 30
        },
        padding: {
            bottom: 0,
            left: 0,
            right: 0,
            top: 0,
        },
    };

    public panelHeading = {
        height: 60,
        padding: {
            bottom: 0,
            left: 0,
            right: 0,
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

    public flowCtrl = {
        bottomMargin: STATEMENT_HEIGHT / 2,
        condition: {
            height: STATEMENT_HEIGHT * 3
        },
        foreach: {
            height: STATEMENT_HEIGHT * 2,
            width: STATEMENT_HEIGHT * 3
        },
        leftMargin: STATEMENT_HEIGHT,
        leftMarginDefault: 60,
        whileGap: STATEMENT_HEIGHT,
    };
}

export const DefaultConfig = new DiagramConfig();
