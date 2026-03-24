package com.medilink.app.api;

import com.medilink.app.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Base URL is now managed in app/build.gradle via buildConfigField
    // Base URL is now managed in app/build.gradle via buildConfigField
    private static String currentBaseUrl = BuildConfig.BASE_URL;
    private static Retrofit retrofit;

    public static void setBaseUrl(String url) {
        currentBaseUrl = url;
        retrofit = null; // Re-initialize on next getClient call
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void resetClient() {
        retrofit = null;
    }
}
