package uk.ac.wlv.myblogapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Screen used to edit an existing blog message
public class EditMessageActivity extends AppCompatActivity {

    TextView editHeader;
    EditText editTitleInput, editMessageInput;
    FrameLayout editImageFrame;
    ImageView editImagePreview;
    ImageButton changeImageButton;
    Button addImageButton, updateMessageButton;

    DatabaseHelper databaseHelper;
    int messageId;
    Message currentMessage;

    String selectedImagePath = "";
    String currentPhotoPath = "";

    private static final int REQUEST_GALLERY = 200;
    private static final int REQUEST_CAMERA = 201;
    private static final int REQUEST_CAMERA_PERMISSION = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_message);

        editHeader = findViewById(R.id.editHeader);
        editTitleInput = findViewById(R.id.editTitleInput);
        editMessageInput = findViewById(R.id.editMessageInput);

        editImageFrame = findViewById(R.id.editImageFrame);
        editImagePreview = findViewById(R.id.editImagePreview);
        changeImageButton = findViewById(R.id.changeImageButton);
        addImageButton = findViewById(R.id.addImageButton);
        updateMessageButton = findViewById(R.id.updateMessageButton);

        databaseHelper = new DatabaseHelper(this);
        messageId = getIntent().getIntExtra("MESSAGE_ID", -1);

        editHeader.setOnClickListener(v -> finish());

        loadMessageData();

        changeImageButton.setOnClickListener(v -> showImageOptions());
        addImageButton.setOnClickListener(v -> showImageOptions());

        updateMessageButton.setOnClickListener(v -> updateMessage());
    }

    // Loads existing message details into edit fields
    private void loadMessageData() {
        currentMessage = databaseHelper.getMessageById(messageId);

        if (currentMessage != null) {
            editTitleInput.setText(currentMessage.getTitle());
            editMessageInput.setText(currentMessage.getContent());

            selectedImagePath = currentMessage.getImagePath();

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File imageFile = new File(selectedImagePath);

                if (imageFile.exists()) {
                    editImagePreview.setImageURI(Uri.fromFile(imageFile));
                    editImageFrame.setVisibility(View.VISIBLE);
                    addImageButton.setVisibility(View.GONE);
                } else {
                    editImageFrame.setVisibility(View.GONE);
                    addImageButton.setVisibility(View.VISIBLE);
                }
            } else {
                editImageFrame.setVisibility(View.GONE);
                addImageButton.setVisibility(View.VISIBLE);
            }

        } else {
            Toast.makeText(this, "Message not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Shows image change options for gallery, camera, or remove
    private void showImageOptions() {
        String[] options = {"Choose from Photos/Gallery", "Capture from Camera", "Remove Image"};

        new AlertDialog.Builder(this)
                .setTitle("Change Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else if (which == 1) {
                        checkCameraPermission();
                    } else {
                        removeImage();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose picture"), REQUEST_GALLERY);
    }

    // Checks camera permission before taking photo
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        }
    }

    // Opens camera to capture a new image
    private void openCamera() {
        try {
            File imageFile = createImageFile();

            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    imageFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAMERA);

        } catch (Exception e) {
            Toast.makeText(this, "Camera open failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Creates image file for edited message camera photo
    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "EDIT_IMG_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Copies gallery image into app storage
    private String copyImageToAppStorage(Uri imageUri) {
        try {
            if (imageUri == null) {
                return "";
            }

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir == null) {
                return "";
            }

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                return "";
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            File imageFile = new File(
                    storageDir,
                    "EDIT_GALLERY_" + timeStamp + ".jpg"
            );

            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[4096];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gallery error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return "";
        }
    }

    // Handles selected image or camera photo result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedUri = data.getData();

                selectedImagePath = copyImageToAppStorage(selectedUri);

                if (!selectedImagePath.isEmpty()) {
                    editImagePreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                    editImageFrame.setVisibility(View.VISIBLE);
                    addImageButton.setVisibility(View.GONE);
                    Toast.makeText(this, "Image changed from Photos/Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image change failed", Toast.LENGTH_SHORT).show();
                }
            }

            if (requestCode == REQUEST_CAMERA) {
                selectedImagePath = currentPhotoPath;

                editImagePreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                editImageFrame.setVisibility(View.VISIBLE);
                addImageButton.setVisibility(View.GONE);
                Toast.makeText(this, "Image changed from Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handles camera permission response
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Validates input and updates message in SQLite database
    private void updateMessage() {
        String title = editTitleInput.getText().toString().trim();
        String content = editMessageInput.getText().toString().trim();

        if (title.isEmpty()) {
            editTitleInput.setError("Please enter title");
            return;
        }

        if (content.isEmpty()) {
            editMessageInput.setError("Please enter message");
            return;
        }

        String dateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

        currentMessage.setTitle(title);
        currentMessage.setContent(content);
        currentMessage.setImagePath(selectedImagePath);
        currentMessage.setDateTime(dateTime);

        boolean updated = databaseHelper.updateMessage(currentMessage);

        if (updated) {
            Toast.makeText(this, "Message updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Removes image from current message
    private void removeImage() {
        selectedImagePath = "";

        editImagePreview.setImageDrawable(null);
        editImageFrame.setVisibility(View.GONE);
        addImageButton.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Image removed. Click Update Message to save.", Toast.LENGTH_SHORT).show();
    }
}