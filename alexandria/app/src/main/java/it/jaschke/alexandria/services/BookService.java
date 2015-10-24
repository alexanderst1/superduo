package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {

        if(ean.length()!=13){
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry.getCount() > 0){
            bookEntry.close();
            return;
        }

        bookEntry.close();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        boolean exceptionHappenedInWebRequest = false;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            exceptionHappenedInWebRequest = true;
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        //AlexSt
        if (exceptionHappenedInWebRequest || bookJsonString == null) {
            showMessageCouldNotGetBookInfo();
            return;
        }

        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        //AlexSt: refactored code below to separate code which may throw JSON
        //exception from code which puts data into database to update database
        //only when JSON code completed with no exceptions.
        //Also added check for null strings to replace them with empty string
        //before putting into database. Otherwise null-strings check would be needed
        //in every place in code where strings are retrieved from database and
        //Strings methods are attempted to be executed on them.

        String title = null;
        String subtitle = null;
        String desc = null;
        String imgUrl = null;
        List<String> authors = null;
        List<String> categories = null;

        boolean jsonExceptionHappened = false;

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            if(!bookJson.has(ITEMS)) {
                showMessageNoBookFound();
                return;
            }

            JSONArray bookArray = bookJson.getJSONArray(ITEMS);
            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            title = bookInfo.getString(TITLE);
            if (title == null || title.length() == 0) {
                showMessageNoBookFound();
                return;
            }

            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }
            if(bookInfo.has(AUTHORS)) {
                authors = getListFromJSONArray((JSONArray)bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                categories = getListFromJSONArray((JSONArray)bookInfo.getJSONArray(CATEGORIES));
            }

        } catch (JSONException e) {
            jsonExceptionHappened = true;
            Log.e(LOG_TAG, "Error ", e);
        }

        if (jsonExceptionHappened) {
            showMessageNoBookFound();
            return;
        }

        //AlexSt: check for null-strings and convert them into empty strings before putting
        //into database to avoid null-reference exception on executing string methods on nulls
        if (subtitle == null) subtitle = "";
        if (desc == null) desc = "";
        if (imgUrl == null) imgUrl = "";

        writeBackBook(ean, title, subtitle, desc, imgUrl);
        if (authors != null && authors.size() > 0)
            writeBackAuthors(ean, authors);
        if (categories != null && categories.size() > 0)
            writeBackCategories(ean, categories);
    }

    private List<String> getListFromJSONArray(JSONArray array) throws JSONException
    {
        if (array == null || array.length() == 0)
            return null;
        List<String> list = new ArrayList<String>();
        for (int i=0; i < array.length(); i++){
            String string = array.get(i).toString();
            //AlexSt: check for null-string and convert it into empty strings before putting
            //into database to avoid null-reference exception on executing string methods on nulls
            if (string == null) string = "";
            list.add(string);
        }
        return list;
    }

    private void showMessage(int resId) {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(resId));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    private void showMessageNoBookFound() {
        showMessage(R.string.not_found);
    }

    private void showMessageCouldNotGetBookInfo() {
        showMessage(R.string.could_not_get_book_info);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, List<String> authors) {
        ContentValues values;
        ContentResolver cr = getContentResolver();
        for (int i = 0; i < authors.size(); i++) {
            values = new ContentValues();
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            String author = authors.get(i);
            //AlexSt: avoid null-reference exception after retrieving null-strings from database and
            //trying to execute any methods on them
            if (author == null) author = "";
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
            cr.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
        }
    }

    private void writeBackCategories(String ean,  List<String> categories) {
        ContentValues values;
        for (int i = 0; i < categories.size(); i++) {
            values = new ContentValues();
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            //AlexSt: avoid null-reference exception after retrieving null-strings from database and
            //trying to execute any methods on them
            String category = categories.get(i);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
        }
    }
 }