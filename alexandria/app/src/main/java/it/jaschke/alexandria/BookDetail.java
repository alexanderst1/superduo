package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    public static final String BACK_BUTTON_VISIBLE = "BACK_BUTTON_VISIBLE";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;
    //AlexSt: Have shareIntent as a member make it available in function onCreateOptionsMenu
    //to be set in shareActionProvider
    private Intent shareIntent = new Intent(Intent.ACTION_SEND);

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        boolean backButtonVisible = true;

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            backButtonVisible = arguments.getBoolean(BACK_BUTTON_VISIBLE, true);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        rootView.findViewById(R.id.backButton)
                .setVisibility(backButtonVisible ? View.VISIBLE : View.GONE);
        return rootView;
    }


    private Menu menu = null;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);
        this.menu = menu;
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        //AlexSt: set share intent in share action provider so that it can be sent when user
        //presses button "Share"
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onDestroy()
    {
        if (menu != null && menu.findItem(R.id.action_share) != null)
            menu.removeItem(R.id.action_share);
        super.onDestroy();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        // AlexSt: fixed null-reference exception in shareActionProvider.setShareIntent
        // as shareActionProvider might be null if onLoadFinished is executed before
        // onCreateOptionsMenu where shareActionProvider is initialized.
        // "java.lang.NullPointerException: Attempt to invoke virtual method
        // 'void android.support.v7.widget.ShareActionProvider.setShareIntent(android.content.Intent)'
        // on a null object reference"
        if (shareIntent.hasExtra(Intent.EXTRA_TEXT))
            shareIntent.removeExtra(Intent.EXTRA_TEXT);
        //AlexSt: inserted space between colon and book title
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + bookTitle);
        //shareActionProvider.setShareIntent(shareIntent);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        // AlexSt: may return null, fixed exception in String.split
        // "java.lang.NullPointerException: Attempt to invoke virtual method
        // 'java.lang.String[] java.lang.String.split(java.lang.String)' on a null object reference"
        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        int numLines = 0;
        if (authors != null) {
            String[] arr = authors.split(",");
            if (arr != null) numLines = arr.length;
            authors = authors.replace(",","\n");
        }
        ((TextView) rootView.findViewById(R.id.authors)).setLines(numLines);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors);

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        if(rootView.findViewById(R.id.right_container)!=null){
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}