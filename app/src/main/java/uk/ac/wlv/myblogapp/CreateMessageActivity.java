package uk.ac.wlv.myblogapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

// Screen used to create and save a new blog message
public class CreateMessageActivity extends AppCompatActivity {

    EditText titleInput, messageInput;
    Button attachImageButton, saveMessageButton;
    ImageView imagePreview;
    TextView backArrowText;

    DatabaseHelper databaseHelper;

    String selectedImagePath = "";
    String currentPhotoPath = "";

    private static final int REQUEST_GALLERY = 100;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        titleInput = findViewById(R.id.titleInput);
        messageInput = findViewById(R.id.messageInput);
        attachImageButton = findViewById(R.id.attachImageButton);
        saveMessageButton = findViewById(R.id.saveMessageButton);
        imagePreview = findViewById(R.id.imagePreview);
        backArrowText = findViewById(R.id.backArrowText);

        backArrowText.setOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        attachImageButton.setOnClickListener(v -> showImageOptions());

        saveMessageButton.setOnClickListener(v -> saveMessage());
    }

    // Shows options to attach image from gallery or camera
    private void showImageOptions() {
        String[] options = {"Choose from Gallery", "Capture from Camera"};

        new AlertDialog.Builder(this)
                .setTitle("Attach Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        checkCameraPermission();
                    }
                })
                .show();
    }

    // Opens phone gallery to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    // Checks camera permission before opening camera
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

    // Opens camera and saves captured image in app storage
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

    // Creates a temporary image file for camera photo
    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Copies selected gallery image into app storage
    private String copyImageToAppStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "GALLERY_" + timeStamp + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            return "";
        }
    }

    // Receives selected or captured image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedUri = data.getData();

                selectedImagePath = copyImageToAppStorage(selectedUri);

                if (!selectedImagePath.isEmpty()) {
                    imagePreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                    imagePreview.setVisibility(ImageView.VISIBLE);
                    Toast.makeText(this, "Gallery image attached", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image attach failed", Toast.LENGTH_SHORT).show();
                }
            }

            if (requestCode == REQUEST_CAMERA) {
                selectedImagePath = currentPhotoPath;

                imagePreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
                imagePreview.setVisibility(ImageView.VISIBLE);
                Toast.makeText(this, "Camera image attached", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handles camera permission result
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

    // Validates user input and saves message into SQLite
    private void saveMessage() {
        String title = titleInput.getText().toString().trim();
        String messageText = messageInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Please enter title");
            return;
        }

        if (messageText.isEmpty()) {
            messageInput.setError("Please enter message");
            return;
        }

        String dateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

        Message message = new Message(title, messageText, selectedImagePath, dateTime);

        boolean inserted = databaseHelper.insertMessage(message);

        if (inserted) {
            Toast.makeText(this, "Message saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Message save failed", Toast.LENGTH_SHORT).show();
        }
    }
}