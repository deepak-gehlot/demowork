package com.widevision.dollarstar.activity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.ListView;

import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetPostDao;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.NetworkUtils;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshService extends Service {

    private Timer timer;
    private TimerTask timerTask;
    public static String REFRESH_TAG = "com.widevision.dollarstar.refresh_tag";

    public RefreshService() {
        timer = new Timer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timerTask = new TimerTask() {
            @Override
            public void run() {
                checkRefresh();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 100000, 20000);

        return START_STICKY;
    }

    private void checkRefresh() {
        GetPostDao getPostDao = new GetPostDao(PreferenceConnector.readString(RefreshService.this, PreferenceConnector.LOGIN_UserId, ""));
        getPostDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            Collections.reverse(result.data);
                            int postCount = PreferenceConnector.readInteger(RefreshService.this, PreferenceConnector.POST_COUNT, 0);
                            if (result.data.size() > postCount) {
                                sendBroadcast(new Intent(REFRESH_TAG));
                                PreferenceConnector.writeInteger(RefreshService.this, PreferenceConnector.POST_COUNT, result.data.size());
                            }
                        }
                    }
                }
            }
        });
    }
}
