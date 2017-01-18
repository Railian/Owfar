package com.owfar.android.ui.preview_image;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.owfar.android.R;
import com.owfar.android.api.file.FileManager;
import com.owfar.android.api.users.ProgressListener;
import com.owfar.android.models.api.classes.Media;

@SuppressWarnings("FieldCanBeLocal")
public class UploadImageActivity extends AppCompatActivity {

    //region constants
    private static final String TAG = UploadImageActivity.class.getSimpleName();
    public static final String EXTRA_FILE_PATH = TAG + ".EXTRA_FILE_PATH";
    //endregion

    //region widgets
    private ImageView ivImage;
    private View vProgress;
    private TextView tvProgress;
    //endregion

    //region extras
    private String path;
    //endregion

    public static void start(Context context, Media media) {
        Intent intent = new Intent(context, UploadImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, media);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        ivImage = (ImageView) findViewById(R.id.activity_preview_image_ivImage);
        vProgress = findViewById(R.id.activity_preview_image_vProgress);
        tvProgress = (TextView) findViewById(R.id.activity_preview_image_tvProgress);

        path = getIntent().getParcelableExtra(EXTRA_FILE_PATH);

//
//        FileManager.get().getUploadDelegatesSet().addDelegate(media.getMediaFileId() + "" + mediaSize, progressListener);
//        MediaHelper.get().
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        initFullscreen();
//    }

    @Override
    protected void onDestroy() {
        FileManager.get().getUploadDelegatesSet().removeDelegate(progressListener);
        super.onDestroy();
    }

//    private void initFullscreen() {
//        View decorView = getWindow().getDecorView();
//        if (Build.VERSION.SDK_INT < 19) decorView.setSystemUiVisibility(View.GONE);
//        else decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }

    private ProgressListener progressListener = new ProgressListener.Simple() {
        @Override
        public void onStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vProgress.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onUpdated(final long bytesRead, final long contentLength) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvProgress.setText("Image loading..." + bytesRead * 100 / contentLength + "%");
                }
            });
        }

        @Override
        public void onFinished() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vProgress.setVisibility(View.GONE);
                }
            });
        }
    };
}
