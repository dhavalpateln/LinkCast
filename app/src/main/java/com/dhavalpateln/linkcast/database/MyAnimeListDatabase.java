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

    public class VideoData {
        String url;
        String title;
        String imageURL;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }
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

    public List<VideoData> fetchVideos(MyAnimelistAnimeData myAnimelistAnimeData) {
        List<VideoData> result = new ArrayList<>();
        String url = myAnimelistAnimeData.getUrl() + "/video";
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Element promotionalVideoDiv = html.selectFirst("div.video-block.promotional-video");
            Elements videoElements = promotionalVideoDiv.select("div.video-list-outer");
            for(Element videoElement: videoElements) {
                VideoData videoData = new VideoData();
                videoData.setImageURL(videoElement.selectFirst("img").attr("data-src"));
                videoData.setUrl(videoElement.selectFirst("a").attr("href"));
                videoData.setTitle(videoElement.selectFirst("span.title").text().trim());
                result.add(videoData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public MyAnimelistCharacterData getCharacterData(MyAnimelistCharacterData data) {
        MyAnimelistCharacterData result = MyAnimeListCache.getInstance().getCharacterData(data.getUrl());
        if(result == null) {
            result = new MyAnimelistCharacterData();
            result.setUrl(data.getUrl());
            JikanDatabase.getInstance().getCharacterData(result);
            String picsUrl = data.getUrl() + "/pics";
            try {
                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(picsUrl);
                SimpleHttpClient.setBrowserUserAgent(urlConnection);
                Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
                Element contentDiv = html.selectFirst("div#content");
                Element table = contentDiv.selectFirst("table");
                Element picsCol = html.selectFirst("div#content").selectFirst("table").selectFirst("tr").child(1);
                Elements picElements = picsCol.select("div.picSurround");
                for(Element picElement: picElements) {
                    result.addImage(picElement.selectFirst("img").attr("data-src"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    public List<EpisodeNode> getEpisodeTitles(int page, String baseURL) {
        String url = baseURL + "/episode?offset=" + (page * 100);

        List<EpisodeNode> episodeNodes = MyAnimeListCache.getInstance().getEpisodeNodes(url);
        if(episodeNodes != null) return episodeNodes;
        episodeNodes = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));

            Elements episodeElements = html.selectFirst("table.episode_list").select("tr.episode-list-data");

            for(Element episodeElement: episodeElements) {
                EpisodeNode node = new EpisodeNode(episodeElement.selectFirst("td.episode-number").text(), null);
                node.setTitle(episodeElement.selectFirst("td.episode-title").selectFirst("a").text());
                node.setFiller(episodeElement.text().contains("Filler"));
                episodeNodes.add(node);
            }
            MyAnimeListCache.getInstance().storeEpisodeNodesCache(url, episodeNodes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return episodeNodes;
    }
}
