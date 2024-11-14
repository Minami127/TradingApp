package com.example.carrotapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.PictureRes;
import com.example.carrotapp.model.PictureUpload;
import com.example.carrotapp.model.User;
import com.example.carrotapp.model.UserRes;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserProfileUpdateActivity extends AppCompatActivity {

    ImageButton backBtn,updateBtn;
    EditText updateNickname;
    TextView nicknameView;
    ShapeableImageView updateImg;
    String nickname;
    int id;
    ActivityResultLauncher<Intent> galleryLauncher;
    String profileImgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        nicknameView = findViewById(R.id.update_nickname_view);
        updateNickname = findViewById(R.id.update_nickname);
        updateImg = findViewById(R.id.update_profile_img);
        backBtn = findViewById(R.id.update_back_btn);
        updateBtn = findViewById(R.id.update);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        profileImgUrl = sp.getString("profileImg", null);
        nickname = sp.getString("nickname", null);
        String userId = sp.getString("userId", null);
        id = Integer.parseInt(userId);
        Log.d("qaz", "저장된 프로필 이미지 URL: " + profileImgUrl);
        Log.d("qaz", "저장된 닉네임 " + nickname);
        Log.d("qaz", "저장된id " + id);

        nicknameView.setText(nickname);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // 하나의 이미지 선택하는 경우
                        if (data.getData() != null) {
                            Uri imageUri = data.getData();

                            Glide.with(this)
                                    .load(imageUri)
                                    .into(updateImg);

                            profileImgUrl = imageUri.toString();
                        }


                    }
                }
        );

        if (updateImg != null) {
            if (profileImgUrl != null) {
                Glide.with(this)
                        .load(profileImgUrl)
                        .into(updateImg);
            } else {
                Glide.with(this)
                        .load(R.drawable.person_gray)
                        .into(updateImg);
            }
        } else {
            Log.e("ProfileFragment", "Profile image view is null");
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateBtn.setOnClickListener(v -> {
            nickname = updateNickname.getText().toString().trim();
            Log.d("qwer", "onCreate: " + nickname);
            editNickname(id,nickname);
            editProfileImg(id, profileImgUrl);
            getUserInfo(id);
        });
        updateImg.setOnClickListener(view -> myPermission());

    }

    private void editNickname(int id,String nickname){

        Retrofit retrofit = NetworkClient.getRetrofitClient(UserProfileUpdateActivity.this);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token","");

        User user = new User(nickname);

        UserApi api = retrofit.create(UserApi.class);



        Call<UserRes> call = api.EditProfileNickname(id,"Bearer " + token,user);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful()){
                    Log.d("qwqrt", "success: " + response.body());

                } else {
                    Log.d("qwqrt", "fail: " + response.code() +"  "+ response.message());
                }

            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("qwqrt", "fail: ");

            }
        });

    }
    private void editProfileImg(int id,String profileImgUrl){

        Retrofit retrofit = NetworkClient.getRetrofitClient(UserProfileUpdateActivity.this);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token = sp.getString("token","");

        // content:// URI를 실제 파일 경로로 변환
        Uri uri = Uri.parse(profileImgUrl);
        String filePath = getRealPathFromURI(uri);

        // 이미지 URI를 File 객체로 변환
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);


        UserApi api = retrofit.create(UserApi.class);

        Call<UserRes> call = api.EditProfileImg(id,"Bearer " + token, body);

        Log.d("qwe2", "Token: " + token);  // 토큰 확인

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if(response.isSuccessful()){
                    Log.d("qwe2", "success: " + response.body());

                }else {
                    Log.d("qwe2", "failure " + response.code());
                }

            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("qwe2", "die: " + t.getMessage());  // 예외 메시지 출력
                t.printStackTrace();  // 예외 스택 트레이스 출력
            }
        });
    }

    private void getUserInfo(int id){
        Retrofit retrofit = NetworkClient.getRetrofitClient(UserProfileUpdateActivity.this);

        UserApi api = retrofit.create(UserApi.class);

        Call<UserRes> call = api.userInfo(id);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if(response.isSuccessful()){
                    UserRes userRes = response.body();
                    Log.d("qwqrt", "success: " + response.body());
                    SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", userRes.accessToken);
                    editor.putString("userId", userRes.userId);
                    editor.putString("nickname", userRes.nickname);
                    editor.putString("profileImg", userRes.profileImg);
                    editor.putInt("type", 0);
                    editor.apply();

                    finish();


                } else {
                    Log.d("qwqrt", "fail: " + response.code() +"  "+ response.message());
                }
            }
            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                Log.d("qwqrt", "fail: ");

            }
        });
    }

    private void openGalleryWithLauncher() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<String []> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permission -> {
                boolean allGranted = true;

                for(Boolean isGranted : permission.values()){
                    if(!isGranted){
                        allGranted = false;
                        break;
                    }
                }
                if(allGranted){
                    // all granted
                } else {
                    // all is not granted
                }

            });

    private void myPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            String[] permissions = {
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_AUDIO,
                    android.Manifest.permission.READ_MEDIA_VIDEO
            };

            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);

                }
            }

            if (permissionsToRequest.isEmpty()) {
                openGalleryWithLauncher();
            } else {
                String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
                boolean shouldShowRationale = false;

                for (String permission : permissionsArray) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }

                if (shouldShowRationale) {
                    new AlertDialog.Builder(this).setMessage("Please Allow all Permissions")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                } else {
                    requestPermissionLauncher.launch(permissionsArray);
                }


            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
            };

            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);

                }
            }

            if (permissionsToRequest.isEmpty()) {
                openGalleryWithLauncher();
            } else {
                String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
                boolean shouldShowRationale = false;

                for (String permission : permissionsArray) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }

                if (shouldShowRationale) {
                    new AlertDialog.Builder(this).setMessage("Please Allow all Permissions")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                } else {
                    requestPermissionLauncher.launch(permissionsArray);
                }

            }

        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

}