package uk.ac.wlv.myblogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Main screen that displays all blog messages and handles search
public class MainActivity extends AppCompatActivity {

    Button addMessageButton;
    ImageButton selectMessagesButton;
    TextView emptyText;
    EditText searchInput;
    RecyclerView messageRecyclerView;

    DatabaseHelper databaseHelper;
    ArrayList<Message> messageList;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addMessageButton = findViewById(R.id.addMessageButton);
        selectMessagesButton = findViewById(R.id.selectMessagesButton);
        emptyText = findViewById(R.id.emptyText);
        searchInput = findViewById(R.id.searchInput);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        databaseHelper = new DatabaseHelper(this);

        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Opens create message screen
        addMessageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateMessageActivity.class);
            startActivity(intent);
        });

        // Opens select screen for group delete and group upload
        selectMessagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectMessageActivity.class);
            startActivity(intent);
        });

        // Searches messages while user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();

                if (keyword.isEmpty()) {
                    loadMessages();
                } else {
                    searchMessages(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Reloads messages when returning to this screen
    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }

    // Loads all messages from SQLite and displays them in RecyclerView
    private void loadMessages() {
        messageList = databaseHelper.getAllMessages();

        messageAdapter = new MessageAdapter(this, messageList);
        messageRecyclerView.setAdapter(messageAdapter);

        if (messageList.isEmpty()) {
            emptyText.setText("No messages yet.\nClick + to create your first message.");
            emptyText.setVisibility(View.VISIBLE);
            messageRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            messageRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Displays messages that match the search keyword
    private void searchMessages(String keyword) {
        messageList = databaseHelper.searchMessages(keyword);

        messageAdapter = new MessageAdapter(this, messageList);
        messageRecyclerView.setAdapter(messageAdapter);

        if (messageList.isEmpty()) {
            emptyText.setText("No matching messages found.");
            emptyText.setVisibility(View.VISIBLE);
            messageRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            messageRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}