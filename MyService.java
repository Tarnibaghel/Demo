package com.example.rakesh.serviceplaymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class MyService extends Service {
    String URL = "http://www.petsworld.in/blog/wp-content/uploads/2014/09/cute-adorable-kitten.jpg";
    ProgressDialog mProgressDialog;
    Context mContext;
    final String LOG_TAG = "MyService";

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException(getString(R.string.onbind));
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, R.string.service_created, Toast.LENGTH_LONG).show();
        mContext  = getApplicationContext();
        new DownloadImage().execute(URL);
    }

    private class DownloadImage extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mProgressDialog.setTitle(getString(R.string.downloading));
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... URL) {
            boolean isSaved = false;
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
                Log.e(LOG_TAG,"Bitmap : " + bitmap);

                FileOutputStream output;
                File dir = new File(Environment.getExternalStorageDirectory() + "/Piccca");
                if (!dir.exists()) {
                    boolean saved = dir.mkdir();
                    Log.v("MyService", "saved : " + saved);
                }

                File file = new File(dir, "myimage.jpg");
                Log.v("MyService", "path : " + file.getAbsolutePath());
                try {
                    output = new FileOutputStream(file);
                    isSaved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isSaved;
        }

        @Override
        protected void onPostExecute(Boolean isSaved) {
            if (isSaved) {
                //demo
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MyService.this)
                                .setSmallIcon(R.drawable.ic_help_black_24dp)
                                .setContentTitle(getString(R.string.file_saved))
                                .setContentText(getString(R.string.file_down_saved));
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
                mProgressDialog.dismiss();
                Toast.makeText(MyService.this, R.string.download_complete, Toast.LENGTH_LONG).show();
                MyService.this.stopSelf();
                Toast.makeText(MyService.this, R.string.service_destroyed, Toast.LENGTH_LONG).show();
            }
        }
    }
}
