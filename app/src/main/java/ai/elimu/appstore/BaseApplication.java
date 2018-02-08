package ai.elimu.appstore;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.securepreferences.SecurePreferences;

import org.greenrobot.greendao.database.Database;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import ai.elimu.appstore.dao.CustomDaoMaster;
import ai.elimu.appstore.dao.DaoSession;
import ai.elimu.appstore.service.ProgressResponseBody;
import ai.elimu.appstore.service.ProgressUpdateCallback;
import ai.elimu.appstore.util.VersionHelper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import timber.log.Timber;

public class BaseApplication extends Application {

    //Name of the shared pref file. If null use the default shared prefs
    private static final String PREF_FILE_NAME = "app_store_preferences.xml";

    //user password/code used to generate encryption key.
    private static String PREF_PASSWORD;

    private DaoSession daoSession;

    private Retrofit retrofit;

    private static SecurePreferences securePreferences;

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        PREF_PASSWORD = getKeyHash();
        context = getApplicationContext();

        // Log config
        if (BuildConfig.DEBUG) {
            // Log everything
            Timber.plant(new Timber.DebugTree());
        } else {
            // Only log warnings and errors
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable throwable) {
                    if (priority == Log.WARN) {
                        Log.w(tag, message);
                    } else if (priority == Log.ERROR) {
                        Log.e(tag, message);
                    }
                }
            });
        }
        Timber.i("onCreate");

        // greenDAO config
        CustomDaoMaster.DevOpenHelper helper = new CustomDaoMaster.DevOpenHelper(this, "appstore-db");
        Database db = helper.getWritableDb();
        daoSession = new CustomDaoMaster(db).newSession();

        VersionHelper.updateAppVersion(getApplicationContext());
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * Initializes Retrofit and makes it available to all activities.
     *
     * @param progressUpdateCallback If the request needs to update progress to user (in case of downloading or
     *                               uploading
     *                               big file, then pass in a progress update callback
     * @return Retrofit instance
     */
    public Retrofit getRetrofit(final ProgressUpdateCallback progressUpdateCallback) {
        Timber.i("getRetrofit");

        /**
         * Adding logging interceptor for printing out Retrofit request url
         * in debug mode
         */
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        final ProgressResponseBody.ProgressListener progressListener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
//                Timber.i("bytesRead: " + bytesRead);
//                Timber.i("contentLength: " + contentLength);
//                Timber.i("done: " + done);
//                Timber.i("%d%% done\n", (100 * bytesRead) / contentLength);

                if (progressUpdateCallback != null) {
                    long progress = (bytesRead * 100) / contentLength;
//                    Timber.d("progress: " + progress);

                    // E.g. "6.00 MB/12.00 MB   50%"
                    String progressText = String.format(context.getString(R.string
                                    .app_list_download_progress_number), bytesRead / 1024f / 1024f,
                            contentLength / 1024f / 1024f, progress);

//                    Timber.i("progressText: " + progressText);
                    progressUpdateCallback.onProgressUpdated(progressText, (int) progress);
                }
            }
        };


        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        /**
         * In case of downloading/uploading big files and having necessity to update progress to UI, then add a
         * progress listener
         */
        if (progressListener != null) {
            okHttpClientBuilder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        if (retrofit == null || progressUpdateCallback != null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.REST_URL + "/")
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

    /**
     * Provides {@SecurePreferences} singleton instance to access shared preferences
     *
     * @return A single instance of {@SecurePreferences}
     */
    public static SharedPreferences getSharedPreferences() {
        if (securePreferences == null) {
            synchronized (BaseApplication.class) {
                if (securePreferences == null) {
                    securePreferences = new SecurePreferences(getAppContext(), PREF_PASSWORD,
                            PREF_FILE_NAME);
                    SecurePreferences.setLoggingEnabled(BuildConfig.DEBUG);
                }
            }
        }
        return securePreferences;
    }

    public static Context getAppContext() {
        return context;
    }

    /**
     * Get keystore hash value to use as secure preferences' password
     *
     * @return The hash value generated from signing key
     */
    private String getKeyHash() {
        String keyHash = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }

            return keyHash;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e);
            return "";
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e);
            return "";
        }
    }
}
