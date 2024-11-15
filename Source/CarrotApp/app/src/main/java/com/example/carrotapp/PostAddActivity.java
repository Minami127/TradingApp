package com.example.carrotapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrotapp.adapter.PictureUploadAdapter;
import com.example.carrotapp.api.CategoryApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PictureUploadApi;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.Category;
import com.example.carrotapp.model.CategoryList;
import com.example.carrotapp.model.PictureRes;
import com.example.carrotapp.model.PictureUpload;
import com.example.carrotapp.model.PostAdd;
import com.google.firebase.database.collection.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostAddActivity extends AppCompatActivity {

    TextView picNum;
    EditText postTitle, postPrice, postDescribe;
    Button postAddBtn;
    ImageButton imgAdd,cancelBtn;
    Spinner categorySpinner;
    RecyclerView imgRecyclerView;
    ScrollView scrollView;
    private PictureUploadAdapter adapter;
    List<PictureUpload> imageItemList = new ArrayList<>();
    private List<String> selectedImageUrls = new ArrayList<>();
    List<PictureRes> imgList = new ArrayList<>();
    int productId = 0;
    int productState = 1;
    int sellerId = 0;
    ActivityResultLauncher<Intent> galleryLauncher,cameraLauncher;
    int count = 0;
    private int selectedCategoryId = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postTitle = findViewById(R.id.post_title);
        postPrice = findViewById(R.id.post_price);
        postDescribe = findViewById(R.id.post_describe);
        imgAdd = findViewById(R.id.imgAdd);
        postAddBtn = findViewById(R.id.add_post_btn);
        categorySpinner = findViewById(R.id.category_spinner);
        scrollView = findViewById(R.id.add_scrolview);
        cancelBtn = findViewById(R.id.cancel_btn);
        picNum = findViewById(R.id.pic_num);

        cancelBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostAddActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        getNetworkCategoryData();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            if (count > 10) {
                                Toast.makeText(this, "최대 10개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                String imageUrl = imageUri.toString();
                                if (!selectedImageUrls.contains(imageUrl)) {
                                    imageItemList.add(new PictureUpload(imageUrl));
                                    selectedImageUrls.add(imageUrl);
                                    imgList.add(new PictureRes(imageUrl,productId));
                                }
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            String imageUrl = imageUri.toString();
                            if (!selectedImageUrls.contains(imageUrl)) {
                                imageItemList.add(new PictureUpload(imageUrl));
                                selectedImageUrls.add(imageUrl);
                                imgList.add(new PictureRes(imageUrl,productId));
                                count = 1;
                            }
                        }
                        adapter.notifyDataSetChanged();

                        int selectedCount = selectedImageUrls.size();
                        picNum.setText(selectedCount + "/10");
                    }
                }
        );



        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        sellerId = sp.getInt("id", 0);


        postAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = postTitle.getText().toString().trim();
                String description = postDescribe.getText().toString().trim();
                String priceString = postPrice.getText().toString().trim();
                int price;

                try {
                    price = Integer.parseInt(priceString);
                } catch (NumberFormatException e) {
                    Toast.makeText(PostAddActivity.this, "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                int categoryId = 1;

                showProgress();

                Retrofit retrofit = NetworkClient.getRetrofitClient(PostAddActivity.this);
                PostApi api = retrofit.create(PostApi.class);
                PostAdd postAdd = new PostAdd(sellerId, categoryId, title, price, description, productState);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                String token = sp.getString("token", "");

                Call<PostAdd> call = api.getPostAdd("Bearer " + token, postAdd);
                Log.d("PostAddActivity", "Category ID: " + categoryId);


                call.enqueue(new Callback<PostAdd>() {
                    @Override
                    public void onResponse(Call<PostAdd> call, Response<PostAdd> response) {
                        dismissProgress();
                        if (response.isSuccessful() && response.body() != null) {

                            PostAdd postAdd = response.body();
                            Log.d("PosteddActivity", "success: " + postAdd.getPostId());
                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("saved_post_id", postAdd.getPostId());
                            editor.apply();


                            int product_id = postAdd.getPostId();
                            uploadImages(product_id);


                            Intent intent = new Intent(PostAddActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            dismissProgress();
                            Toast.makeText(PostAddActivity.this, "응답 실패", Toast.LENGTH_SHORT).show();
                            Log.e("PostAddActivity", "응답이 null입니다.");
                        }
                    }

                    @Override
                    public void onFailure(Call<PostAdd> call, Throwable t) {
                        Log.e("PostAddActivity", "API 호출 실패: " + t.getMessage());
                        dismissProgress();
                        Toast.makeText(PostAddActivity.this, "실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        imgRecyclerView = findViewById(R.id.img_recyclerView);
        imgRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PictureUploadAdapter(this, imageItemList);
        imgRecyclerView.setAdapter(adapter);


        imgAdd.setOnClickListener(view -> myPermission());


        imgRecyclerView.setOnClickListener(v -> {
            Log.d("PostAddActivity", "선택된 이미지 URL: " + selectedImageUrls); // 로그 추가
            Intent intent = new Intent(PostAddActivity.this, ImageSliderActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(selectedImageUrls));
            startActivity(intent);
        });



        postTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (scrollView != null) {
                    scrollView.post(() -> scrollView.scrollTo(0, postTitle.getTop()));
                }
            }
        });
        postPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (scrollView != null) {
                    scrollView.post(() -> scrollView.scrollTo(0, postPrice.getTop()));
                }
            }
        });
        postDescribe.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {

                if (scrollView != null) {
                    scrollView.post(() -> scrollView.scrollTo(0, postDescribe.getTop()));
                }
            }
        });
        postTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                postPrice.requestFocus();
                return true;
            }
            return false;
        });
        postPrice.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                postDescribe.requestFocus();
                return true;
            }
            return false;
        });


    }


    private void openGalleryWithLauncher() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }

    private void uploadImages(int product_id) {
        for (PictureRes picture : imgList) {
            getNetworkData(picture.getImgUrl(), product_id);
        }
    }

    private void getNetworkCategoryData(){

        Retrofit retrofit = NetworkClient.getRetrofitClient(PostAddActivity.this);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String token  = sp.getString("token","");

        CategoryApi api = retrofit.create(CategoryApi.class);

        Call<CategoryList> call = api.getCategoryList("Bearer "+token);
        Log.d("asdf", "asdf: "+ token);

        call.enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(Call<CategoryList> call, Response<CategoryList> response) {
                if(response.isSuccessful() && response.body() != null){
                    CategoryList categoryList = response.body();
                    Log.d("asdf", "onResponse: "+categoryList.items);
                    List<Category> categories = categoryList.items;
                    List<String> categoryNames = new ArrayList<>();

                    for (Category category : categories) {
                        categoryNames.add(category.getCategory());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(PostAddActivity.this,
                            android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);

                    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position != 0) {
                                selectedCategoryId = categories.get(position - 1).getId(); // 선택된 카테고리 id 저장
                                Log.d("Category Selected", "ID: " + selectedCategoryId);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // 아무것도 선택되지 않았을 때 처리
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CategoryList> call, Throwable t) {
                Log.d("asdf", "onFailure: ");
            }
        });

    }


    private void getNetworkData(String imageUrl, int productId) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(PostAddActivity.this);
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        PictureUploadApi api = retrofit.create(PictureUploadApi.class);

        String realPath = getRealPathFromURI(Uri.parse(imageUrl));
        Log.d("PostAddActivity", "실제 파일 경로: " + realPath);
        if (realPath != null && new File(realPath).exists()) {
            Log.d("PostAddActivity", "파일이 존재합니다: " + realPath);
        } else {
            Log.e("PostAddActivity", "파일이 존재하지 않거나 경로가 잘못되었습니다.");
        }

        File file = new File(realPath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);
        Log.d("PostAddActivity", "Sending request with product_id: " + productId + " and image: " + file.getAbsolutePath());




        Call<PictureUpload> call = api.getPost("Bearer " + token,body,productId);
        Log.d("PosteddActivity", "Sending request with product_id: " + productId + " and image: " + file.getAbsolutePath());

        call.enqueue(new Callback<PictureUpload>() {
            @Override
            public void onResponse(Call<PictureUpload> call, Response<PictureUpload> response) {
                Log.d("PostAddActivity", "서버 응답 코드: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                } else {
                    Log.e("PostAddActivity", "요청 실패: " + response.message());
                    Log.e("PostAddActivity", "업로드 실패: " + response.message());
                    Toast.makeText(PostAddActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<PictureUpload> call, Throwable t) {
                Log.e("PostAddActivity", "업로드 실패: " + t.getMessage());
                Toast.makeText(PostAddActivity.this,"업로드 실패",Toast.LENGTH_SHORT).show();

            }
        });

    }
    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String realPath = cursor.getString(columnIndex);
            cursor.close();
            return realPath;
        }
        return null;
    }

    Dialog dialog;

    private void showProgress() {
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void dismissProgress() {
        dialog.dismiss();
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
}
