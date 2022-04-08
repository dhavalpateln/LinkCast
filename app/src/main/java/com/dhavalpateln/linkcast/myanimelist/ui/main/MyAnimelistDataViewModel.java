package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dhavalpateln.linkcast.data.MyAnimeListCache;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class MyAnimelistDataViewModel extends ViewModel {

    private MutableLiveData<MyAnimelistAnimeData> data;

    public MyAnimelistDataViewModel() { this.data = new MutableLiveData<>(); }

    public void setData(MyAnimelistAnimeData data) {
        this.data.setValue(data);
    }

    public LiveData<MyAnimelistAnimeData> getData() {
        if(data == null) {
            data = new MutableLiveData<>();
            data.setValue(new MyAnimelistAnimeData(0));
        }
        return data;
    }

    public void loadData(MyAnimelistAnimeData myAnimelistAnimeData) {
        new ExtractInfo().execute(myAnimelistAnimeData);
    }

    private class ExtractInfo extends AsyncTask<MyAnimelistAnimeData, Void, MyAnimelistAnimeData> {

        @Override
        protected void onPostExecute(MyAnimelistAnimeData myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);
            setData(myAnimelistAnimeData);
        }

        @Override
        protected MyAnimelistAnimeData doInBackground(MyAnimelistAnimeData... myAnimelistAnimeDatas) {
            MyAnimelistAnimeData myAnimelistAnimeData = myAnimelistAnimeDatas[0];
            MyAnimelistAnimeData cache = MyAnimeListCache.getInstance().getInfo(myAnimelistAnimeData.getUrl());
            if(cache != null) {
                myAnimelistAnimeData = cache;
            }
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

                        MyAnimeListCache.getInstance().storeCache(myAnimelistAnimeData.getUrl(), myAnimelistAnimeData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            return myAnimelistAnimeData;
        }
    }
}