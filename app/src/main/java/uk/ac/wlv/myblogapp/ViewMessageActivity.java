package uk.ac.wlv.myblogapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import androidx.core.content.FileProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Screen used to view one full blog message
public class ViewMessageActivity extends AppCompatActivity {

    TextView backArrowText, headerTitleText, viewTitle, viewDate, viewContent;
    ImageView viewImage;
    View editAction, shareAction, uploadAction, deleteAction;

    DatabaseHelper databaseHelper;
    int messageId;
    Message currentMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);

        backArrowText = findViewById(R.id.backArrowText);
        headerTitleText = findViewById(R.id.headerTitleText);
        viewTitle = findViewById(R.id.viewTitle);
        viewDate = findViewById(R.id.viewDate);
        viewImage = findViewById(R.id.viewImage);
        viewContent = findViewById(R.id.viewContent);

        editAction = findViewById(R.id.editAction);
        shareAction = findViewById(R.id.shareAction);
        uploadAction = findViewById(R.id.uploadAction);
        deleteAction = findViewById(R.id.deleteAction);

        databaseHelper = new DatabaseHelper(this);

        messageId = getIntent().getIntExtra("MESSAGE_ID", -1);

        backArrowText.setOnClickListener(v -> finish());

        deleteAction.setOnClickListener(v -> confirmDelete());

        shareAction.setOnClickListener(v -> shareMessage());

        uploadAction.setOnClickListener(v -> uploadMessage());

        // Opens edit screen with selected message id
        editAction.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMessageActivity.this, EditMessageActivity.class);
            intent.putExtra("MESSAGE_ID", messageId);
            startActivity(intent);
        });
    }

    // Reloads message after returning from edit screen
    @Override
    protected void onResume() {
        super.onResume();
        loadMessage();
    }

    // Loads selected message details from SQLite database
    private void loadMessage() {
        currentMessage = databaseHelper.getMessageById(messageId);

        if (currentMessage != null) {
            viewTitle.setText(currentMessage.getTitle());
            headerTitleText.setText(currentMessage.getTitle());
            viewDate.setText(currentMessage.getDateTime());
            viewContent.setText(currentMessage.getContent());

            if (currentMessage.getImagePath() != null && !currentMessage.getImagePath().isEmpty()) {
                File imageFile = new File(currentMessage.getImagePath());

                if (imageFile.exists()) {
                    viewImage.setImageURI(Uri.fromFile(imageFile));
                    viewImage.setVisibility(View.VISIBLE);
                } else {
                    viewImage.setVisibility(View.GONE);
                }
            } else {
                viewImage.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Confirms before deleting a single message
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteMessage(messageId);

                    if (deleted) {
                        Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Shares message text and image using Android email intent
    private void shareMessage() {
        if (currentMessage == null) {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailSubject = currentMessage.getTitle();

        String emailBody = "Title: " + currentMessage.getTitle() + "\n\n" +
                "Message:\n" + currentMessage.getContent() + "\n\n" +
                "Date: " + currentMessage.getDateTime();

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        if (currentMessage.getImagePath() != null && !currentMessage.getImagePath().isEmpty()) {
            File imageFile = new File(currentMessage.getImagePath());

            if (imageFile.exists()) {
                Uri imageUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        imageFile
                );

                emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Share message by Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Uploads one message to Firebase Realtime Database
    private void uploadMessage() {
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        if (currentMessage == null) {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance("https://myblogapp-ffb05-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("uploaded_messages");

        String uploadId = databaseReference.push().getKey();

        if (uploadId == null) {
            Toast.makeText(this, "Upload ID failed", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("localId", currentMessage.getId());
        messageMap.put("title", currentMessage.getTitle());
        messageMap.put("content", currentMessage.getContent());
        messageMap.put("dateTime", currentMessage.getDateTime());
        messageMap.put("imagePath", currentMessage.getImagePath());

        databaseReference.child(uploadId).setValue(messageMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Message uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = "Upload failed";

                        if (task.getException() != null) {
                            error = task.getException().getMessage();
                        }

                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}