/*
 *    Copyright 2016 Jason Maxfield
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.loserskater.suhidegui.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loserskater.suhidegui.R;
import com.loserskater.suhidegui.objects.Package;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class Utils {

    public static final String UID_FILE_PATH = "/su/suhide/suhide.uid";
    public static final String COMMAND_UID_LIST = "/su/suhide/list";
    public static final String COMMAND_UID_ADD = "/su/suhide/add %s";
    public static final String COMMAND_UID_REMOVE = "/su/suhide/rm \"%s\"";
    public static final String EXISTS = "exists";
    public static final String DOES_NOT_EXIST = "doesnt_exist";
    public static final String COMMAND_CHECK_FILE_EXISTS = "ls %s > /dev/null 2>&1 && echo " + EXISTS + " || echo " + DOES_NOT_EXIST;
    public static boolean haveRoot = true;
    public static ArrayList<String> invalidIDs;

    public static ArrayList<Package> getInstalledApps(Context context) {
        ArrayList<Package> res = new ArrayList<Package>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            String name = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
            int uid = p.applicationInfo.uid;
            Drawable icon = p.applicationInfo.loadIcon(context.getPackageManager());
            res.add(new Package(name, uid, icon));
        }
        Collections.sort(res, new Package.PackageNameComparator());
        return res;
    }

    public static ArrayList<Integer> getAddedUIDs() {
        ArrayList<Integer> packageUids = new ArrayList<>();
        invalidIDs = new ArrayList<>();
        List<String> list = Shell.SU.run(COMMAND_UID_LIST);
        if (list != null) {
            for (String uid : list) {
                if (!uid.isEmpty()) {
                    try {
                        packageUids.add(Integer.parseInt(uid));
                    } catch (NumberFormatException e) {
                        invalidIDs.add(uid);
                    }
                }
            }
        }
        return packageUids;
    }

    public static void addUID(int uid) {
        new runBackgroudTask(null, false)
                .execute(String.format(COMMAND_UID_ADD, Integer.toString(uid)));
    }

    public static void removeInvalidUIDs(Context context) {
        String[] commands = new String[invalidIDs.size()];
        for (int i = 0; i < invalidIDs.size(); i++) {
            commands[i] = String.format(COMMAND_UID_REMOVE, invalidIDs.get(i));
        }
        new runBackgroudTask(context, true).execute(commands);
    }

    public static void removeUID(int uid) {
        new runBackgroudTask(null, false)
                .execute(String.format(COMMAND_UID_REMOVE, Integer.toString(uid)));
    }

    private static class runBackgroudTask extends AsyncTask<String, Void, Void> {

        Context context;
        Boolean showToast;

        runBackgroudTask(Context context, boolean showToast) {
            this.context = context;
            this.showToast = showToast;
        }

        @Override
        protected Void doInBackground(String... params) {
            Shell.SU.run(params);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (showToast){
                Toast.makeText(context, context.getString(R.string.completed_successfully), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
