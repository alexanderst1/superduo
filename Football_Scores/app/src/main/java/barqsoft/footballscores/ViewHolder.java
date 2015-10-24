package barqsoft.footballscores;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ViewHolder {
    public TextView textHomeName;
    public TextView textAwayName;
    public TextView textScore;
    public TextView textMatchTime;
    public ImageView imageHomeCrest;
    public ImageView imageAwayCrest;
    public int matchId;
    public ViewHolder(View view) {
        textHomeName = (TextView) view.findViewById(R.id.home_name);
        textAwayName = (TextView) view.findViewById(R.id.away_name);
        textScore = (TextView) view.findViewById(R.id.score);
        textMatchTime = (TextView) view.findViewById(R.id.matchtime);
        imageHomeCrest = (ImageView) view.findViewById(R.id.home_crest);
        imageAwayCrest = (ImageView) view.findViewById(R.id.away_crest);
    }
}
