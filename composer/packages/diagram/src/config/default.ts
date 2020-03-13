
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
        actionHeight: STATEMENT_HEIGHT * 2,
        expanded: {
            bottomMargin: 10,
            collapserHeight: 14,
            collapserWidth: 20,
            footer: 10,
            header: 40,
            labelGutter: 2,
            margin: 20,
            offset: 60,
            rightMargin: 30,
            topMargin: 10,
        },
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
            left: 8,
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
            bottom: 20,
            left: 0,
            right: 200,
            top: 20,
        },
    };

    public panelHeading = {
        height: 30,
        padding: {
            bottom: 0,
            left: 0,
            right: 0,
            top: 0,
        },
        title: {
            margin: {
                left: 20
            }
        }
    };

    public panelGroup = {
        header: {
            height: STATEMENT_HEIGHT * 2,
        },
        title: {
            margin: {
                left: 25
            }
        }
    };

    public lifeLine = {
        bottombox: {
            height: 45,
        },
        footer: {
            height: 30,
        },
        gutter: {
            h: 50,
        },
        header: {
            height: 30,
        },
        leftMargin: 120 / 2,
        line: {
            height: 135,
        },
        radius: {
            rx: 12,
            ry: 12,
        },
        topbox: {
            height: 45,
        },
        width: 120,
    };

    public clientLine = {
        footer: {
            height: 30,
        },
        gutter: {
            h: 50,
        },
        header: {
            height: 30,
        },
        leftMargin: 120 / 2,
        line: {
            height: 135,
        },
        width: 120,
    };

    public condition = {
        caseLabel: {
            height: 19,
            margin: {
                bottom: 5,
                left: 5,
                right: 5,
                top: 5,
            },
            padding: {
                left: 4,
                right: 4,
            }
        }
    };

    public flowCtrl = {
        bottomMargin: STATEMENT_HEIGHT / 2,
        condition: {
            bottomMargin: 20,
            height: STATEMENT_HEIGHT * 3,
        },
        foreach: {
            height: STATEMENT_HEIGHT * 2 + 10,
            width: STATEMENT_HEIGHT * 3 + 10
        },
        leftMargin: STATEMENT_HEIGHT,
        leftMarginDefault: 60,
        paddingTop: STATEMENT_HEIGHT / 4,
        rightMargin: STATEMENT_HEIGHT,
        whileGap: STATEMENT_HEIGHT,
    };

    public block = {
        bottomMargin: STATEMENT_HEIGHT,
        hoverRect: {
            leftMargin: 30
        },
        menuTriggerMargin: 12
    };

    public frame = {
        header: {
            height: STATEMENT_HEIGHT,
            width: 70
        },
        leftMarginDefault: 60,
        rightMargin: STATEMENT_HEIGHT,
        topMargin: 5,
    };
}

export const DefaultConfig = new DiagramConfig();
