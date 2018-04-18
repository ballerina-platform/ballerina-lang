const fs = require('fs');
const request = require('request-promise');
const { getParserService } = require('../serverStarter');

function render (content) {
    const parseOpts = {
        content,
        filename: 'file.bal',
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }

    return getParserService()
    .then(({port}) => {
        return request.post({
            url: `http://127.0.0.1:${port||9091}/composer/ballerina/parser/file/validate-and-parse`,
            json: parseOpts,
        })
    })
    .then((body) => {
        jsonModel = JSON.stringify(body.model);
        const diagram = renderDiagram(body.model);
        return diagram;
    });
};


function renderDiagram(modelJson) {
    const page = `
    <!DOCTYPE html>
    <html>
    
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="file://${__dirname}/resources/css/bundle.css" />
        <link rel="stylesheet" href="file://${__dirname}/resources/css/default.css" />
        <link rel="stylesheet" href="file://${__dirname}/resources/css/less.css" />
        <style>
            .overlay {
                display: none;
            }
            .drop-zone.rect {
                fill-opacity: 0;
            }
            #diagram {
                overflow-x: hidden;
                overflow-y: scroll;
                height : 100%;
            }
        </style>
    </head>

    <body>
    <div class="ballerina-editor design-view-container" id="diagram">
    </div>
    </body>
    <script charset="UTF-8" src="file://${__dirname}/resources/js/diagram.js"></script>
    <script>
            const json = window.modeljsonbal = ${jsonModel};
            window.onresize = () => {
                ballerinaDiagram.renderDiagram(document.getElementById("diagram"), json, {
                    width: window.innerWidth - 6, height: window.innerHeight
                });
            };
            ballerinaDiagram.renderDiagram(document.getElementById("diagram"), json, {
                width: window.innerWidth - 6, height: window.innerHeight
            });
    </script>
    </html>
    `;

    return page;
}

module.exports.render = render;
module.exports.activate = getParserService;