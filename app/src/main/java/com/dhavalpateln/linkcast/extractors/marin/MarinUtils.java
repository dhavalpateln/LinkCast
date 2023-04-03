package com.dhavalpateln.linkcast.extractors.marin;

import static java.net.URLDecoder.decode;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarinUtils {

    private static MarinUtils utils = null;

    private String xsrf = null;
    private String cookie = null;
    private String ddosCookie = ";__ddg1_=;__ddg2_=;";

    private MarinUtils() {}

    public static MarinUtils getInstance() {
        if(utils == null) {
            utils = new MarinUtils();
        }
        return utils;
    }

    public Map<String, String> getCookies() throws IOException {
        Map<String, String> result = new HashMap<>();
        if(cookie == null) {
            SimpleHttpClient.bypassDDOS(ProvidersData.MARIN.URL);
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(ProvidersData.MARIN.URL);
            SimpleHttpClient.getResponse(urlConnection);
            List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
            cookie = String.join(";", cookieList);
            cookie += ddosCookie;
            for(String cookieValue: cookieList) {
                if(cookieValue.startsWith("XSRF-TOKEN")) {
                    xsrf = decode(cookieValue.substring(cookieValue.indexOf("=") + 1, cookieValue.indexOf(";")));
                    cookie += "cutemarinmoe_session=" + xsrf;
                }
            }
        }
        result.put("cookie", cookie);
        result.put("x-xsrf-token", xsrf);
        return result;
    }
}
