package com.dhavalpateln.linkcast.database.kitsu;

import android.util.Log;

import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsuDB {
    private static KitsuDB instance;
    private final String TAG = "KitsuDB";
    private Map<String, Map<String, KitsuEpisodeData>> animeEpisodeMap;
    private Map<String, Map<String, KitsuEpisodeData>> mangaEpisodeMap;

    private KitsuDB() {
        this.animeEpisodeMap = new HashMap<>();
        this.mangaEpisodeMap = new HashMap<>();
    }

    public static KitsuDB getInstance() {
        if(instance == null) {
            instance = new KitsuDB();
        }
        return instance;
    }

    private Map<String, Map<String, KitsuEpisodeData>> getMapRef(boolean isAnime) {
        if(isAnime) return this.animeEpisodeMap;
        return this.mangaEpisodeMap;
    }

    private void setKitsuEpisodeData(String malID, boolean isAnime, KitsuEpisodeData data) {
        Map<String, Map<String, KitsuEpisodeData>> ref = getMapRef(isAnime);
        if(!ref.containsKey(malID)) {
            ref.put(malID, new HashMap<>());
        }
        ref.get(malID).put(data.getNumber(), data);
    }

    public KitsuEpisodeData getKitsuEpisodeData(String malID, String number, boolean isAnime) {
        Map<String, Map<String, KitsuEpisodeData>> ref = getMapRef(isAnime);
        if(ref.containsKey(malID) && ref.get(malID).containsKey(number)) {
            return ref.get(malID).get(number);
        }
        return null;
    }

    public void updateEpisodeNodes(List<EpisodeNode> nodes, String malID, boolean isAnime) {
        Map<String, KitsuEpisodeData> dataRef = getMapRef(isAnime).get(malID);
        if(dataRef == null) return;
        for(EpisodeNode node: nodes) {
            if(dataRef.containsKey(node.getEpisodeNumString())) {
                KitsuEpisodeData episodeData = dataRef.get(node.getEpisodeNumString());
                if(episodeData == null) continue;
                if(node.getTitle() == null) node.setTitle(episodeData.getTitle());
                if(node.getDescription() == null)   node.setDescription(episodeData.getDescription());
                if(node.getThumbnail() == null) node.setThumbnail(episodeData.getThumbnail());
            }
        }
    }

    public void fetchData(String malID, boolean isAnime) {
        if(getMapRef(isAnime).containsKey(malID))   return;
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection("https://kitsu.io/api/graphql");
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String externalSite = isAnime ? "MYANIMELIST_ANIME" : "MYANIMELIST_MANGA";
            String queryPayload = "query {\n" +
                    "  lookupMapping(externalId: " + malID + ", externalSite: " + externalSite + ") {\n" +
                    "    __typename\n" +
                    "    ... on Anime {\n" +
                    "      id\n" +
                    "      episodes(first: 2000) {\n" +
                    "        nodes {\n" +
                    "          number\n" +
                    "          titles {\n" +
                    "            canonical\n" +
                    "          }\n" +
                    "          description\n" +
                    "          thumbnail {\n" +
                    "            original {\n" +
                    "              url\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            SimpleHttpClient.setPayload(urlConnection, "query=" + URLEncoder.encode(queryPayload));

            String response = SimpleHttpClient.getResponse(urlConnection);

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray episodeData = jsonResponse.getJSONObject("data").getJSONObject("lookupMapping").getJSONObject("episodes").getJSONArray("nodes");

            for(int i = 0; i < episodeData.length(); i++) {
                JSONObject data = episodeData.getJSONObject(i);
                if(data != null) {
                    KitsuEpisodeData kitsuEpisodeData = new KitsuEpisodeData();
                    kitsuEpisodeData.setNumber(data.getString("number"));
                    kitsuEpisodeData.setTitle(data.getJSONObject("titles").getString("canonical"));
                    if(data.getJSONObject("description").has("en")) {
                        kitsuEpisodeData.setDescription(data.getJSONObject("description").getString("en"));
                    }
                    kitsuEpisodeData.setThumbnail(data.getJSONObject("thumbnail").getJSONObject("original").getString("url"));
                    setKitsuEpisodeData(malID, isAnime, kitsuEpisodeData);
                }
            }

            Log.d(TAG, "Data loaded");
        } catch (IOException e) {
            Log.d(TAG, "exception while fetching kitsu data");
        } catch (JSONException e) {
            Log.d(TAG, "exception while fetching kitsu data");
        }
    }
}
