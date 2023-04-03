package com.dhavalpateln.linkcast.extractors.mangafourlife;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.extractors.MangaExtractor;
import com.dhavalpateln.linkcast.mangascrappers.MangaScrapper;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangaFourLifeExtractor extends MangaExtractor {

    private boolean isImageURL(String url) {
        try {
            HttpURLConnection imageCheckConnection = (HttpURLConnection) (new URL(url)).openConnection();
            if(imageCheckConnection.getContentType().contains("image/")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<EpisodeNode> getChapters(String url) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            String htmlContent = SimpleHttpClient.getResponse(urlConnection);

            Document html = Jsoup.parse(htmlContent);



            EpisodeNode.EpisodeType type = EpisodeNode.EpisodeType.MANGA;

            try {
                Elements infoElements = html.select("li.list-group-item");
                for (Element infoElement : infoElements) {
                    String[] infos = infoElement.text().split(":");
                    if (infos[0].trim().equalsIgnoreCase("type")) {
                        if (!infos[1].trim().equalsIgnoreCase("manga")) {
                            type = EpisodeNode.EpisodeType.MANHWA;
                        }
                        break;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            for(String line: htmlContent.split("\n")) {
                line = line.trim();
                if(line.startsWith("vm.Chapters")) {
                    Pattern chapterListPattern = Pattern.compile("vm.Chapters = (\\[.*\\]?)");
                    Matcher matcher = chapterListPattern.matcher(line);
                    if(matcher.find()) {
                        JSONArray chapterList = new JSONArray(matcher.group(1));
                        for(int i = 0; i < chapterList.length(); i++) {
                            String chapterInfo = chapterList.getJSONObject(i).getString("Chapter");
                            String index = chapterInfo.substring(0, 1);
                            String chapterNum = chapterInfo.substring(1, chapterInfo.length() - 1).replaceAll("^0*", "");
                            if(chapterNum.equals(""))   chapterNum = "0";
                            String subNum = chapterInfo.substring(chapterInfo.length() - 1);
                            if(!subNum.equals("0")) continue;
                            String chapterURL = url.replace("/manga/", "/read-online/") + "-chapter-" + chapterNum + (subNum.equals("0") ? "" : ("." + subNum)) + (index.equals("1") ? "" : ("-index-" + index)) + ".html";
                            EpisodeNode node = new EpisodeNode(chapterNum, chapterURL);
                            node.setType(type);
                            result.add(node);
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < result.size(); i++) {
            result.get(i).setEpisodeNumString(String.valueOf(result.size() - i));
        }
        return result;
    }

    @Override
    public List<String> getPages(String url) {
        List<String> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            String lines[] = SimpleHttpClient.getResponse(urlConnection).split("\n");
            JSONObject curChapter = null;
            String curPathName = null;
            String indexName = null;
            for (String line : lines) {
                if (line.contains("<img class=\"img-fluid")) {
                    Pattern pattern = Pattern.compile(" src=\"(.*?)\"");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                }
                if (curChapter == null && line.contains("vm.CurChapter = {")) {
                    Pattern pattern = Pattern.compile("vm.CurChapter = (.*?);");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        try {
                            curChapter = new JSONObject(matcher.group(1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (curPathName == null && line.contains("vm.CurPathName = ")) {
                    Pattern pattern = Pattern.compile("vm.CurPathName = \"(.*?)\";");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        curPathName = matcher.group(1);
                    }
                }
                if (indexName == null && line.contains("vm.IndexName = ")) {
                    Pattern pattern = Pattern.compile("vm.IndexName = \"(.*?)\";");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        indexName = matcher.group(1);
                    }
                }
            }
            String chapter = curChapter.getString("Chapter").substring(1, curChapter.getString("Chapter").length() - 1);
            chapter += curChapter.getString("Chapter").charAt(curChapter.getString("Chapter").length() - 1) == '0' ? "" : ("." + curChapter.getString("Chapter").charAt(curChapter.getString("Chapter").length() - 1));
            int pages = Integer.valueOf(curChapter.getString("Page"));
            String directory = curChapter.getString("Directory");
            String imgLinkPrefix = "https://" + curPathName + "/manga/" + indexName + "/" + (directory.equals("") ? "" : (directory+'/')) + chapter + "-";//{{vm.PageImage(Page)}}.png";
            if(!isImageURL(imgLinkPrefix + "001.png")) {
                imgLinkPrefix = "https://" + "scans-hot.leanbox.us" + "/manga/" + indexName + "/" + (directory.equals("") ? "" : (directory+'/')) + chapter + "-";
            }
            if(isImageURL(imgLinkPrefix + "001.png")) {
                for (int pageNum = 1; pageNum <= pages; pageNum++) {
                    String pageString = "000" + pageNum;
                    result.add(imgLinkPrefix + pageString.substring(pageString.length() - 3) + ".png");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.MANGAFOURLIFE.URL);
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.MANGAFOURLIFE.NAME;
    }
}
