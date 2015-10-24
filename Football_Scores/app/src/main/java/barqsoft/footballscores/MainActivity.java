package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
    public static String LOG_TAG = "MainActivity";

    public static int mCurrentMatchId = ScoresProvider.INVALID_MATCH_ID;
    public static int mCurrentMatchIndex = ListView.INVALID_POSITION;
    public static int mCurrentPageIndex = PagerFragment.TODAY;

    public static final String ACTION_SHOW_GAME_DETAILS =
            "barqsoft.footballscores.action.SHOW_GAME_DETAILS";

    private PagerFragment mPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            //AlexSt: handle intent from widget
            Intent intent = getIntent();
            if (intent != null && intent.getAction() == ACTION_SHOW_GAME_DETAILS) {
                mCurrentPageIndex = intent.getIntExtra(StackWidgetProvider.EXTRA_DAY_INDEX,
                        PagerFragment.TODAY);
                mCurrentMatchId = intent.getIntExtra(StackWidgetProvider.EXTRA_MATCH_ID,
                        ScoresProvider.INVALID_MATCH_ID);
                mCurrentMatchIndex = intent.getIntExtra(StackWidgetProvider.EXTRA_MATCH_INDEX,
                        ListView.INVALID_POSITION);
            }

            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPagerFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt("mCurrentPageIndex", mPagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt("mCurrentMatchId", mCurrentMatchId);
        getSupportFragmentManager().putFragment(outState, "mPagerFragment", mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        mCurrentPageIndex = savedInstanceState.getInt("mCurrentPageIndex");
        mCurrentMatchId = savedInstanceState.getInt("mCurrentMatchId");
        mPagerFragment = (PagerFragment)getSupportFragmentManager().getFragment(savedInstanceState, "mPagerFragment");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
