package com.dhavalpateln.linkcast.animesearch;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeKisaSearch extends AnimeSearch {
    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        String searchUrl = "https://animekisa.tv/search?q=" + URLEncoder.encode(term);
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String htmlContent = getHttpContent(searchUrl);
            String title = null;
            String poster = null;
            String url = null;
            Pattern urlPattern = Pattern.compile("<a class=\"an\" href=\"(.*?)\">");
            Pattern posterPattern = Pattern.compile("<img class=\"coveri\" .* src=\"(.*?)\"");
            Pattern titlePattern = Pattern.compile("<div class=\"similardd\">(.*?)</div>");
            int infosFound = 0;

            for(String line: htmlContent.split("\n")) {
                line = line.trim();
                if(line.startsWith("<a class=\"an\" href=\"")) {
                    Matcher matcher = urlPattern.matcher(line);
                    if (matcher.find()) {
                        url = "https://animekisa.tv" + matcher.group(1);
                    }
                }
                else if(line.startsWith("<div class=\"similarpic\">")) {
                    Matcher matcher = posterPattern.matcher(line);
                    if (matcher.find()) {
                        poster = "https://animekisa.tv" + matcher.group(1);
                    }
                }
                else if(line.startsWith("<div class=\"similardd\">")) {
                    Matcher matcher = titlePattern.matcher(line);
                    if (matcher.find()) {
                        title = matcher.group(1);
                    }

                    if(title != null && poster != null && url != null) {
                        AnimeLinkData animeLinkData = new AnimeLinkData();
                        Map<String, String> data = new HashMap<>();
                        data.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");
                        data.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, poster);

                        animeLinkData.setTitle(title);
                        animeLinkData.setUrl(url);
                        animeLinkData.setData(data);

                        result.add(animeLinkData);

                        title = null;
                        poster = null;
                        url = null;
                    }
                }



            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return "animekisa.tv";
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
