/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.vitorpamplona.netrometer.NetrometerApplication;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class SetWallpaperAsync extends AsyncTask<Void, Integer, Void> {

    private final String WALLPAPER = "https://eyenetra.com/assets/wallpaper/eyenetra-wallpaper.png";

    String remoteURL;
    String localURL;
    Activity act;
    Uri localUri;

    public SetWallpaperAsync(String localURL, Activity act) {
        this.remoteURL = WALLPAPER;
        this.localURL = localURL;
        this.act = act;

        Toast.makeText(act, "Downloading Wallpaper...", Toast.LENGTH_SHORT).show();

        act.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            setWallpaper();
        }
    };

    public static Uri getImageContentUri(Context context, File file) {
        String filePath = file.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            Log.i("Wallpaper", "Returning Cursor");
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (file.exists()) {
                Log.i("Wallpaper", "Returning Image");
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                Log.i("Wallpaper", "Returning null");

                return null;
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setWallpaperKitkat(Uri file) {
        Intent intent = new Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER);
        String mime = "image/*";
        intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true);
        intent.setDataAndType(file, mime);
        try {
            act.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //handle error
        }
    }

    public void setWallpaper() {
        act.unregisterReceiver(onComplete);

        if (localUri != null) {
            setWallpaperKitkat(localUri);
            /*
            Log.i("Wallpaper", "Setting up...");
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(NetrometerApplication.get().getBaseContext());

            File wallpaper_file = new File(localUri);
            Uri contentURI = getImageContentUri(act.getApplicationContext(),wallpaper_file);

            Log.i("Wallpaper", "URI " + contentURI);

            ContentResolver cr = act.getContentResolver();
            Log.d("CONTENT TYPE: ", "IS: " + cr.getType(contentURI));

            myWallpaperManager.getCropAndSetWallpaperIntent(contentURI);*/
        }
    }

    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean logoExists(String fileName) {
        File f = new File(localURL + fileName);
        return f.exists();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        boolean remoteExists = exists(remoteURL);

        Uri downloadUri = Uri.parse(remoteURL);

        localUri = Uri.parse("file://" + localURL + downloadUri.getLastPathSegment());

        boolean localLogoExists = logoExists(downloadUri.getLastPathSegment());

        Log.i("Wallpaper", "Starting..." + remoteURL);

        if (remoteExists && !localLogoExists) {
            File direct = new File(localURL);

            if (!direct.exists()) {
                direct.mkdirs();
            }

            Log.i("Wallpaper", "Downloading..." + remoteURL);

            DownloadManager mgr = (DownloadManager) NetrometerApplication.get().getBaseContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Your Logo File")
                    .setDescription("Downloading new logo for the app")
                    .setDestinationUri(localUri);


            mgr.enqueue(request);
        } else {
            setWallpaper();
        }

        return null;
    }
}
