"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var fs = require("fs");
var path = require("path");
var yaml = require("js-yaml");
var plist = require("plist");
var Language;
(function (Language) {
    Language["TypeScript"] = "ballerina";
    Language["TypeScriptReact"] = "ballerina";
})(Language || (Language = {}));
var Extension;
(function (Extension) {
    Extension["TmLanguage"] = "tmLanguage";
    Extension["TmTheme"] = "tmTheme";
    Extension["YamlTmLangauge"] = "YAML-tmLanguage";
    Extension["YamlTmTheme"] = "YAML-tmTheme";
})(Extension || (Extension = {}));
function file(language, extension) {
    return path.join(__dirname, '..', language + "." + extension);
}
function writePlistFile(grammar, fileName) {
    var text = plist.build(grammar);
    fs.writeFileSync(fileName, text);
}
function readYaml(fileName) {
    var text = fs.readFileSync(fileName, "utf8");
    return yaml.safeLoad(text);
}
function changeTsToTsx(str) {
    return str.replace(/\.ts/g, '.tsx');
}
function transformGrammarRule(rule, propertyNames, transformProperty) {
    for (var _i = 0, propertyNames_1 = propertyNames; _i < propertyNames_1.length; _i++) {
        var propertyName_1 = propertyNames_1[_i];
        if (typeof rule[propertyName_1] === 'string') {
            rule[propertyName_1] = transformProperty(rule[propertyName_1]);
        }
    }
    for (var propertyName in rule) {
        var value = rule[propertyName];
        if (typeof value === 'object') {
            transformGrammarRule(value, propertyNames, transformProperty);
        }
    }
}
function transformGrammarRepository(grammar, propertyNames, transformProperty) {
    var repository = grammar.repository;
    for (var key in repository) {
        transformGrammarRule(repository[key], propertyNames, transformProperty);
    }
}
function changeTsToTsxGrammar(grammar, variables) {
    var tsxUpdates = updateGrammarVariables(readYaml(file(Language.TypeScriptReact, Extension.YamlTmLangauge)), variables);
    // Update name, file types, scope name and uuid
    for (var key in tsxUpdates) {
        if (key !== "repository") {
            grammar[key] = tsxUpdates[key];
        }
    }
    // Update scope names to .tsx
    transformGrammarRepository(grammar, ["name", "contentName"], changeTsToTsx);
    // Add repository items
    var repository = grammar.repository;
    var updatesRepository = tsxUpdates.repository;
    for (var key in updatesRepository) {
        switch (key) {
            case "expressionWithoutIdentifiers":
                // Update expression
                repository[key].patterns.unshift(updatesRepository[key].patterns[0]);
                break;
            default:
                // Add jsx
                repository[key] = updatesRepository[key];
        }
    }
    return grammar;
}
function replacePatternVariables(pattern, variableReplacers) {
    var result = pattern;
    for (var _i = 0, variableReplacers_1 = variableReplacers; _i < variableReplacers_1.length; _i++) {
        var _a = variableReplacers_1[_i], variableName = _a[0], value = _a[1];
        result = result.replace(variableName, value);
    }
    return result;
}
function updateGrammarVariables(grammar, variables) {
    delete grammar.variables;
    var variableReplacers = [];
    for (var variableName in variables) {
        // Replace the pattern with earlier variables
        var pattern = replacePatternVariables(variables[variableName], variableReplacers);
        variableReplacers.push([new RegExp("{{" + variableName + "}}", "gim"), pattern]);
    }
    console.log(variableReplacers);
    transformGrammarRepository(grammar, ["begin", "end", "match"], function (pattern) { return replacePatternVariables(pattern, variableReplacers); });
    return grammar;
}
function buildGrammar() {
    var tsGrammarBeforeTransformation = readYaml(file(Language.TypeScript, Extension.YamlTmLangauge));
    var variables = tsGrammarBeforeTransformation.variables;
    console.log(variables, '<-------------------------------');
    var tsGrammar = updateGrammarVariables(tsGrammarBeforeTransformation, variables);
    // Write TypeScript.tmLanguage
    writePlistFile(tsGrammar, file(Language.TypeScript, Extension.TmLanguage));
    // Write TypeScriptReact.tmLangauge
    // const tsxGrammar = changeTsToTsxGrammar(tsGrammar, variables);
    // writePlistFile(tsxGrammar, file(Language.TypeScriptReact, Extension.TmLanguage));
}
function changeTsToTsxTheme(theme) {
    var tsxUpdates = readYaml(file(Language.TypeScriptReact, Extension.YamlTmTheme));
    // Update name, uuid
    for (var key in tsxUpdates) {
        if (key !== "settings") {
            theme[key] = tsxUpdates[key];
        }
    }
    // Update scope names to .tsx
    var settings = theme.settings;
    for (var i = 0; i < settings.length; i++) {
        settings[i].scope = changeTsToTsx(settings[i].scope);
    }
    // Add additional setting items
    theme.settings = theme.settings.concat(tsxUpdates.settings);
    return theme;
}
function buildTheme() {
    var tsTheme = readYaml(file(Language.TypeScript, Extension.YamlTmTheme));
    // Write TypeScript.tmTheme
    writePlistFile(tsTheme, file(Language.TypeScript, Extension.TmTheme));
    // Write TypeScriptReact.thTheme
    // const tsxTheme = changeTsToTsxTheme(tsTheme);
    // writePlistFile(tsxTheme, file(Language.TypeScriptReact, Extension.TmTheme));
}
buildGrammar();
//buildTheme();
