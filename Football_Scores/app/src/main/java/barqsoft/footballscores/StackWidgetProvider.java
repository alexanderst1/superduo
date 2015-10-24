package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AlexSt on 10/13/2015.
 */
public class StackWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_SWITCH_TO_ANOTHER_DAY =
            "barqsoft.footballscores.ACTION_ANOTHER_DAY";
    public static final String EXTRA_DAY_INDEX = "barqsoft.footballscores.EXTRA_DAY_INDEX";
    public static final String EXTRA_MATCH_ID = "barqsoft.footballscores.EXTRA_MATCH_ID";
    public static final String EXTRA_MATCH_INDEX = "barqsoft.footballscores.EXTRA_MATCH_INDEX";

    int mDayIndex = PagerFragment.TODAY;

    public static final SparseIntArray DAY_INDEX_TO_BUTTON_ID_MAP =
            new SparseIntArray() {{
                put(PagerFragment.TODAY, R.id.buttonToday);
                put(PagerFragment.YESTERDAY, R.id.buttonYesterday);
                put(PagerFragment.BEFORE_YESTERDAY, R.id.buttonBeforeYesterday);
                put(PagerFragment.TOMORROW, R.id.buttonTomorrow);
                put(PagerFragment.AFTER_TOMORROW, R.id.buttonAfterTomorrow);
        }};

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SWITCH_TO_ANOTHER_DAY)) {
            int appWidgetId = intent.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            mDayIndex = intent.getIntExtra(EXTRA_DAY_INDEX, PagerFragment.TODAY);
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            onUpdate(context, mgr, new int[] {appWidgetId});
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            setUpRemoteCollectionView(context, rv, appWidgetIds[i]);
            highlightClickedButton(rv);

            setDayButtonsOnClickIntent(context, rv, appWidgetIds[i]);
            setCollectionItemOnClickIntent(context, rv, appWidgetIds[i]);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.stack_view);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void highlightClickedButton(RemoteViews widgetLayoutRemoteView) {
        for (int i = 0; i < DAY_INDEX_TO_BUTTON_ID_MAP.size(); i++) {
            int dayIndex = DAY_INDEX_TO_BUTTON_ID_MAP.keyAt(i);
            int buttonId = DAY_INDEX_TO_BUTTON_ID_MAP.valueAt(i);
            widgetLayoutRemoteView.setInt(buttonId, "setBackgroundResource", //API function name
                    dayIndex == mDayIndex ?
                    R.color.widget_button_background_clicked : R.color.widget_button_background);
        }
    }

    private void setUpRemoteCollectionView(Context context, RemoteViews widgetLayoutRemoteView,
                                           int widgetId) {
        // Here we setup the intent which points to the StackViewService
        // which will provide the views for this collection.
        Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        //Provide information matches of which day to show
        intent.putExtra(EXTRA_DAY_INDEX, mDayIndex);
        // When intents are compared, the extras are ignored, so we need to
        // embed the extras into the data so that the extras will not be ignored.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        widgetLayoutRemoteView.setRemoteAdapter(widgetId, R.id.stack_view, intent);

        // The empty view is displayed when the collection has no items. It
        // should be a sibling of the collection view.
        widgetLayoutRemoteView.setEmptyView(R.id.stack_view, R.id.empty_view);
    }

    private void setCollectionItemOnClickIntent(Context context, RemoteViews widgetLayoutRemoteView,
                                                int widgetId) {
        // Here we setup the a pending intent template. Individuals items of
        // a collection cannot setup their own pending intents, instead, the collection
        // as a whole can setup a pending intent template, and the individual items can set
        // a fillInIntent to create unique before on an item to item basis.
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(MainActivity.ACTION_SHOW_GAME_DETAILS);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                widgetId);
        intent.putExtra(EXTRA_DAY_INDEX, mDayIndex);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetLayoutRemoteView.setPendingIntentTemplate(R.id.stack_view, pendingIntent);
    }

    private void setDayButtonsOnClickIntent(Context context, RemoteViews rv, int widgetId) {
        for (int i = 0; i < DAY_INDEX_TO_BUTTON_ID_MAP.size(); i++) {
            int dayIndex = DAY_INDEX_TO_BUTTON_ID_MAP.keyAt(i);
            int buttonId = DAY_INDEX_TO_BUTTON_ID_MAP.valueAt(i);
            Intent intent = new Intent(context, StackWidgetProvider.class);
            intent.setAction(ACTION_SWITCH_TO_ANOTHER_DAY);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(EXTRA_DAY_INDEX, dayIndex);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(buttonId, pendingIntent);
        }
    }

}
