package ai.elimu.appstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LicenseOptionActivity extends AppCompatActivity {

    public static final String PREF_LICENSE_OPTION = "pref_license_option";

    private Button buttonOptionNo;

    private Button buttonOptionYes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_option);

        buttonOptionNo = (Button) findViewById(R.id.buttonOptionNo);
        buttonOptionYes = (Button) findViewById(R.id.buttonOptionYes);
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        buttonOptionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "buttonOptionNo onClick");

                sharedPreferences.edit().putString(PREF_LICENSE_OPTION, "no").commit();

                // Restart application
                Intent intent = getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonOptionYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "buttonOptionYes onClick");

                sharedPreferences.edit().putString(PREF_LICENSE_OPTION, "yes").commit();

                // Restart application
                Intent intent = getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}