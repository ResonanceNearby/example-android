package net.getresonance.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cht.resonance.Resonance;
import com.cht.resonance.ResonanceError;
import com.cht.resonance.ResonanceListener;
import com.cht.resonance.model.Client;

public class MainActivity extends AppCompatActivity {
    private final int RECORD_AUDIO_REQUEST_CODE = 1;

    EditText payloadEditText;
    Button startSearchButton;
    TextView resultTextView;
    String payload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payloadEditText = findViewById(R.id.payload);
        resultTextView = findViewById(R.id.result);
        startSearchButton = findViewById(R.id.startSearch);

        Resonance.init(this, "7c6dc857-94f6-434f-ab5c-98b46fdb1049");

        startSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payload = payloadEditText.getText().toString();

                if("".equals(payload)) {
                    Toast.makeText(MainActivity.this, "Please input some payload", Toast.LENGTH_SHORT).show();
                    return;
                }

                prepareToSearch();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Resonance.stopSearch();
    }

    private void prepareToSearch() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startSearch();
        } else {
            String permissions[] = {Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(this, permissions, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case RECORD_AUDIO_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSearch();
                } else {
                    Toast.makeText(MainActivity.this, "User denied mic access", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void startSearch() {
        if(Resonance.isSearchRunning()) {
            return;
        }

        Resonance.startSearch(payload, new ResonanceListener() {
            @Override
            public void onNearbyFound(Client client) {
                print("Found: \"" + client.getPayload() + "\"");
            }

            @Override
            public void onNearbyLost(Client client) {
                print("Lost: \"" + client.getPayload() + "\"");
            }

            @Override
            public void onSearchStopped(ResonanceError resonanceError) {
                print("Search stopped");
                startSearchButton.setVisibility(View.VISIBLE);
                payloadEditText.setEnabled(true);
            }

            @Override
            public void onSearchStarted() {
                print("Search started. My payload: \"" + payload + "\"");
            }
        });

        startSearchButton.setVisibility(View.GONE);
        payloadEditText.setEnabled(false);
    }

    private void print(String message) {
        resultTextView.append(message + "\n");
    }
}
