package com.dhavalpateln.linkcast.database;

import com.dhavalpateln.linkcast.data.MyAnimeListCache;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MyAnimeListDatabase {
    private static MyAnimeListDatabase myAnimeListDatabase;

    public enum TopAnimeType {
        TOP_AIRING,
        TOP_UPCOMING,
        TOP_TV_SERIES,
        TOP_MOVIES,
        TOP_POPULAR,
        TOP_FAVORITE
    }

    private MyAnimeListDatabase() {

    }

    public static MyAnimeListDatabase getInstance() {
        if(myAnimeListDatabase == null) myAnimeListDatabase = new MyAnimeListDatabase();
        return myAnimeListDatabase;
    }

    public List<MyAnimelistAnimeData> getTopAnime(TopAnimeType type, int limit) {
        return null;
    }

    public MyAnimelistAnimeData fetchAnimeInfo(MyAnimelistAnimeData myAnimelistAnimeData) {
        MyAnimelistAnimeData cache = MyAnimeListCache.getInstance().getInfo(myAnimelistAnimeData.getUrl());
        if(cache != null) return cache;
        else {
            if (myAnimelistAnimeData.getUrl() != null) {
                try {
                    HttpURLConnection httpURLConnection = SimpleHttpClient.getURLConnection(myAnimelistAnimeData.getUrl());
                    SimpleHttpClient.setBrowserUserAgent(httpURLConnection);
                    Document html = Jsoup.parse(SimpleHttpClient.getResponse(httpURLConnection));

                    Element titleElement = html.selectFirst("div[itemprop=name]");
                    myAnimelistAnimeData.setTitle(titleElement.selectFirst("h1").text());

                    // GET INFOS
                    Elements infoElements = html.select("div.spaceit_pad");
                    for (Element infoElement : infoElements) {
                        try {
                            String infoKey = infoElement.getElementsByTag("span").get(0).text();
                            String infoValue = infoElement.text().replace(infoKey, "");
                            infoKey = infoKey.replace(":", "");
                            if(infoValue.startsWith("N/A")) infoValue = "N/A";
                            switch (infoKey) {
                                case "Score":
                                    infoValue = infoElement.selectFirst("span.score-label").text();
                                    //infoValue = String.format("%.2f", Double.valueOf(infoValue.trim().split(" ")[0]));
                                    break;
                                case "Ranked":
                                    infoValue = html.selectFirst("span.numbers.ranked").selectFirst("strong").text();
                                case "Popularity":
                                    infoValue = infoValue.trim().split(" ")[0];
                                    break;
                                default:
                                    break;
                            }
                            myAnimelistAnimeData.putInfo(infoKey, infoValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // GET SYNOPSIS
                    try {
                        Elements sysnopsisElements = html.select("p[itemprop=description]");
                        myAnimelistAnimeData.setSynopsis(sysnopsisElements.get(0).text());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // GET IMAGE
                    try {
                        Elements imageElements = html.select("img[itemprop=image]");
                        myAnimelistAnimeData.addImage(imageElements.get(0).attr("data-src"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // GET RELATED ANIMES
                    try {
                        Elements relatedAnimeElements = html.selectFirst("table.anime_detail_related_anime").select("tr");
                        for (Element relatedAnimeElement : relatedAnimeElements) {
                            String relatedType = relatedAnimeElement.selectFirst("td").text().split(":")[0];
                            Element link = relatedAnimeElement.selectFirst("a");
                            MyAnimelistAnimeData data = new MyAnimelistAnimeData();
                            data.setUrl(link.attr("href"));
                            data.setTitle(link.text());
                            switch (relatedType) {
                                case "Prequel":
                                    myAnimelistAnimeData.addPrequel(data);
                                    break;
                                case "Sequel":
                                    myAnimelistAnimeData.addSequel(data);
                                    break;
                                case "Side story":
                                    myAnimelistAnimeData.addSideStory(data);
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Get Characters
                    try {
                        Elements characterElements = html.selectFirst("div.detail-characters-list").select("tr");
                        for(Element characterElement: characterElements) {
                            try {
                                MyAnimelistCharacterData characterData = new MyAnimelistCharacterData();
                                Elements infoTdElements = characterElement.select("td");

                                Element nameElement = infoTdElements.get(1).selectFirst("a");
                                characterData.setName(nameElement.text());
                                characterData.setUrl(nameElement.attr("href"));
                                characterData.setType(infoTdElements.get(1).selectFirst("small").text());

                                String imageUrl = infoTdElements.get(0).selectFirst("img").attr("data-src");
                                imageUrl = imageUrl.replaceAll("r/\\d+x\\d+/", "");
                                characterData.addImage(imageUrl);

                                myAnimelistAnimeData.addCharacter(characterData);
                            } catch (Exception e) {e.printStackTrace();}
                        }
                    } catch (Exception e) {e.printStackTrace();}

                    MyAnimeListCache.getInstance().storeAnimeCache(myAnimelistAnimeData.getUrl(), myAnimelistAnimeData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return myAnimelistAnimeData;
    }

    private List<MyAnimelistCharacterData> getAllCharacters(String url) {
        List<MyAnimelistCharacterData> result = MyAnimeListCache.getInstance().getAnimeCharacterList(url);
        if(result != null)  return result;
        try {
            result = new ArrayList<>();
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Elements characterTables = html.select("table.js-anime-character-table");
            for(Element characterTable: characterTables) {
                try {
                    Elements infoTDs = characterTable.select("td");
                    Element imgTD = infoTDs.get(0);
                    Element nameTD = infoTDs.get(1);
                    MyAnimelistCharacterData character = new MyAnimelistCharacterData();

                    String imageUrl = imgTD.selectFirst("img").attr("data-src");
                    imageUrl = imageUrl.replaceAll("r/\\d+x\\d+/", "");
                    character.addImage(imageUrl);

                    Element nameElement = nameTD.selectFirst("a");
                    character.setName(nameElement.text().trim());
                    character.setUrl(nameElement.attr("href"));
                    character.setType(nameTD.select("div.spaceit_pad").get(1).text().trim());
                    result.add(character);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(result.size() > 0)
                MyAnimeListCache.getInstance().storeCharacterCache(url, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<MyAnimelistAnimeData> getRecommendations(String url) {
        List<MyAnimelistAnimeData> result = MyAnimeListCache.getInstance().getQueryResult(url);
        if(result != null)  return result;
        try {
            result = new ArrayList<>();
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Element recommendationContainer = html.selectFirst("div.rightside");
            Elements recommendationElements = recommendationContainer.select("div.borderClass");
            for(Element recommendationElement: recommendationElements) {
                try {
                    Elements infoTDs = recommendationElement.select("td");
                    Element imgElement = infoTDs.get(0).selectFirst("img");
                    Element linkElement = infoTDs.get(1).selectFirst("a");
                    MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();

                    String imageURL = imgElement.attr("data-src").replaceAll("r/\\d+x\\d+/", "");
                    myAnimelistAnimeData.addImage(imageURL);

                    myAnimelistAnimeData.setUrl(linkElement.attr("href"));
                    myAnimelistAnimeData.setTitle(linkElement.text().trim());

                    result.add(myAnimelistAnimeData);
                } catch (Exception e){e.printStackTrace();}
            }
            if(result.size() > 0)   MyAnimeListCache.getInstance().storeAnimeCache(url, result);
        } catch (Exception e) {e.printStackTrace();}
        return result;
    }

    public void getAllCharacters(MyAnimelistAnimeData myAnimelistAnimeData) {
        List<MyAnimelistCharacterData> characterList = getAllCharacters(myAnimelistAnimeData.getUrl() + "/characters");
        for(MyAnimelistCharacterData characterData: characterList)
            myAnimelistAnimeData.addCharacter(characterData);
    }

    public void getRecommendations(MyAnimelistAnimeData myAnimelistAnimeData) {
        List<MyAnimelistAnimeData> animeList = getRecommendations(myAnimelistAnimeData.getUrl() + "/userrecs");
        for(MyAnimelistAnimeData animeData: animeList)
            myAnimelistAnimeData.addRecommendation(animeData);
    }
}
