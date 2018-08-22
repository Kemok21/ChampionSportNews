package com.example.jeko.championsportnews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChampionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<ChampionNews>> {

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * Parameters for the API request
     */
    private static String SECTION = "sport";
    private static String Q = "champion";//%20AND%20mountain"; tag=sport%2Ftennis
    private static String SHOW_FIELDS = "byline";
    private static String PAGE_SIZE = "20";

    private ProgressBar mSpinner;
    private TextView mEmptyView;

    /**
     * Adapter for the list of news
     */
    private NewsAdapter mAdapter;

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<ChampionNews>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String fromDate = sharedPrefs.getString(
                getString(R.string.settings_from_date_key),
                ChampionUtils.getYearAgo());

        String tag = sharedPrefs.getString(
                getString(R.string.settings_tag_key),
                getString(R.string.settings_tag_default)
        );

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        // Create the URL query
        String urlQuery = ChampionUtils.urlRequest(SECTION, tag, Q, orderBy, SHOW_FIELDS, fromDate, PAGE_SIZE);
        // Create a new loader for the given URL
        return new NewsLoader(ChampionActivity.this, urlQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<ChampionNews>> loader, List<ChampionNews> championNews) {
        mEmptyView.setText(R.string.no_news);
        mSpinner.setVisibility(View.GONE);
        // Clear the adapter of previous earthquake data
        mAdapter.clear();
        // If there is a valid list of {@link ChampionNews}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (championNews != null && !championNews.isEmpty()) {
            mAdapter.addAll(championNews);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ChampionNews>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champion);

        // Checking the Internet connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        mSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);
        mEmptyView = (TextView) findViewById(R.id.empty);
        newsListView.setEmptyView(mEmptyView);

        // Create a new adapter that takes an empty list of news as input
        mAdapter = new NewsAdapter(this, new ArrayList<ChampionNews>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mAdapter.getItem(position).getUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            mSpinner.setVisibility(View.GONE);
            mEmptyView.setText(R.string.no_internet);
        }
    }
    // This method initialize the contents of the Activity's options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    // This method is called whenever an item in the options menu is selected.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}