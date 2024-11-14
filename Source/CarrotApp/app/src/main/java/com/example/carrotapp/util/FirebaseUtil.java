package com.example.carrotapp.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {

    public static String CurrentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        if(CurrentUserId() != null) {
            return true;
        }else {
            return false;
        }
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(CurrentUserId());
    }
}
