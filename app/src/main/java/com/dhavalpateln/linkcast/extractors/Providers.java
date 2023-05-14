package com.dhavalpateln.linkcast.extractors;

import android.content.Context;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.extractors.animepahe.AnimePaheExtractor;
import com.dhavalpateln.linkcast.extractors.animepahe.AnimePaheNavigator;
import com.dhavalpateln.linkcast.extractors.animepahe.AnimePaheSearch;
import com.dhavalpateln.linkcast.extractors.gogoanime.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.extractors.gogoanime.GogoAnimeSearch;
import com.dhavalpateln.linkcast.extractors.mangafourlife.MangaFourLifeExtractor;
import com.dhavalpateln.linkcast.extractors.mangafourlife.MangaFourLifeSearch;
import com.dhavalpateln.linkcast.extractors.mangareader.MangaReaderExtractor;
import com.dhavalpateln.linkcast.extractors.mangareader.MangaReaderSearch;
import com.dhavalpateln.linkcast.extractors.marin.MarinExtractor;
import com.dhavalpateln.linkcast.extractors.marin.MarinSearch;
import com.dhavalpateln.linkcast.extractors.streamsb.StreamSBNavigator;
import com.dhavalpateln.linkcast.extractors.zoro.ZoroExtractor;
import com.dhavalpateln.linkcast.extractors.zoro.ZoroSearch;

import java.util.HashMap;
import java.util.Map;

public class Providers {

    private static Providers instance;
    private Map<String, AnimeExtractor> cachedAnimeExtractor;
    private Map<String, MangaExtractor> cachedMangaExtractor;

    private Providers() {
        cachedAnimeExtractor = new HashMap<>();
        cachedMangaExtractor = new HashMap<>();
    }

    public static Providers getInstance() {
        if(instance == null) {
            instance = new Providers();
        }
        return instance;
    }

    public static Map<String, AnimeExtractor> getAnimeExtractors() {
        Map<String, AnimeExtractor> extractors = new HashMap<>();
        extractors.put(ProvidersData.MARIN.NAME, new MarinExtractor());
        extractors.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeExtractor());
        extractors.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheExtractor());
        extractors.put(ProvidersData.ZORO.NAME, new ZoroExtractor());
        return extractors;
    }

    public static Map<String, MangaExtractor> getMangaExtractors() {
        Map<String, MangaExtractor> extractors = new HashMap<>();
        extractors.put(ProvidersData.MANGAFOURLIFE.NAME, new MangaFourLifeExtractor());
        extractors.put(ProvidersData.MANGAREADER.NAME, new MangaReaderExtractor());
        return extractors;
    }

    public static Map<String, Extractor> getExtractors() {
        Map<String, Extractor> extractors = new HashMap<>();
        extractors.put(ProvidersData.MARIN.NAME, new MarinExtractor());
        extractors.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeExtractor());
        extractors.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheExtractor());
        extractors.put(ProvidersData.ZORO.NAME, new ZoroExtractor());
        extractors.put(ProvidersData.MANGAFOURLIFE.NAME, new MangaFourLifeExtractor());
        extractors.put(ProvidersData.MANGAREADER.NAME, new MangaReaderExtractor());
        return extractors;
    }

    public static Map<String, AnimeMangaSearch> getSearchers() {
        Map<String, AnimeMangaSearch> searchers = new HashMap<>();
        searchers.put(ProvidersData.MARIN.NAME, new MarinSearch());
        searchers.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeSearch());
        searchers.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheSearch());
        searchers.put(ProvidersData.ZORO.NAME, new ZoroSearch());
        searchers.put(ProvidersData.MARIN.NAME, new MarinSearch());
        searchers.put("manga4life", new MangaFourLifeSearch());
        searchers.put(ProvidersData.MANGAREADER.NAME, new MangaReaderSearch());
        return searchers;
    }

    public static String[] getSearchOrder() {
        return new String[] {
                ProvidersData.GOGOANIME.NAME,
                ProvidersData.ZORO.NAME,
                ProvidersData.ANIMEPAHE.NAME,
                ProvidersData.MARIN.NAME,
                ProvidersData.MANGAFOURLIFE.NAME,
                ProvidersData.MANGAREADER.NAME
        };
    }

    public static Map<String, SourceNavigator> getNavigators() {
        Map<String, SourceNavigator> navigators = new HashMap<>();
        navigators.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheNavigator());
        navigators.put(ProvidersData.STREAMSB.NAME, new StreamSBNavigator());
        return navigators;
    }
}
