package com.example.recipesbook;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dmvzcwm3g");
            config.put("api_key", "928673523751425");
            config.put("api_secret", "z8HjnTnJpibkkgXFM4D6GKLyQ_I");
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}

