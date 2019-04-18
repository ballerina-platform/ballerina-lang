const Handlebars = require("handlebars");
const fs = require("fs-extra");
const path = require("path");

const outPath = path.join(__dirname, "..", "dist");
const indexHBS = fs.readFileSync(path.join(__dirname, "index.hbs")).toString();
const data = JSON.parse(fs.readFileSync(path.join(__dirname, "..", "data", "model.json")).toString());

// Partials
const partialPath = path.join(__dirname, "partial");
const headerHBS = fs.readFileSync(path.join(partialPath, 'head.hbs')).toString();
Handlebars.registerPartial("htmlHead", Handlebars.compile(headerHBS));

function buildModules(modules) {
    const modHBS = fs.readFileSync(path.join(__dirname, "module.hbs")).toString();
    const modTemplate = Handlebars.compile(modHBS);
    modules.forEach((mod) => {
        const modOutDir = path.join(outPath, mod.id);
        fs.ensureDirSync(modOutDir);
        fs.writeFileSync(path.join(modOutDir, "index.html"), modTemplate(mod));
        if (mod.constructs){
            buildConstructs(mod);
        }
    });
}

function buildRecords(modId, records) {
    const recordHBS = fs.readFileSync(path.join(__dirname, "records.hbs")).toString();
    const recordTemplate = Handlebars.compile(recordHBS);
    records.forEach((rec) => {
        const recOutDir = path.join(outPath, modId, 'records');
        fs.ensureDirSync(recOutDir);
        fs.writeFileSync(path.join(recOutDir, rec.name + ".html"), recordTemplate({ rec, all: data }));
    });
}
function buildObjects(modId, objects) {
    const objectsHBS = fs.readFileSync(path.join(__dirname, "objects.hbs")).toString();
    const objectsTemplate = Handlebars.compile(objectsHBS);
    objects.forEach((obj) => {
        const objOutDir = path.join(outPath, modId, 'objects');
        fs.ensureDirSync(objOutDir);
        fs.writeFileSync(path.join(objOutDir, obj.name + ".html"), objectsTemplate({ obj, all: data }));
    });
}
function buildClients(modId, clients) {
    const clientsHBS = fs.readFileSync(path.join(__dirname, "clients.hbs")).toString();
    const clientsTemplate = Handlebars.compile(clientsHBS);
    clients.forEach((clnt) => {
        const clntOutDir = path.join(outPath, modId, 'clients');
        fs.ensureDirSync(clntOutDir);
        fs.writeFileSync(path.join(clntOutDir, clnt.name + ".html"), clientsTemplate({ clnt, all: data }));
    });
}

function buildFunctions(modId, functions) {
    const functionsHBS = fs.readFileSync(path.join(__dirname, "functions.hbs")).toString();
    const functionsTemplate = Handlebars.compile(functionsHBS);
    const funcOutDir = path.join(outPath, modId, 'functions');
    fs.ensureDirSync(funcOutDir);
    fs.writeFileSync(path.join(funcOutDir, "functions.html"), functionsTemplate({ functions, all: data }));
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
        buildRecords(mod.id, records);
    }
    if (objects.length != 0) {
        buildObjects(mod.id, objects);
    }
    if (clients.length != 0) {
        buildClients(mod.id, clients);
    }
    if (functions.length != 0) {
        buildFunctions(mod.id, functions);
    }
    if (listeners.length != 0) {
        buildListeners(mod.id, listeners);
    }
    if (constants.length != 0) {
        buildConstants(mod.id, constants);
    }
    if (annotations.length != 0) {
        buildAnnotations(mod.id, annotations);
    }
    if (errors.length != 0) {
        buildErrors(mod.id, errors);
    }
}

try {
    var indexTemplate = Handlebars.compile(indexHBS);
    fs.ensureDirSync(outPath);
    fs.writeFileSync(path.join(outPath, "index.html"), indexTemplate(data));
    buildModules(data.modules);
    fs.copy(path.join(__dirname, "styles"), path.join(outPath, "styles"));
    fs.copy(path.join(__dirname, "images"), path.join(outPath, "images"));
    
} catch(e) {
    console.log("Error" + e.message);
}
