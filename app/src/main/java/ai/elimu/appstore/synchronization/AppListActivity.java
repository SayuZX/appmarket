package ai.elimu.appstore.synchronization;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ai.elimu.appstore.BaseApplication;
import ai.elimu.appstore.R;
import ai.elimu.appstore.dao.ApplicationDao;
import ai.elimu.appstore.model.Application;
import ai.elimu.appstore.receiver.InstallCompleteReceiver;
import timber.log.Timber;

public class AppListActivity extends AppCompatActivity {

    private final String DATE_FORMAT_LAST_SYNC = "yyyy-MM-dd HH:mm";

    private TextView textViewLastSynchronization;

    private List<Application> applicationsList;

    private AppListAdapter appListAdapter;

    private RecyclerView appListRecyclerView;

    private ApplicationDao applicationDao;

    private InstallCompleteReceiver installCompleteReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        BaseApplication baseApplication = (BaseApplication) getApplication();
        applicationDao = baseApplication.getDaoSession().getApplicationDao();

        initInstallCompleteReceiver();
        initViews();
    }

    /**
     * Initialize install completion receiver
     */
    private void initInstallCompleteReceiver() {
        installCompleteReceiver = new InstallCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        intentFilter.addDataScheme("package");
        registerReceiver(installCompleteReceiver, intentFilter);
    }

    /**
     * Initialize main views of app list activity
     */
    private void initViews() {
        appListRecyclerView = findViewById(R.id.recycler_view_apps);
        appListRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        appListRecyclerView.setLayoutManager(layoutManager);

        textViewLastSynchronization = findViewById(R.id.textViewLastSynchronization);
        // Display the time of last synchronization with the server
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());
        long timeOfLastSynchronization = sharedPreferences.getLong(AppSynchronizationActivity
                .DownloadAppListAsyncTask.PREF_LAST_SYNCHRONIZATION, 0);
        Date date = new Date(timeOfLastSynchronization);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_LAST_SYNC);
        String dateAsString = simpleDateFormat.format(date);
        textViewLastSynchronization.setText(String.format(getString(R.string
                .last_synchronization), dateAsString));

        // Load the list of Applications stored in the local database
        applicationsList = applicationDao.loadAll();
        Timber.i("applicationsList.size(): " + applicationsList.size());
        appListAdapter = new AppListAdapter(applicationsList, installCompleteReceiver);
        appListRecyclerView.setAdapter(appListAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(installCompleteReceiver);
    }
}
