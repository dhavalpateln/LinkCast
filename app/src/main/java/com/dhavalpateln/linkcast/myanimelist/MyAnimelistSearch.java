package com.dhavalpateln.linkcast.myanimelist;

import android.net.Uri;

import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyAnimelistSearch {

    public static Map<String, Integer> genreMap = new HashMap<>();
    public static String[] animeTypes = new String[] {"TV", "Movie", "OVA", "Special", "ONA"};
    public static String[] statusTypes = new String[] {"Airing", "Complete", "Upcoming"};
    public static String[] genres = new String[] {"Action", "Adventure", "Avant Garde", "Award Winning", "Boys Love", "Comedy", "Drama", "Ecchi", "Fantasy", "Girls Love", "Gourmet", "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Suspense", "Work Life"};
    public static String[] themes = new String[] {"Adult Cast", "Anthropomorphic", "CGDCT", "Childcare", "Combat Sports", "Crossdressing", "Delinquents", "Detective", "Educational", "Gag Humor", "Gore", "Harem", "High Stakes Game", "Historical", "Idols (Female)", "Idols (Male)", "Isekai", "Iyashikei", "Love Polygon", "Magical Sex Shift", "Mahou Shoujo", "Martial Arts", "Mecha", "Medical", "Military", "Music", "Mythology", "Organized Crime", "Otaku Culture", "Parody", "Performing Arts", "Pets", "Psychological", "Racing", "Reincarnation", "Reverse Harem", "Romantic Subtext", "Samurai", "School", "Showbiz", "Space", "Strategy Game", "Super Power", "Survival", "Team Sports", "Time Travel", "Vampire", "Video Game", "Visual Arts", "Workplace"};
    public static String[] demographics = new String[] {"Josei", "Kids", "Seinen", "Shoujo", "Shounen"};

    static {
        genreMap.put("action", 1);
        genreMap.put("adventure", 2);
        genreMap.put("avant garde", 5);
        genreMap.put("award winning", 46);
        genreMap.put("boys love", 28);
        genreMap.put("comedy", 4);
        genreMap.put("drama", 8);
        genreMap.put("fantasy", 10);
        genreMap.put("girls love", 26);
        genreMap.put("gourmet", 47);
        genreMap.put("horror", 14);
        genreMap.put("mystery", 7);
        genreMap.put("romance", 22);
        genreMap.put("sci-fi", 24);
        genreMap.put("slice of life", 36);
        genreMap.put("sports", 30);
        genreMap.put("supernatural", 37);
        genreMap.put("suspense", 41);
        genreMap.put("work life", 48);
        genreMap.put("ecchi", 9);
        genreMap.put("erotica", 49);
        genreMap.put("hentai", 12);
        genreMap.put("racing", 3);
        genreMap.put("mythology", 6);
        genreMap.put("strategy game", 11);
        genreMap.put("harem", 35);
        genreMap.put("historical", 13);
        genreMap.put("martial arts", 17);
        genreMap.put("mecha", 18);
        genreMap.put("military", 38);
        genreMap.put("music", 19);
        genreMap.put("parody", 20);
        genreMap.put("detective", 39);
        genreMap.put("psychological", 40);
        genreMap.put("samurai", 21);
        genreMap.put("school", 23);
        genreMap.put("space", 29);
        genreMap.put("super power", 31);
        genreMap.put("vampire", 32);
        genreMap.put("adult cast", 50);
        genreMap.put("anthropomorphic", 51);
        genreMap.put("cgdct", 52);
        genreMap.put("childcare", 53);
        genreMap.put("combat sports", 54);
        genreMap.put("crossdressing", 81);
        genreMap.put("delinquents", 55);
        genreMap.put("educational", 56);
        genreMap.put("gag humor", 57);
        genreMap.put("gore", 58);
        genreMap.put("high stakes game", 59);
        genreMap.put("idols (female)", 60);
        genreMap.put("idols (male)", 61);
        genreMap.put("isekai", 62);
        genreMap.put("iyashikei", 63);
        genreMap.put("love polygon", 64);
        genreMap.put("magical sex shift", 65);
        genreMap.put("mahou shoujo", 66);
        genreMap.put("medical", 67);
        genreMap.put("organized crime", 68);
        genreMap.put("otaku culture", 69);
        genreMap.put("performing arts", 70);
        genreMap.put("pets", 71);
        genreMap.put("reincarnation", 72);
        genreMap.put("reverse harem", 73);
        genreMap.put("romantic subtext", 74);
        genreMap.put("showbiz", 75);
        genreMap.put("survival", 76);
        genreMap.put("team sports", 77);
        genreMap.put("time travel", 78);
        genreMap.put("video game", 79);
        genreMap.put("visual arts", 80);
        genreMap.put("workplace", 48);
        genreMap.put("josei", 43);
        genreMap.put("kids", 15);
        genreMap.put("seinen", 42);
        genreMap.put("shoujo", 25);
        genreMap.put("shounen", 27);
    }

    public static List<MyAnimelistAnimeData> search(String term, boolean anime) {

        String searchUrl = "https://myanimelist.net/search/prefix.json?type=" + (anime ? "anime" : "manga") + "&keyword=" + Uri.encode(term) + "&v=1";
        List<MyAnimelistAnimeData> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(searchUrl);
            JSONObject httpContent = new JSONObject(SimpleHttpClient.getResponse(urlConnection));
            JSONArray searchResult = httpContent.getJSONArray("categories").getJSONObject(0).getJSONArray("items");
            for(int i = 0; i < searchResult.length(); i++) {
                JSONObject animeData = searchResult.getJSONObject(i);
                MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData(animeData.getInt("id"));
                myAnimelistAnimeData.setTitle(animeData.getString("name"));
                myAnimelistAnimeData.setUrl(animeData.getString("url"));
                myAnimelistAnimeData.addImage(animeData.getString("image_url"));
                myAnimelistAnimeData.setSearchScore(animeData.getDouble("es_score"));
                result.add(myAnimelistAnimeData);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
