package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {
    public static final int BEFORE_YESTERDAY = 0;
    public static final int YESTERDAY = 1;
    public static final int TODAY = 2;
    public static final int TOMORROW = 3;
    public static final int AFTER_TOMORROW = 4;
    public static final int NUM_PAGES = 5;

    public ViewPager mPagerHandler;
    private ScoresPageAdapter mPagerAdapter;
    private MainScreenFragment[] pages = new MainScreenFragment[NUM_PAGES];
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new ScoresPageAdapter(getChildFragmentManager());
        for (int dayIndex = 0;dayIndex < NUM_PAGES;dayIndex++) {
            String date  = Utilities.getStringDateForQuery(dayIndex - TODAY);
            pages[dayIndex] = new MainScreenFragment();
            pages[dayIndex].setFragmentDate(date);
        }
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.mCurrentPageIndex);
        pages[MainActivity.mCurrentPageIndex].mPosition = MainActivity.mCurrentMatchIndex;
        return rootView;
    }
    private class ScoresPageAdapter extends FragmentStatePagerAdapter {
        @Override
        public Fragment getItem(int i)
        {
            return pages[i];
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public ScoresPageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(getActivity(), Utilities.getDateInMsec(position - TODAY));
        }

        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            String dayName;
            Time time = new Time();
            time.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.day_name_today);
            } else if (julianDay == currentJulianDay + 1) {
                dayName = context.getString(R.string.day_name_tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                dayName = context.getString(R.string.day_name_yesterday);
            } else {
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                dayName = dayFormat.format(dateInMillis);
            }
            return dayName;
        }
    }
}
