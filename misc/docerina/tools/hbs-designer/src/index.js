const Handlebars = require("handlebars");
const fs = require("fs-extra");
const path = require("path");

const outPath = path.join(__dirname, "..", "dist");
const indexHBS = fs.readFileSync(path.join(__dirname, "index.hbs")).toString();
const data = fs.readFileSync(path.join(__dirname, "..", "data", "model.json")).toString();

function buildModules(modules) {
    const modHBS = fs.readFileSync(path.join(__dirname, "module.hbs")).toString();
    const modTemplate = Handlebars.compile(modHBS);
    modules.forEach((mod) => {
        const modOutDir = path.join(outPath, mod.id);
        fs.ensureDirSync(modOutDir);
        fs.writeFileSync(path.join(modOutDir, "index.html"), modTemplate(mod));
    });
}

try {
    var indexTemplate = Handlebars.compile(indexHBS);
    var model = JSON.parse(data);
    fs.ensureDirSync(outPath);
    fs.writeFileSync(path.join(outPath, "index.html"), indexTemplate(model));
    buildModules(model.modules);
    fs.copy(path.join(__dirname, "styles"), path.join(outPath, "styles"));
    fs.copy(path.join(__dirname, "images"), path.join(outPath, "images"));
    
} catch(e) {
    console.log("Error" + e.message);
}
