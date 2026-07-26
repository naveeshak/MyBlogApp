package uk.ac.wlv.myblogapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.ArrayList;

// Screen used to select multiple messages for group delete or group upload
public class SelectMessageActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;TextView selectHeader, selectionCountText, selectAllText;
    RecyclerView selectRecyclerView;
    View uploadSelectedAction, deleteSelectedAction;
    ArrayList<Message> messageList;
    SelectMessageAdapter selectMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_message);

        selectHeader = findViewById(R.id.selectHeader);
        selectionCountText = findViewById(R.id.selectionCountText);
        selectAllText = findViewById(R.id.selectAllText);
        selectRecyclerView = findViewById(R.id.selectRecyclerView);

        uploadSelectedAction = findViewById(R.id.uploadSelectedAction);
        deleteSelectedAction = findViewById(R.id.deleteSelectedAction);

        databaseHelper = new DatabaseHelper(this);

        selectHeader.setOnClickListener(v -> finish());

        selectRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadMessages();

        // Selects all messages or clears all selected messages
        selectAllText.setOnClickListener(v -> {
            if (selectMessageAdapter.getSelectedMessages().size() == messageList.size()) {
                selectMessageAdapter.clearSelection();
            } else {
                selectMessageAdapter.selectAllMessages();
            }
        });

        uploadSelectedAction.setOnClickListener(v -> uploadSelectedMessages());
        deleteSelectedAction.setOnClickListener(v -> confirmDeleteSelected());
    }

    // Loads messages into selectable list
    private void loadMessages() {
        messageList = databaseHelper.getAllMessages();

        selectMessageAdapter = new SelectMessageAdapter(this, messageList, this::updateSelectionCount);
        selectRecyclerView.setAdapter(selectMessageAdapter);

        updateSelectionCount();
    }

    // Updates selected message count text
    private void updateSelectionCount() {
        int count = selectMessageAdapter.getSelectedMessages().size();
        selectionCountText.setText(count + " selected");

        if (!messageList.isEmpty() && count == messageList.size()) {
            selectAllText.setText("Clear All");
        } else {
            selectAllText.setText("Select All");
        }
    }

    // Confirms and deletes selected messages
    private void confirmDeleteSelected() {
        ArrayList<Message> selectedMessages = selectMessageAdapter.getSelectedMessages();

        if (selectedMessages.isEmpty()) {
            Toast.makeText(this, "Please select messages first", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Messages")
                .setMessage("Are you sure you want to delete selected messages?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedCount = databaseHelper.deleteSelectedMessages(selectedMessages);

                    if (deletedCount > 0) {
                        Toast.makeText(this, deletedCount + " messages deleted", Toast.LENGTH_SHORT).show();
                        loadMessages();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Uploads selected group of messages to Firebase
    private void uploadSelectedMessages() {
        ArrayList<Message> selectedMessages = selectMessageAdapter.getSelectedMessages();

        if (selectedMessages.isEmpty()) {
            Toast.makeText(this, "Please select messages first", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading selected messages...", Toast.LENGTH_SHORT).show();

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance("https://myblogapp-ffb05-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("uploaded_group_messages");

        String groupId = databaseReference.push().getKey();

        if (groupId == null) {
            Toast.makeText(this, "Upload group ID failed", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> groupMap = new HashMap<>();
        groupMap.put("uploadedAt", String.valueOf(System.currentTimeMillis()));
        groupMap.put("messageCount", selectedMessages.size());

        HashMap<String, Object> messagesMap = new HashMap<>();

        for (Message message : selectedMessages) {
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("localId", message.getId());
            messageMap.put("title", message.getTitle());
            messageMap.put("content", message.getContent());
            messageMap.put("dateTime", message.getDateTime());
            messageMap.put("imagePath", message.getImagePath());

            messagesMap.put(String.valueOf(message.getId()), messageMap);
        }

        groupMap.put("messages", messagesMap);

        databaseReference.child(groupId).setValue(groupMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Selected messages uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = "Upload failed";

                        if (task.getException() != null) {
                            error = task.getException().getMessage();
                        }

                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}