const Handlebars = require("handlebars");
const fs = require("fs-extra");
const path = require("path");

const outPath = path.join(__dirname, "..", "dist");
const resPath = path.join(outPath, "resources");
const nodeModulesPath = path.join(__dirname, "..", "node_modules");
const indexHBS = fs.readFileSync(path.join(__dirname, "index.hbs")).toString();
const data = JSON.parse(fs.readFileSync(path.join(__dirname, "..", "data", "model.json")).toString());

// Partials
const partialPath = path.join(__dirname, "partial");
const headerHBS = fs.readFileSync(path.join(partialPath, 'head.hbs')).toString();
Handlebars.registerPartial("htmlHead", Handlebars.compile(headerHBS));
const allModHBS = fs.readFileSync(path.join(partialPath, 'all-modules.hbs')).toString();
Handlebars.registerPartial("allModules", Handlebars.compile(allModHBS));
const currentModHBS = fs.readFileSync(path.join(partialPath, 'current-module.hbs')).toString();
Handlebars.registerPartial("currentModule", Handlebars.compile(currentModHBS));

function buildModules(modules) {
    const modHBS = fs.readFileSync(path.join(__dirname, "module.hbs")).toString();
    const modTemplate = Handlebars.compile(modHBS);
    modules.forEach((mod) => {
        const modOutDir = path.join(outPath, mod.id);
        fs.ensureDirSync(modOutDir);
        fs.writeFileSync(path.join(modOutDir, "index.html"), modTemplate({ module: mod, data } ));
        if (mod.constructs){
            buildConstructs(mod);
        }
    });
}

function buildRecords(mod, records) {
    const recordHBS = fs.readFileSync(path.join(__dirname, "records.hbs")).toString();
    const recordTemplate = Handlebars.compile(recordHBS);
    records.forEach((record) => {
        const recordOutDir = path.join(outPath, mod.id, 'records');
        fs.ensureDirSync(recordOutDir);
        fs.writeFileSync(path.join(recordOutDir, record.name + ".html"), recordTemplate({ record, module: mod, data }));
    });
}
function buildObjects(mod, objects) {
    const objectsHBS = fs.readFileSync(path.join(__dirname, "objects.hbs")).toString();
    const objectsTemplate = Handlebars.compile(objectsHBS);
    objects.forEach((object) => {
        const objectOutDir = path.join(outPath, mod.id, 'objects');
        fs.ensureDirSync(objectOutDir);
        fs.writeFileSync(path.join(objectOutDir, object.name + ".html"), objectsTemplate({ object, module: mod, data }));
    });
}
function buildClients(mod, clients) {
    const clientsHBS = fs.readFileSync(path.join(__dirname, "clients.hbs")).toString();
    const clientsTemplate = Handlebars.compile(clientsHBS);
    clients.forEach((client) => {
        const clientOutDir = path.join(outPath, mod.id, 'clients');
        fs.ensureDirSync(clientOutDir);
        fs.writeFileSync(path.join(clientOutDir, client.name + ".html"), clientsTemplate({ client, module: mod, data }));
    });
}

function buildFunctions(mod, functions) {
    const functionsHBS = fs.readFileSync(path.join(__dirname, "functions.hbs")).toString();
    const functionsTemplate = Handlebars.compile(functionsHBS);
    const funcOutDir = path.join(outPath, mod.id, 'functions');
    fs.ensureDirSync(funcOutDir);
    fs.writeFileSync(path.join(funcOutDir, "functions.html"), functionsTemplate({ functions, module: mod, data }));
}

function buildConstants(mod, constants) {
    const constantsHBS = fs.readFileSync(path.join(__dirname, "constants.hbs")).toString();
    const constantsTemplate = Handlebars.compile(constantsHBS);
    const constantsOutDir = path.join(outPath, mod.id, 'constants');
    fs.ensureDirSync(constantsOutDir);
    fs.writeFileSync(path.join(constantsOutDir, "constants.html"), constantsTemplate({ constants, module: mod, data }));
}

function buildAnnotations(mod, annotations) {
    const annotationsHBS = fs.readFileSync(path.join(__dirname, "annotations.hbs")).toString();
    const annotationsTemplate = Handlebars.compile(annotationsHBS);
    const annotationsOutDir = path.join(outPath, mod.id, 'annotations');
    fs.ensureDirSync(annotationsOutDir);
    fs.writeFileSync(path.join(annotationsOutDir, "annotations.html"), annotationsTemplate({ annotations, module: mod, data }));
}

function buildConstructs(mod) {
    const records = mod.constructs.records;
    const objects = mod.constructs.objects;
    const clients = mod.constructs.clients;
    const functions = mod.constructs.functions;
    const listeners = mod.constructs.listeners;
    const constants = mod.constructs.constants;
    const annotations = mod.constructs.annotations;
    const errors = mod.constructs.errors;

    if (records.length != 0) {
        buildRecords(mod, records);
    }
    if (objects.length != 0) {
        buildObjects(mod, objects);
    }
    if (clients.length != 0) {
        buildClients(mod, clients);
    }
    if (functions.length != 0) {
        buildFunctions(mod, functions);
    }
    if (listeners.length != 0) {
        buildListeners(mod, listeners);
    }
    if (constants.length != 0) {
        buildConstants(mod, constants);
    }
    if (annotations.length != 0) {
        buildAnnotations(mod, annotations);
    }
    if (errors.length != 0) {
        buildErrors(mod, errors);
    }
}

try {
    fs.emptyDirSync(outPath);
    var indexTemplate = Handlebars.compile(indexHBS);
    fs.ensureDirSync(outPath);
    fs.writeFileSync(path.join(outPath, "index.html"), indexTemplate(data));
    buildModules(data.modules);
    fs.copySync(path.join(__dirname, "styles"), path.join(outPath, "styles"));
    fs.ensureDirSync(resPath);
    fs.copySync(path.join(nodeModulesPath, "semantic-ui-css", "semantic.min.css"), 
        path.join(resPath, "semantic.min.css"));
    fs.copySync(path.join(nodeModulesPath, "semantic-ui-css", "semantic.min.js"), 
        path.join(resPath, "semantic.min.js"));
    fs.copySync(path.join(nodeModulesPath, "jquery", "dist", "jquery.min.js"), 
        path.join(resPath, "jquery.min.js"));
    fs.copySync(path.join(__dirname, "images"), path.join(outPath, "images"));
} catch(e) {
    console.log("Error" + e.message);
}
