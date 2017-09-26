package com.realm.moviesmazak.Manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "HDTeluguMovies";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setRated() {
        editor.putBoolean("RATED", true);
        editor.commit();
    }

    public boolean isRated() {
        return pref.getBoolean("RATED", false);
    }
}
