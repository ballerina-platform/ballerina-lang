/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ballerinalang.containers.docker.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Random;

/**
 * Utility methods.
 */
public class Utils {

    private static String[] adjectives = new String[]{"able", "abnormal", "above", "absent", "absolute", "abstract",
            "absurd", "academic", "acceptable", "accessible", "accounting", "accurate", "accused", "active", "actual",
            "acute", "added", "additional", "adequate", "adjacent", "administrative", "adult", "advanced", "adverse",
            "advisory", "aesthetic", "afraid", "aggregate", "aggressive", "agreed", "agricultural", "alert", "alien",
            "alive", "alleged", "allied", "alone", "alright", "alternative", "amateur", "amazing", "ambiguous",
            "ambitious", "ample", "ancient", "angry", "annual", "anonymous", "anxious", "appalling", "apparent",
            "applicable", "applied", "appointed", "appropriate", "approved", "arbitrary", "archaeological",
            "architectural", "armed", "artificial", "artistic", "ashamed", "asleep", "assistant", "associated",
            "astonishing", "atomic", "attempted", "attractive", "automatic", "autonomous", "available", "average",
            "awake", "aware", "awful", "awkward", "back", "bad", "balanced", "bare", "basic", "beautiful", "beneficial",
            "big", "binding", "biological", "bitter", "bizarre", "black", "blank", "bleak", "blind", "blonde", "bloody",
            "blue", "bodily", "bold", "bored", "boring", "bottom", "bourgeois", "brave", "brief", "bright", "brilliant",
            "broad", "broken", "brown", "bureaucratic", "burning", "busy", "calm", "capable", "capital", "careful",
            "casual", "causal", "cautious", "central", "certain", "changing", "characteristic", "charming", "cheap",
            "cheerful", "chemical", "chief", "chosen", "chronic", "circular", "civic", "civil", "civilian", "classic",
            "classical", "clean", "clear", "clerical", "clever", "clinical", "close", "closed", "cooperative",
            "coastal", "cognitive", "coherent", "cold", "collective", "colonial", "color-blind", "colorful", "combined",
            "comfortable", "coming", "commercial", "common", "communist", "comparable", "comparative", "compatible",
            "competent", "competitive", "complementary", "complete", "complex", "complicated", "comprehensive",
            "compulsory", "conceptual", "concerned", "concrete", "confident", "confidential", "conscious",
            "conservative", "considerable", "consistent", "constant", "constitutional", "constructive", "contemporary",
            "content", "continental", "continued", "continuing", "continuous", "contractual", "contrary", "controlled",
            "controversial", "convenient", "conventional", "convincing", "cool", "corporate", "correct",
            "corresponding", "costly", "crazy", "creative", "criminal", "critical", "crucial", "crude", "cruel",
            "cultural", "curious", "current", "daily", "damaging", "damp", "dangerous", "dark", "dead", "deadly",
            "deaf", "dear", "decent", "decisive", "decorative", "deep", "defensive", "definite", "deliberate",
            "delicate", "delicious", "delighted", "delightful", "democratic", "dense", "departmental", "dependent",
            "depending", "depressed", "desirable", "desired", "desperate", "detailed", "determined", "developed",
            "developing", "devoted", "different", "differential", "difficult", "digital", "diplomatic", "direct",
            "dirty", "disabled", "disastrous", "disciplinary", "distant", "distinct", "distinctive", "distinguished",
            "distributed", "diverse", "divine", "domestic", "dominant", "double", "doubtful", "dramatic", "dreadful",
            "driving", "drunk", "dry", "dual", "due", "dull", "dynamic", "eager", "early", "eastern", "easy",
            "economic", "educational", "effective", "efficient", "elaborate", "elderly", "elected", "electoral",
            "electric", "electrical", "electronic", "elegant", "eligible", "embarrassed", "embarrassing", "emotional",
            "empirical", "empty", "encouraging", "endless", "enhanced", "enjoyable", "enormous", "enthusiastic",
            "entire", "environmental", "equal", "equivalent", "essential", "established", "estimated", "eternal",
            "ethical", "ethnic", "eventual", "everyday", "evident", "evil", "evolutionary", "exact", "excellent",
            "exceptional", "excess", "excessive", "excited", "exciting", "exclusive", "executive", "existing",
            "exotic", "expected", "expensive", "experienced", "experimental", "expert", "explicit", "express",
            "extended", "extensive", "external", "extra", "extraordinary", "extreme", "faint", "fair", "faithful",
            "false", "familiar", "famous", "fantastic", "far", "fascinating", "fashionable", "fast", "fat", "fatal",
            "favorable", "favorite", "feasible", "federal", "fellow", "female", "feminine", "fierce", "final",
            "financial", "fine", "finished", "firm", "first", "fiscal", "fit", "fixed", "flat", "flexible", "following",
            "fond", "foolish", "foreign", "formal", "formidable", "forthcoming", "fortunate", "forward", "fragile",
            "free", "frequent", "fresh", "friendly", "frightened", "front", "frozen", "futile", "full", "full-time",
            "fun", "functional", "fundamental", "funny", "furious", "future", "gastric"};

