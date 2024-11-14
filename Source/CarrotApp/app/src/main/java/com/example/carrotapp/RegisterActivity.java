package com.example.carrotapp;

import android.app.Dialog;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.UserApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.User;
import com.example.carrotapp.model.UserRes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageButton btn,imgBtn;
    EditText register_nickname, register_email, register_password, register_correct_password;
    FirebaseAuth mAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressBar = findViewById(R.id.register_progressBar);
        btn = findViewById(R.id.register_button);
        imgBtn = findViewById(R.id.back_btn);

        progressBar.setVisibility(View.GONE);

        register_nickname = findViewById(R.id.register_nickname);
        register_email = findViewById(R.id.register_email);
        register_password = findViewById(R.id.register_password);
        register_correct_password = findViewById(R.id.register_correct_password);


        mAuth = FirebaseAuth.getInstance();


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = register_nickname.getText().toString().trim();
                String email = register_email.getText().toString().trim();
                String password = register_password.getText().toString().trim();
                String password2 = register_correct_password.getText().toString().trim();


                if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || password2.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"항목을 모두 입력하세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식이 맞는지 체크
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if (pattern.matcher(email).matches() == false){
                    Toast.makeText(RegisterActivity.this,"이메일 형식을 확인하세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 길이체크 8~24자리까지 허용

                if(password.length() < 8 || password.length() >= 24){
                    Toast.makeText(RegisterActivity.this,"비밀번호 길이를 확인하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }


                // 비밀번호 2개가 일치하는지 확인

                if(!password.equals(password2)){
                    Toast.makeText(RegisterActivity.this,"비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }


                showProgress();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // 인증 이메일 보내기
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                dismissProgress();
                                                if (emailTask.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "인증 이메일을 확인하세요.", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();
                                                    getNetworkData(nickname, email, password);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                dismissProgress();
                                Toast.makeText(RegisterActivity.this,
                                        "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });



            }
        });

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getNetworkData(String nickname, String email, String password) {

        Retrofit retrofit = NetworkClient.getRetrofitClient(RegisterActivity.this);


        UserApi api = retrofit.create(UserApi.class);


        User user = new User(nickname,email,password);

        // 4. api 호출
        Call<UserRes> call = api.register(user);

        // 5.  서버로부터 받은 응답을 처리하는 코드 작성.
        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                dismissProgress();
                //서버에서 보낸 응답이 200 ok 일때 처리하는 코드.
                if (response.isSuccessful()){
                    UserRes userRes = response.body();
                    Log.i("AAA","result : " + userRes.result);
                    Log.i("AAA","Token : " + userRes.accessToken);
                    SharedPreferences sp =
                            getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token",userRes.accessToken);
                    editor.apply();


                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    startActivity(intent);
                    finish();

                }

                else if(response.code() == 400){
                    Toast.makeText(RegisterActivity.this,"잘못된 이메일형식이거나 비밀번호길이가 맞지 않습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }else if(response.code() == 500){
                    Toast.makeText(RegisterActivity.this,"DB 처리중에 문제가 있습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    Toast.makeText(RegisterActivity.this,"잠시 후 이용하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                //유저한테 네트워크 통신 실패했다고 알려준다.
                dismissProgress();

                Toast.makeText(RegisterActivity.this,"통신실패",Toast.LENGTH_SHORT).show();
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