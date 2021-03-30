package com.example.mywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView wallet = findViewById(R.id.wallet);
        Button connect = findViewById(R.id.connect);



        EditText publicKey = findViewById(R.id.public_key);
        EditText privateKey = findViewById(R.id.private_key);

        connect.setOnClickListener(view -> {
            RetrieveWallet retrieveWallet = new RetrieveWallet();
            try {
                wallet.setText(String.valueOf(retrieveWallet.execute(
                        publicKey.getText().toString(),
                        privateKey.getText().toString(),
                        "EUR").get()));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}