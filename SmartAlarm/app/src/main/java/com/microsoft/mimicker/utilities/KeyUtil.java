package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class KeyUtil {
    public static String getToken(Context caller, String resource) {
        String token = null;
        try {
            ApplicationInfo ai = caller.getPackageManager().getApplicationInfo(caller.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            token = bundle.getString("com.microsoft.mimicker.token." + resource);
        } catch (Exception ex) {
            Logger.trackException(ex);
        }
        return token;
    }
}
