package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by Alexander on 10/24/2015.
 */
public class Utils {

    //Based on a stackoverflow snippet
    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static Toast mToastNoNetwork = null;
    public static void showToastNoNetworkAvailable(Activity activity) {
        if (mToastNoNetwork != null)
            mToastNoNetwork.cancel();
        mToastNoNetwork = Toast.makeText(activity, it.jaschke.alexandria.R.string.no_network_message, Toast.LENGTH_LONG);
        mToastNoNetwork.show();
    }
}
