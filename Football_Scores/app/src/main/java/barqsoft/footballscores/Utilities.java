package barqsoft.footballscores;

import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {

    public static final int LEAGUE_ID_SERIE_A = 357;
    public static final int LEAGUE_ID_PREMIER = 354;
    public static final int LEAGUE_ID_CHAMPIONS = 362;
    public static final int LEAGUE_ID_PRIMERA_DIVISION = 358;
    public static final int LEAGUE_ID_BUNDESLIGA = 351;

    public static String getLeagueName(Resources res, int leagueId) {
        switch (leagueId) {
            case LEAGUE_ID_SERIE_A:
                return res.getString(R.string.league_name_seriaa);
            case LEAGUE_ID_PREMIER:
                return res.getString(R.string.league_name_premierleague);
            case LEAGUE_ID_CHAMPIONS:
                return res.getString(R.string.league_name_champions_league);
            case LEAGUE_ID_PRIMERA_DIVISION:
                return res.getString(R.string.league_name_primeradivison);
            case LEAGUE_ID_BUNDESLIGA:
                return res.getString(R.string.league_name_bundesliga);
            default:
                return res.getString(R.string.league_name_unknown);
        }
    }
    public static String getMatchDay(Resources res, int matchDayCount, int leagueId) {
        if(leagueId == LEAGUE_ID_CHAMPIONS) {
            if (matchDayCount <= 6) {
                return res.getString(R.string.match_day_category_up_to_6);
            } else if(matchDayCount == 7 || matchDayCount == 8) {
                return res.getString(R.string.match_day_category_7_or_8);
            } else if(matchDayCount == 9 || matchDayCount == 10) {
                return res.getString(R.string.match_day_category_9_or_10);
            } else if(matchDayCount == 11 || matchDayCount == 12) {
                return res.getString(R.string.match_day_category_11_or_12);
            } else {
                return res.getString(R.string.match_day_category_more_than_12);
            }
        } else {
            return res.getString(R.string.match_day_category_prefix) + " : " +
                    String.valueOf(matchDayCount);
        }
    }

    public static String getScores(int homeGoals,int awayGoals) {
        if(homeGoals < 0 || awayGoals < 0) {
            return " - ";
        } else {
            return String.valueOf(homeGoals) + " - " + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (Resources res, String teamName) {
        //This is the set of icons that are currently in the app. Feel free to find and add more
        //as you go.
        if (teamName == null)
            return R.drawable.no_icon;
        else if (teamName == res.getString(R.string.team_name_crest_arsenal))
            return R.drawable.arsenal;
        else if (teamName == res.getString(R.string.team_name_crest_manchester))
            return R.drawable.manchester_united;
        else if (teamName == res.getString(R.string.team_name_crest_swansea))
            return R.drawable.swansea_city_afc;
        else if (teamName == res.getString(R.string.team_name_crest_leicester))
            return R.drawable.leicester_city_fc_hd_logo;
        else if (teamName == res.getString(R.string.team_name_crest_everton))
            return R.drawable.everton_fc_logo1;
        else if (teamName == res.getString(R.string.team_name_crest_westham))
            return R.drawable.west_ham;
        else if (teamName == res.getString(R.string.team_name_crest_tottenham))
            return R.drawable.tottenham_hotspur;
        else if (teamName == res.getString(R.string.team_name_crest_west_bromwich))
            return R.drawable.west_bromwich_albion_hd_logo;
        else if (teamName == res.getString(R.string.team_name_crest_sunderland))
            return R.drawable.sunderland;
        else if (teamName == res.getString(R.string.team_name_crest_stoke_city))
            return R.drawable.stoke_city;
        else
            return R.drawable.no_icon;
    }

    public static final int DAY_DURATION_MSEC = 24 * 3600 * 1000;

    public static long getDateInMsec(int offsetFromToday) {
        return System.currentTimeMillis()+(offsetFromToday * DAY_DURATION_MSEC);
    }

    public static String getStringDateForQuery(int offsetFromToday) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date(getDateInMsec(offsetFromToday)));
    }
}
