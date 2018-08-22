package com.example.jeko.championsportnews;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper methods related to requesting and receiving news from guardianapis.com.
 */
public final class ChampionUtils {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ChampionUtils.class.getSimpleName();
    /**
     * One year in milliseconds
     */
    private static long ONE_YEAR = 31536000000L;

    private static String API_URL = "https://content.guardianapis.com/search";
    private static String API_KEY_VALUE = "4bbe3fd4-3aee-4e00-89c3-383b49f8402b";
    private static String API_KEY = "api-key";
    private static String SECTION = "section";
    private static String TAG = "tag";
    private static String TAG_SPORT = "sport/";
    private static String Q = "q";
    private static String ORDER_BY = "order-by";
    private static String SHOW_FIELDS = "show-fields";
    private static String FROM_DATE = "from-date";
    private static String TO_DATE = "to-date";
    private static String PAGE_SIZE = "page-size";

    private static String mToDate;

    /**
     * Create a private constructor because no one should ever create a {@link ChampionUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name ChampionUtils (and an object instance of ChampionUtils is not needed).
     */
    private ChampionUtils() {
    }

    /**
     * Get the query URL string with the specified parameters
     *
     * @param section    like "sport" or "politic"
     * @param tag        for example "sport/sport" or "sport/tennis"
     * @param q          filter
     * @param orderBy    Sort by relevance, newest and oldest
     * @param showFields requests byline
     * @param fromDate   news search from this date
     * @param pageSize   Number of news in the news line
     * @return URL query in String object
     */
    public static String urlRequest(String section, String tag, String q, String orderBy, String showFields, String fromDate, String pageSize) {

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(API_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Today's date
        mToDate = formatDate(getCurrentDate());

        uriBuilder.appendQueryParameter(API_KEY, API_KEY_VALUE);
        uriBuilder.appendQueryParameter(SECTION, section);
        uriBuilder.appendQueryParameter(TAG, TAG_SPORT + tag);
        uriBuilder.appendQueryParameter(Q, q);
        uriBuilder.appendQueryParameter(ORDER_BY, orderBy);
        uriBuilder.appendQueryParameter(SHOW_FIELDS, showFields);
        uriBuilder.appendQueryParameter(FROM_DATE, fromDate);
        uriBuilder.appendQueryParameter(TO_DATE, mToDate);
        uriBuilder.appendQueryParameter(PAGE_SIZE, pageSize);

        return uriBuilder.toString();
    }

    /**
     * Return the formatted date string (i.e. "1984-03-17") from milliseconds.
     */
    private static String formatDate(long timeInMillis) {
        Date dateObject = new Date(timeInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-LL-dd");
        return dateFormat.format(dateObject);
    }

    /**
     * Return date year ago in string (i.e. "2017-06-17")
     */
    public static String getYearAgo() {
        // date in year ago in milliseconds
        long yearAgo = getCurrentDate() - ONE_YEAR;
        return formatDate(yearAgo);
    }

    /**
     * Return the current date in milliseconds
     */
    private static long getCurrentDate() {
        // Current date in milliseconds
        long currentTime = Calendar.getInstance().getTimeInMillis();
        return currentTime;
    }

    /**
     * Make a request to the TheGuardian site and parse the JSON response
     *
     * @param urlQuery URL in String object
     * @return ArrayList climber news
     */
    public static ArrayList<ChampionNews> extractNews(String urlQuery) {
        // Empty list of news
        ArrayList<ChampionNews> championNews = new ArrayList<ChampionNews>();

        String jsonString = null;
        try {
            jsonString = makeHttpRequest(createUrl(urlQuery));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject root = new JSONObject(jsonString);
            JSONObject response = root.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); ++i) {
                JSONObject news = results.getJSONObject(i);
                String section = news.getString("sectionId");
                String title = news.getString("webTitle");
                String webUrl = news.getString("webUrl");
                JSONObject fields = news.getJSONObject("fields");
                String author = fields.getString("byline");
                String date = news.getString("webPublicationDate");

                championNews.add(new ChampionNews(section, title, webUrl, author, date.substring(0, 10)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return championNews;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Create URL connection
     *
     * @param url URL object
     * @return JSON response in String object
     * @throws IOException
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
