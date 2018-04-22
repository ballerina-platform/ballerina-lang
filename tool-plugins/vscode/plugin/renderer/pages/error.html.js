module.exports = `
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="file://${__dirname}/../resources/bundle.css" />
        <style>
            .warning {
                color:rgb(255, 255, 0);
            }
        </style>
    </head>

    <body>
    <div class="ballerina-editor design-view-container" id="diagram">
        <br/>
        <span class="warning fw fw-warning"></span> Ballerina sdk path is not configured</p>
        <p>Please configure Ballerina SDK path for diagram rendering to work.</p>
        <a href="command:workbench.action.openGlobalSettings">Open Settings</a>
    </div>
    </body>
    </html>
`;
