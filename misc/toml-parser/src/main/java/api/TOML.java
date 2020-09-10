package api;

import internal.parser.ParserFactory;
import internal.parser.TomlParser;
import internal.parser.tree.STNode;
import sementic.analyser.TomlNodeTransformer;
import syntax.tree.KeyValue;
import syntax.tree.ModuleMemberDeclarationNode;
import syntax.tree.ModulePartNode;
import syntax.tree.Node;
import syntax.tree.SyntaxKind;
import syntax.tree.TableNode;
import syntax.tree.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TOML {
    private Map<String,Object> values;

    public TOML(){
        this.values = new HashMap<>();
    }

    public TOML(Map<String,Object> values){
        this.values = values;
    }

    private Map<String,Object> toMap () {
        return values;
    }

    public TOML read(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        ParserRunner parserRunner= new ParserRunner(content);
        values = parserRunner.parse();
        return this;
    }

    public String get(String key) {

        String[] split = key.split("\\.");
        if (split.length == 1){
            return (String) values.get(key);
        } else {
            String[] parentTables = Arrays.copyOf(split, split.length - 1);
            String lastKey = split[split.length-1];
            Map<String,Object> parentTable = values;
            String[] parentKeys= new String[parentTable.size()];
            for (int i = 0; i < parentTables.length; i++) {
                String parent = parentTables[i];
                parentKeys[i] = parent;
                String parentKey = String.join(".",Arrays.copyOf(parentKeys, i+1));
                Object o = parentTable.get(parentKey);
                if (o instanceof Map) {
                    parentTable = (Map<String, Object>) o;
                } else {
                    return null;
                }
            }
            return (String) parentTable.get(split[split.length-1]);
        }
    }

    public TOML getTable(String key) {
        Object value = values.get(key);
        if (value instanceof Map){
            Map<String, Object> map = (Map<String, Object>) value;
            return new TOML(map);
        } else {
            return null;
        }
    }

}
