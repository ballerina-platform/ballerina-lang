package org.ballerinalang.semver.checker.util;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class FileUtils {

    public static void createFile(JsonArray arr, String ss) {;
        JsonObject ob = new JsonObject();
        ob.addProperty("VersionNumber", ss);
        ob.add("Signature", arr);

        String json = new Gson().toJson(ob);
        try {
            File file = new File("filename.json");

            if (!file.exists()) {
                file.createNewFile();
                FileWriter fe = new FileWriter(file);
                fe.write(json);
                fe.close();
            }
            else {
                JsonParser jsonP = new JsonParser();
                Object obj = null;
                JsonObject object = null;
                JsonElement versionNumber = null;
                String a = null;
                try (FileReader reader = new FileReader("filename.json")) {
                    obj = jsonP.parse(reader);
                    String s = obj.toString();
                    object = (JsonObject) obj;
                   /* versionNumber =  object.get("VersionNumber");
                    System.out.println(versionNumber);
                    a = String.valueOf(versionNumber);
                    System.out.println(ob.get("VersionNumber"));*/
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Gson g = new Gson();
                Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> firstMap = g.fromJson(ob, mapType);
                Map<String, Object> secondMap = g.fromJson(object, mapType);
                JsonObject object1 = new JsonObject();
                JsonObject object2 = new JsonObject();
                object1.add("Signature",arr);
                object2.add("Signature",object.get("Signature"));
                Map<String, Object> sig = g.fromJson(object1, mapType);
                Map<String, Object> sig1 = g.fromJson(object2, mapType);
                if (firstMap.get("VersionNumber").equals(secondMap.get("VersionNumber"))){
                    System.out.println("fine");
                }
                else{
                    object.get("Signature");

                    MapDifference<String , Object> difference = Maps.difference(firstMap,secondMap);
                    System.out.println(Maps.difference(firstMap,secondMap));


                    if (!difference.areEqual()) {
                        if (difference.entriesOnlyOnLeft().size() > 0) {
                            System.out.println("Entries only on the left\n--------------------------");
                            difference.entriesOnlyOnLeft().forEach((key, value) -> System.out.println(key + ": " + value));
                        }

                        if (difference.entriesOnlyOnRight().size() > 0) {
                            System.out.println("\n\nEntries only on the right\n--------------------------");
                            difference.entriesOnlyOnRight().forEach((key, value) -> System.out.println(key + ": " + value));
                        }

                        if (difference.entriesDiffering().size() > 0) {
                            System.out.println("\n\nEntries differing\n--------------------------");
                            difference.entriesDiffering().forEach((key, value) -> System.out.println(key + ": " + value));

                        }
                    }
                    String versionNumber1 = firstMap.get("VersionNumber").toString();
                    String versionNumber2 = secondMap.get("VersionNumber").toString();
                    String[] split1 = versionNumber1.split("\\.");
                    String[] split2 = versionNumber2.split("\\.");

                   if (split1[0].equals(split2[0]) && split1[1].equals(split2[1]) && !split1[2].equals(split2[2])){
                       JsonArray jsonElement = (JsonArray) object.get("Signature");
                       JsonArray jsonElement1 = arr;
                       List<JsonArray> jsonArrays = Arrays.asList(jsonElement);
                       List<JsonArray> jsonArrays1 = Arrays.asList(jsonElement1);
                       List<String> differences = new ArrayList(Sets.difference(Sets.newHashSet(jsonElement), Sets.newHashSet(jsonElement1)));
                       List<String> differences1 = new ArrayList(Sets.difference(Sets.newHashSet(jsonElement1), Sets.newHashSet(jsonElement)));
                       System.out.println(differences);
                       System.out.println(differences1);
                       MapDifference<String , Object> differenceSig = Maps.difference(sig,sig1);
                        if (!differenceSig.areEqual()) {
                           if (sig.get("Signature").toString().replaceAll(" ","").equals(sig1.get("Signature").toString().replaceAll(" ",""))){
                               System.out.println("Matches  change");
                           }
                           else {
                               /*int size = differences.size();
                               for (int i=0; i<size;i++){
                                   var phrase1Words = differences.get(i).split(" ");
                                   var phrase2Words = differences1.get(i).split(" ");
                                   Map<String, Object> dif = g.fromJson(String.valueOf(phrase1Words), mapType);
                                   Map<String, Object> dif1 = g.fromJson(String.valueOf(phrase2Words), mapType);

                               }*/
                               System.out.println("Patch Version change does not match with code change");
                           }
                        }
                        else{
                            System.out.println("Matches the change");
                        }
                    }
                   else{
                       System.out.println("Not a patch version change");
                   }
                }

            }


        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

