package com.example.wifidirectchat;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    private ListView listViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private String userName;
    private WifiP2pDevice device;
    private final int PORT = 8988;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listViewMessages = findViewById(R.id.listViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        userName = getIntent().getStringExtra("USER_NAME");
        device = getIntent().getParcelableExtra("DEVICE");

        if (userName == null || device == null) {
            Toast.makeText(this, "Error: Missing user or device information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startServer();

        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
                editTextMessage.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        new Thread(() -> {
            try (Socket socket = new Socket(device.deviceAddress, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(userName + ": " + message);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    try (Socket client = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String message = in.readLine();
                        runOnUiThread(() -> {
                            // Display the received message
                            Toast.makeText(this, "Received: " + message, Toast.LENGTH_SHORT).show();
                        });
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(this, "Error receiving message: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error starting server: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }
}
