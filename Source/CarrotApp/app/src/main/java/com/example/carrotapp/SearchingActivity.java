package com.example.carrotapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carrotapp.api.LikeApi;
import com.example.carrotapp.api.NetworkClient;
import com.example.carrotapp.api.PostApi;
import com.example.carrotapp.config.Config;
import com.example.carrotapp.model.LikeList;
import com.example.carrotapp.model.Post;
import com.example.carrotapp.model.PostList;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchingActivity extends AppCompatActivity {

    ImageView backBtn;
    EditText searchTxt;
    int id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searching);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME,MODE_PRIVATE);
        String userId = sp.getString("userId", null);
        id = Integer.parseInt(userId);

        backBtn = findViewById(R.id.search_back);
        searchTxt = findViewById(R.id.searchEditText);

        // 자동으로 자판을 활성화시키기
        searchTxt.requestFocus();  // 포커스 설정
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchTxt, InputMethodManager.SHOW_IMPLICIT);  // 자판 띄우기

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SearchingActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            startActivity(intent);
            finish();
        });

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchText = searchTxt.getText().toString().trim();

                if (!searchText.isEmpty()) {
                    Intent intent = new Intent(SearchingActivity.this, SearchResultActivity.class);
                    intent.putExtra("searchText", searchText);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
            return false;
        });
    }

}