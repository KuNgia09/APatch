package me.bmax.apatch.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.ipc.RootService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.bmax.apatch.IAPRootService;
import me.bmax.apatch.Natives;
import rikka.parcelablelist.ParcelableListSlice;

public class RootServices extends RootService {
    private static final String TAG = "RootServices";

    class Stub extends IAPRootService.Stub {
        @Override
        public ParcelableListSlice<PackageInfo> getPackages(int flags) {
            List<PackageInfo> list = getInstalledPackagesAll(flags);
            Log.i(TAG, "getPackages: " + list.size());
            return new ParcelableListSlice<>(list);
        }
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return new Stub();
    }

    List<Integer> getUserIds() {
        List<Integer> result = new ArrayList<>();
        UserManager um = (UserManager) getSystemService(Context.USER_SERVICE);
        List<UserHandle> userProfiles = um.getUserProfiles();
        for (UserHandle userProfile : userProfiles) {
            int userId = userProfile.hashCode();
            result.add(userProfile.hashCode());
        }
        return result;
    }

    ArrayList<PackageInfo> getInstalledPackagesAll(int flags) {
        ArrayList<PackageInfo> packages = new ArrayList<>();
        for (Integer userId : getUserIds()) {
            Log.i(TAG, "getInstalledPackagesAll: " + userId);
            packages.addAll(getInstalledPackagesAsUser(flags, userId));
        }
        return packages;
    }

    List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        try {
            PackageManager pm = getPackageManager();
            Method getInstalledPackagesAsUser = pm.getClass().getDeclaredMethod("getInstalledPackagesAsUser", int.class, int.class);
            return (List<PackageInfo>) getInstalledPackagesAsUser.invoke(pm, flags, userId);
        } catch (Throwable e) {
            Log.e(TAG, "err", e);
        }

        return new ArrayList<>();
    }
}