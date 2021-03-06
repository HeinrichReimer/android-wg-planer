package com.heinrichreimersoftware.wg_planer.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.heinrichreimersoftware.wg_planer.Constants;
import com.heinrichreimersoftware.wg_planer.MainActivity;
import com.heinrichreimersoftware.wg_planer.content.UserContentHelper;
import com.heinrichreimersoftware.wg_planer.structure.Lesson;
import com.heinrichreimersoftware.wg_planer.structure.Representation;
import com.heinrichreimersoftware.wg_planer.structure.Teacher;
import com.heinrichreimersoftware.wg_planer.structure.User;
import com.heinrichreimersoftware.wg_planer.sync.api.WgService;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

public class SyncServerInterface {

    private Context context;
    private WgService api;
    private Gson gson;

    public SyncServerInterface(Context context) {
        this.context = context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_URL)
                .build();

        api = retrofit.create(WgService.class);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    public static CharSequence implode(CharSequence delimiter, CharSequence... elements) {
        if (elements.length == 0)
            return "";
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int i = 0; i < elements.length - 1; i++) {
            if (!TextUtils.isEmpty(elements[i].toString().trim())) {
                sb.append(elements[i]);
                sb.append(delimiter);
            }
        }
        sb.append(elements[elements.length - 1]);
        return sb;
    }

    public User getUserInfo(String username) {
        Log.d(MainActivity.TAG, "getUserInfo(" + username + ")");

        try {
            Response<User> response = api.getUser(username).execute();
            if (response.isSuccess()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public Representation[] getRepresentations(String... schoolClasses) {
        Log.d(MainActivity.TAG, "getRepresentations()");

        if (schoolClasses.length == 0) {
            User user = UserContentHelper.getUser(context);
            if (user != null && user.getSchoolClasses().length > 0) {
                schoolClasses = user.getSchoolClasses();
            }
        }

        try {
            Response<Representation[]> response;
            if (schoolClasses.length > 0) {
                response = api.getRepresentations(implode(",", schoolClasses).toString()).execute();
            } else {
                response = api.getRepresentations().execute();
            }
            if (response.isSuccess()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Representation[0];
    }

    @NonNull
    public Lesson[] getTimetable(String... schoolClasses) {
        Log.d(MainActivity.TAG, "getRepresentations()");

        if (schoolClasses.length == 0) {
            User user = UserContentHelper.getUser(context);
            if (user != null && user.getSchoolClasses().length > 0) {
                schoolClasses = user.getSchoolClasses();
            }
        }

        try {
            Response<Lesson[]> response;
            if (schoolClasses.length > 0) {
                response = api.getTimetable(implode(",", schoolClasses).toString()).execute();
            } else {
                response = api.getTimetable().execute();
            }
            if (response.isSuccess()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Lesson[0];
    }

    @NonNull
    public Teacher[] getTeachers() {
        Log.d(MainActivity.TAG, "getTeachers()");

        try {
            Response<Teacher[]> response = api.getTeachers().execute();
            if (response.isSuccess()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Teacher[0];
    }

}