package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by AlexSt on 10/13/2015.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final int COL_HOME = 0;
    public static final int COL_HOME_GOALS = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_AWAY_GOALS = 3;
    public static final int COL_TIME = 4;
    public static final int COL_MATCH_ID = 5;

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.ScoresEntry.HOME_COL,
            DatabaseContract.ScoresEntry.HOME_GOALS_COL,
            DatabaseContract.ScoresEntry.AWAY_COL,
            DatabaseContract.ScoresEntry.AWAY_GOALS_COL,
            DatabaseContract.ScoresEntry.TIME_COL,
            DatabaseContract.ScoresEntry.MATCH_ID,
    };

    private Context mContext;
    private int mAppWidgetId;
    private int mDayIndex;
    Cursor mCursor;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mDayIndex = intent.getIntExtra(StackWidgetProvider.EXTRA_DAY_INDEX,
                PagerFragment.TODAY);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        if (mCursor != null)
            mCursor.close();
    }

    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        int matchId = ScoresProvider.INVALID_MATCH_ID;

        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_list_item);

        if (mCursor != null && mCursor.getCount() > 0 && mCursor.moveToPosition(position)){
            mCursor.moveToPosition(position);
            String pageNum = Integer.toString(position + 1);
            rv.setTextViewText(R.id.item_order_number, pageNum);
            //Call requires API level 15 (current 11)
            //rv.setContentDescription(R.id.item_order_number, "Game card number " + pageNum);
            rv.setTextViewText(R.id.home_name, mCursor.getString(COL_HOME));
            //Call requires API level 15 (current 11)
            //rv.setContentDescription(R.id.home_name, mCursor.getString(COL_HOME));
            rv.setTextViewText(R.id.away_name, mCursor.getString(COL_AWAY));
            //Call requires API level 15 (current 11)
            //rv.setContentDescription(R.id.away_name, mCursor.getString(COL_AWAY));
            String score = Utilities.getScores(mCursor.getInt(COL_HOME_GOALS),
                    mCursor.getInt(COL_AWAY_GOALS));
            rv.setTextViewText(R.id.score, score);
            //Call requires API level 15 (current 11)
            //rv.setContentDescription(R.id.score, score);
            matchId = mCursor.getInt(COL_MATCH_ID);
        }

        // Next, we set a fill-intent which will be used to fill-in the pending
        // intent template which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.EXTRA_MATCH_ID, matchId);
        extras.putInt(StackWidgetProvider.EXTRA_MATCH_INDEX, position);
        Intent intent = new Intent();
        intent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_list_item, intent);

        // You can do heaving lifting in here, synchronously. For example, if you need to process
        // an image, fetch something from the network, etc., it is ok to do it here, synchronously.
        // A loading view will show up in lieu of the actual contents in the interim.

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.)
        // If you return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager
        // notifyAppWidgetViewDataChanged on the collection view corresponding to this factory.
        // You can do heaving lifting in here, synchronously. For example, if you need to process
        // an image, fetch something from the network, etc., it is ok to do it here, synchronously.
        // The widget will remain in its current state while work is being done here, so you don't
        // need to worry about locking up the widget.

        if (mCursor != null)
            mCursor.close();

        Uri uri = DatabaseContract.ScoresEntry.buildScoreWithDate();
        String[] selectionArgs = new String[] {
                Utilities.getStringDateForQuery(mDayIndex - PagerFragment.TODAY)};
        mCursor = mContext.getContentResolver().query(uri, SCORE_COLUMNS, null, selectionArgs, null);
    }
}