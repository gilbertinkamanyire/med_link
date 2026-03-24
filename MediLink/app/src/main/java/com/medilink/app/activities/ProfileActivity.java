package com.medilink.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.medilink.app.R;
import com.medilink.app.api.ApiClient;
import com.medilink.app.api.ApiService;
import com.medilink.app.models.User;
import com.medilink.app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private SessionManager sessionManager;
    private ApiService apiService;
    private User currentUser;

    private TextView tvHeaderName, tvHeaderEmail;
    private ImageView imgProfile;
    private TextInputEditText etName, etPhone, etEmergencyName, etEmergencyPhone;

    private String profilePicBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        currentUser = sessionManager.getUserDetails();

        if (currentUser == null) {
            finish();
            return;
        }

        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderEmail = findViewById(R.id.tv_header_email);
        imgProfile = findViewById(R.id.img_profile);
        
        etName = findViewById(R.id.et_profile_name);
        etPhone = findViewById(R.id.et_profile_phone);
        etEmergencyName = findViewById(R.id.et_emergency_name);
        etEmergencyPhone = findViewById(R.id.et_emergency_phone);

        Button btnSave = findViewById(R.id.btn_save_profile);
        Button btnLogout = findViewById(R.id.btn_logout);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // Setup image picker
        imgProfile.setOnClickListener(v -> openGallery());

        loadUserData();

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                
                // Set imageView
                imgProfile.setImageBitmap(selectedImage);
                // Convert to base64
                profilePicBase64 = encodeImage(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compress heavily to avoid overloading server
        bm.compress(Bitmap.CompressFormat.JPEG, 20, baos); 
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void decodeAndSetImage(String b64) {
        if (b64 != null && !b64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(b64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgProfile.setImageBitmap(decodedByte);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadUserData() {
        tvHeaderName.setText(currentUser.getName());
        tvHeaderEmail.setText(currentUser.getEmail());

        profilePicBase64 = currentUser.getProfilePic() != null ? currentUser.getProfilePic() : "";
        decodeAndSetImage(profilePicBase64);

        etName.setText(currentUser.getName());
        etPhone.setText(currentUser.getPhone());
        etEmergencyName.setText(currentUser.getEmergencyName());
        etEmergencyPhone.setText(currentUser.getEmergencyPhone());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String emName = etEmergencyName.getText().toString().trim();
        String emPhone = etEmergencyPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setPassword(currentUser.getPassword());
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setName(name);
        updatedUser.setPhone(phone);
        updatedUser.setEmergencyName(emName);
        updatedUser.setEmergencyPhone(emPhone);
        updatedUser.setProfilePic(profilePicBase64);

        apiService.updateProfile(currentUser.getId(), updatedUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateUserDetails(response.body());
                    currentUser = response.body();
                    tvHeaderName.setText(currentUser.getName());
                    Toast.makeText(ProfileActivity.this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