    private static String[] nouns = new String[]{"abbey", "ability", "abolition", "abrasion", "absence", "absorption",
            "abuse", "academic", "academy", "accent", "acceptance", "access", "accident", "accommodation", "accord",
            "accordance", "account", "accountability", "accountant", "accumulation", "accuracy", "accusation",
            "achievement", "acid", "acquaintance", "acquisition", "acre", "act", "action", "activist", "activity",
            "actor", "actress", "adaptation", "addition", "address", "adjective", "adjustment", "administration",
            "administrator", "admiration", "admission", "adoption", "adult", "advance", "advantage", "adventure",
            "advertisement", "advertising", "advice", "adviser", "advocate", "affair", "affection", "affinity",
            "afternoon", "age", "agency", "agenda", "agent", "aggression", "agony", "agreement", "agriculture", "aid",
            "aids", "aim", "air", "aircraft", "airline", "airport", "alarm", "album", "alcohol", "allegation",
            "alliance", "allocation", "allowance", "ally", "altar", "alteration", "alternative", "aluminum", "amateur",
            "ambassador", "ambiguity", "ambition", "ambulance", "amendment", "amount", "amp", "amusement", "analogy",
            "analysis", "analyst", "ancestor", "angel", "anger", "angle", "animal", "ankle", "anniversary",
            "announcement", "answer", "ant", "antibody", "anticipation", "anxiety", "apartment", "apology", "apparatus",
            "appeal", "appearance", "appendix", "appetite", "apple", "applicant", "application", "appointment",
            "appraisal", "appreciation", "approach", "approval", "aquarium", "arc", "arch", "archbishop", "architect",
            "architecture", "archive", "area", "arena", "argument", "arm", "armchair", "army", "arrangement", "array",
            "arrest", "arrival", "arrow", "art", "article", "artist", "ash", "aspect", "aspiration", "assault",
            "assembly", "assertion", "assessment", "asset", "assignment", "assistance", "assistant", "associate",
            "association", "assumption", "assurance", "asylum", "athlete", "atmosphere", "atom", "attachment", "attack",
            "attacker", "attainment", "attempt", "attendance", "attention", "attitude", "attraction", "attribute",
            "auction", "audience", "audit", "auditor", "aunt", "author", "authority", "autonomy", "autumn",
            "availability", "avenue", "average", "aviation", "award", "awareness", "axis", "baby", "back", "background",
            "backing", "bacon", "bacteria", "bag", "bail", "balance", "balcony", "ball", "ballet", "balloon", "ballot",
            "ban", "banana", "band", "bang", "bank", "banker", "banking", "bankruptcy", "banner", "bar", "bargain",
            "barn", "barrel", "barrier", "base", "basement", "basin", "basis", "basket", "bass", "bastion", "bat",
            "batch", "bath", "bathroom", "battery", "battle", "bay", "beach", "beam", "bean", "bear", "beard",
            "bearing", "beast", "beat", "beauty", "bed", "bedroom", "bee", "beef", "beer", "beginning", "behalf",
            "behavior", "being", "belief", "bell", "belly", "belt", "bench", "bend", "beneficiary", "benefit", "bet",
            "bias", "bible", "bicycle", "bid", "bike", "bile", "bill", "bin", "biography", "biology", "bird", "birth",
            "birthday", "biscuit", "bishop", "bit", "bit", "bite", "black", "blade", "blame", "blanket", "blast",
            "blessing", "block", "blocker", "blood", "blow", "blue", "board", "boat", "body", "boiler", "bolt", "bomb",
            "bomber", "bond", "bone", "bonus", "book", "booking", "booklet", "boom", "boost", "boot", "border",
            "borough", "boss", "bottle", "bottom", "boundary", "bow", "bowel", "bowl", "bowler", "box", "boxing",
            "boy", "boyfriend", "bracket", "brain", "brake", "branch", "brand", "brandy", "brass", "breach", "bread",
            "break", "breakdown", "breakfast", "breast", "breath", "breed", "breeding", "breeze", "brewery", "brick",
            "bride", "bridge", "brigade", "broadcast", "brochure", "broker", "bronze", "brother", "brow", "brush",
            "bubble", "bucket", "budget", "builder", "building", "bulb", "bulk", "bull", "bullet", "bulletin", "bunch",
            "bundle", "burden", "bureau", "bureaucracy", "burial", "burn", "burst", "bus", "bush", "business",
            "businessman", "butter", "butterfly", "button", "buyer", "cab", "cabin", "cabinet", "cable", "cafe", "cage",
            "cake", "calcium", "calculation", "calendar", "calf", "call", "calm", "calorie", "camera", "camp",
            "campaign", "can", "canal", "cancer", "candidate", "candle", "canvas", "cap", "capability", "capacity",
            "capital", "capitalism"};

    /**
     * Returns a {@link File} object pointing to the specified resource file inside the resources folder.
     *
     * @param resourceName {@link String} resource name. This should be the relative path from the resources folder.
     * @return A {@link File} object constructed from the specified path.
     * @throws FileNotFoundException If the specified path is not found inside the resources folder.
     */
    public static File getResourceFile(String resourceName) throws FileNotFoundException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        if (resource != null) {
            return new File(resource.getFile());
        } else {
            throw new FileNotFoundException("Couldn't find file in resources: " + resourceName);
        }
    }

    /**
     * Generate a random name to be used as a Container name. The name consists of adjective_noun format.
     *
     * @return A {@link String} with a random adjective and noun combination.
     */
    public static String generateContainerName() {
        Random random = new Random();
        int iAdj = random.nextInt(400);
        int iNoun = random.nextInt(400);

        return adjectives[iAdj] + "_" + nouns[iNoun];
    }

    /**
     * Generate a random port number from the ephemeral port range.
     *
     * @return A random port number from the ephemeral port range.
     */
    public static int generateContainerPort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
            //..
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //..
                }
            }
        }

        // This wouldn't be executed.
        return 0;
    }
}
