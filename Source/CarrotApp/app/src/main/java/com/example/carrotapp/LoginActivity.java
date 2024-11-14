package com.example.carrotapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.User;
import com.example.carrotapp.model.UserRes;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;




 public class LoginActivity extends AppCompatActivity {


    TextView txtView;
    ImageButton btnLogin;
    EditText login_email;
    EditText login_password;
    ImageView imgView;


    private static final int RC_SIGN_IN = 9001; // Google Sign-In request code
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtView = findViewById(R.id.txt_register);
        btnLogin = findViewById(R.id.login_button);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);

        imgView = findViewById(R.id.imgView);

        // Google Sign-In 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // 로그인 버튼 클릭 리스너
        findViewById(R.id.btn_google_sign_in).setOnClickListener(view -> signIn());


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = login_email.getText().toString().trim();
                String password = login_password.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this,"항목을 모두 입력하세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 체크
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if (!pattern.matcher(email).matches()){
                    Toast.makeText(LoginActivity.this,"이메일 형식을 확인하세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgress();

                // Firebase Authentication으로 로그인 시도
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            dismissProgress();
                            if (task.isSuccessful()) {
                                // 로그인 성공
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                    // 이메일 인증됨, 서버 API 호출
                                    Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
                                    UserApi api = retrofit.create(UserApi.class);
                                    User user = new User(email, password);
                                    Call<UserRes> call = api.login(user);

                                    // 서버로부터 응답 처리
                                    call.enqueue(new Callback<UserRes>() {
                                        @Override
                                        public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                                            if (response.isSuccessful()) {
                                                UserRes userRes = response.body();

                                                Log.d("rty1", "Result: " + userRes.accessToken);
                                                Log.d("rty1", "UserId: " + userRes.userId);
                                                Log.d("rty1", "ProfileImg: " + userRes.profileImg);
                                                Log.d("rty1", "Nickname: " + userRes.nickname);
                                                Log.d("rty1", "AccessToken: " + userRes.accessToken);
                                                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putString("token", userRes.accessToken);
                                                editor.putString("userId", userRes.userId);
                                                editor.putString("nickname", userRes.nickname);
                                                editor.putString("profileImg", userRes.profileImg);
                                                editor.putInt("type", 0);
                                                editor.apply();

                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                handleLoginError(response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<UserRes> call, Throwable t) {
                                            dismissProgress();
                                            Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    // 이메일 인증 안된 경우
                                    Toast.makeText(LoginActivity.this, "이메일 인증 후 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                                    if (firebaseUser != null) {
                                        // 인증 이메일 다시 보내기
                                        firebaseUser.sendEmailVerification().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "인증 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            } else {
                                // 로그인 실패 처리
                                Log.e("asdf", "onClick: "+ task.getException().getMessage());
                                Toast.makeText(LoginActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleLoginError(int responseCode) {
        switch (responseCode) {
            case 400:
                Toast.makeText(LoginActivity.this, "회원가입이 되지 않은 이메일이거나, 비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show();
                break;
            case 500:
                Toast.makeText(LoginActivity.this, "DB 처리중에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

     private void signIn() {
         GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

         Log.d("edd","acc"+ account);


         if (account != null) {
             String email = account.getEmail();
             String password = "GoogleUser";

             checkUser(email);

             showProgress();

         } else {
             // 로그인 화면 표시
             Intent signInIntent = mGoogleSignInClient.getSignInIntent();
             startActivityForResult(signInIntent, RC_SIGN_IN);
         }
     }

     private void checkUser(String email){

         Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
         UserApi api = retrofit.create(UserApi.class);
         Call<UserRes> call = api.checkEmail(email);

         showProgress();

         call.enqueue(new Callback<UserRes>() {
             @Override
             public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                 if (response.isSuccessful()) {
                     UserRes userRes = response.body();
                     Log.d("rty2", "Result: " + userRes.accessToken);
                     Log.d("rty2", "UserId: " + userRes.userId);
                     Log.d("rty2", "ProfileImg: " + userRes.profileImg);
                     Log.d("rty2", "Nickname: " + userRes.nickname);
                     Log.d("rty2", "AccessToken: " + userRes.accessToken);


                     if (userRes.exists) {
                         SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                         SharedPreferences.Editor editor = sp.edit();
                         editor.putString("token", userRes.accessToken);
                         editor.putString("userId", userRes.userId);
                         editor.putString("nickname", userRes.nickname);
                         editor.putString("profileImg", userRes.profileImg);
                         editor.putInt("type", 0);
                         editor.apply();

                         loginUser(email, "GoogleUser");

                         Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                         startActivity(intent);
                         finish();


                     } else {

                         registerNewUser(email);
                     }
                 } else {
                     handleLoginError(response.code());
                 }
             }

             @Override
             public void onFailure(Call<UserRes> call, Throwable t) {

             }
         });

     }

     private void loginUser(String email,String password){

         Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
         UserApi api = retrofit.create(UserApi.class);
         User user = new User(email, password);
         Call<UserRes> call = api.login(user);

         call.enqueue(new Callback<UserRes>() {
             @Override
             public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                 if (response.isSuccessful()) {
                     UserRes userRes = response.body();
                     Log.d("rty3", "Result: " + userRes.accessToken);
                     Log.d("rty3", "UserId: " + userRes.userId);
                     Log.d("rty3", "ProfileImg: " + userRes.profileImg);
                     Log.d("rty3", "Nickname: " + userRes.nickname);
                     Log.d("rty3", "AccessToken: " + userRes.accessToken);
                     SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                     SharedPreferences.Editor editor = sp.edit();
                     editor.putString("token", userRes.accessToken);
                     editor.putString("userId", userRes.userId);
                     editor.putString("nickname", userRes.nickname);
                     editor.putString("profileImg", userRes.profileImg);
                     editor.putInt("type", 0);
                     editor.apply();

                     Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                     startActivity(intent);
                     finish();
                 } else {
                     handleLoginError(response.code());
                 }
             }

             @Override
             public void onFailure(Call<UserRes> call, Throwable t) {
                 dismissProgress();
                 Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
             }
         });
     }


     @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            firebaseAuthWithGoogle(account);
            String email = account.getEmail();
            Log.d("edd","email"+email);
            loginUser(email,"GoogleUser");


        } catch (ApiException e) {
            Log.w("LoginActivity", "Google sign in failed", e);
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        String email = user.getEmail();
                        String nickname = user.getDisplayName();
                        nickname = nickname.replaceAll("\\s+", ""); // 모든 공백 제거


                        getNetworkData(email,nickname);


                        showProgress();


                    } else {
                        // 로그인 실패
                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
     private void registerNewUser(String email) {
         // 새로운 사용자 등록 프로세스 호출
         String nickname = email.split("@")[0]; // 이메일에서 기본 닉네임 생성
         getNetworkData(email, nickname); // 기존 등록 메서드 호출
     }
    private void getNetworkData(String email, String nickname) {



        Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);

        UserApi api = retrofit.create(UserApi.class);

        String password = "GoogleUser";


        User user = new User(nickname,email,password);

        Call<UserRes> call = api.register(user);

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                dismissProgress();

                if (response.isSuccessful()){
                    UserRes userRes = response.body();
                    SharedPreferences sp =
                            getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token",userRes.accessToken);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }
                else{

                    return;
                }
            }
            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                dismissProgress();
                Toast.makeText(LoginActivity.this,"통신실패",Toast.LENGTH_SHORT).show();
            }
        });

    }
    private long time= 0;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

     // 네트워크 데이터 처리할때 사용할 다이얼로그
     Dialog dialog;
     private void showProgress(){
         dialog = new Dialog(this);
         dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
         dialog.setContentView(new ProgressBar(this));
         dialog.setCancelable(false);
         dialog.setCanceledOnTouchOutside(false);
         dialog.show();
     }

     private void dismissProgress(){
         dialog.dismiss();
     }


 }
