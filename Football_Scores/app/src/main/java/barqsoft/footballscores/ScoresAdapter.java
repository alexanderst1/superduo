package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public double mDetailsMatchId = 0;

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder)view.getTag();
        viewHolder.textHomeName.setText(cursor.getString(ScoresDBHelper.COL_HOME));
        viewHolder.textHomeName.setContentDescription(
                context.getString(R.string.score_list_details_content_description_prefix_home_team)
                        + cursor.getString(ScoresDBHelper.COL_HOME));
        viewHolder.textAwayName.setText(cursor.getString(ScoresDBHelper.COL_AWAY));
        viewHolder.textAwayName.setContentDescription(
                context.getString(R.string.score_list_details_content_description_prefix_away_team)
                        + cursor.getString(ScoresDBHelper.COL_AWAY));
        viewHolder.textMatchTime.setText(cursor.getString(ScoresDBHelper.COL_MATCH_TIME));
        viewHolder.textMatchTime.setContentDescription(
                context.getString(R.string.score_list_details_content_description_prefix_match_time)
                        + cursor.getString(ScoresDBHelper.COL_MATCH_TIME));
        String score = Utilities.getScores(cursor.getInt(ScoresDBHelper.COL_HOME_GOALS),
                cursor.getInt(ScoresDBHelper.COL_AWAY_GOALS));
        viewHolder.textScore.setText(score);
        viewHolder.textScore.setContentDescription(
                context.getString(R.string.score_list_details_content_description_prefix_score)
                        + score);
        viewHolder.imageHomeCrest.setImageResource(Utilities
                .getTeamCrestByTeamName(context.getResources(),
                        cursor.getString(ScoresDBHelper.COL_HOME)));
        viewHolder.imageAwayCrest.setImageResource(Utilities
                .getTeamCrestByTeamName(context.getResources(),
                        cursor.getString(ScoresDBHelper.COL_AWAY)));

        viewHolder.matchId = cursor.getInt(ScoresDBHelper.COL_MATCH_ID);

        ViewGroup container = (ViewGroup)view.findViewById(R.id.details_fragment_container);
        if(viewHolder.matchId == mDetailsMatchId) {
            LayoutInflater inflater = (LayoutInflater) context.getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewDetailsFragment = inflater.inflate(R.layout.detail_fragment, null);
            container.addView(viewDetailsFragment, 0,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            TextView viewMatchDay = (TextView) viewDetailsFragment.findViewById(R.id.text_match_day);
            String matchDay = Utilities.getMatchDay(context.getResources(),
                    cursor.getInt(ScoresDBHelper.COL_MATCH_DAY),
                    cursor.getInt(ScoresDBHelper.COL_LEAGUE));
            viewMatchDay.setText(matchDay);
            viewMatchDay.setContentDescription(
                    context.getString(R.string.details_view_content_description_prefix_match_day)
                            + matchDay);
            TextView viewLeagueName = (TextView)viewDetailsFragment.findViewById(R.id.text_league_name);
            String leagueName = Utilities.getLeagueName(context.getResources(),
                    cursor.getInt(ScoresDBHelper.COL_LEAGUE));
            viewLeagueName.setText(leagueName);
            viewLeagueName.setContentDescription(
                    context.getString(R.string.details_view_content_description_prefix_league_name)
                            + leagueName);
            Button buttonShare = (Button)viewDetailsFragment.findViewById(R.id.button_share);
            final String shareText = viewHolder.textHomeName.getText()
                    + " " + viewHolder.textScore.getText() + " " + viewHolder.textAwayName.getText();
            buttonShare.setContentDescription(
                    context.getString(R.string.details_view_content_description_prefix_share_button)
                            + shareText);
            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareIntent(shareText + " " +
                            context.getResources().getString(R.string.football_scores_share_hashtag)));
                }
            });
        }
        else {
            container.removeAllViews();
        }
    }

    public Intent createShareIntent(String textToShare) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textToShare);
        return intent;
    }

}
