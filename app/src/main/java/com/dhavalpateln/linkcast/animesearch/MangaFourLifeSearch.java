package com.dhavalpateln.linkcast.animesearch;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.dhavalpateln.linkcast.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MangaFourLifeSearch extends AnimeSearch {

    private Map<String, String> mangaMap;

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        term = term.toLowerCase();
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        Map<String, String> mangas = getMangaList();
        Set<String> resultSet = new HashSet<>();
        for(String key: mangas.keySet()) {
            if(key.contains(term) && !resultSet.contains(mangas.get(key))) {
                AnimeLinkData data = new AnimeLinkData();
                data.setTitle(Utils.capFirstLetters(key));
                data.setUrl(ProvidersData.MANGAFOURLIFE.URL + "/manga/" + mangas.get(key));
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, "https://cover.nep.li/cover/" + mangas.get(key) + ".jpg");
                dataMap.put(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.MANGAFOURLIFE.NAME);
                data.setData(dataMap);
                result.add(data);
                resultSet.add(mangas.get(key));
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "manga4life";
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }

    @Override
    public boolean isMangeSource() {
        return true;
    }

    private Map<String, String> getMangaList() {
        if(this.mangaMap == null) {
            this.mangaMap = new HashMap<>();
            try {
                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection("https://manga4life.com/_search.php");
                JSONArray mangaList = new JSONArray(SimpleHttpClient.getResponse(urlConnection));
                for(int i = 0; i < mangaList.length(); i++) {
                    JSONObject mangaObj = mangaList.getJSONObject(i);
                    mangaMap.put(mangaObj.getString("s").toLowerCase(), mangaObj.getString("i"));
                    for(int alternateTitleNum = 0; alternateTitleNum < mangaObj.getJSONArray("a").length(); alternateTitleNum++) {
                        mangaMap.put(mangaObj.getJSONArray("a").getString(alternateTitleNum).toLowerCase(), mangaObj.getString("i"));
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        return this.mangaMap;
    }
}
