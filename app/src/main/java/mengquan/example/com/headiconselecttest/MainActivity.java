package mengquan.example.com.headiconselecttest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private ImageView mImageView;
    public static final String CROP_FILE_NAME = "crop_file.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityPermissionsDispatcher.getMultiWithPermissionCheck(this);
        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
    }

    public void selectPicture() {
        try {
            //每次选择图片吧之前的图片删除
            clearCropFile(buildUri());

            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            this.startActivityForResult(intent, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Uri buildUri() {
        if (checkSDCard()) {
            return Uri.fromFile(Environment.getExternalStorageDirectory()).buildUpon().appendPath(CROP_FILE_NAME).build();
        } else {
            return Uri.fromFile(this.getCacheDir()).buildUpon().appendPath(CROP_FILE_NAME).build();
        }
    }
    public  boolean checkSDCard() {
        String  flag = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(flag);
    }
    public boolean clearCropFile(Uri uri) {
        if (uri == null) {
            return false;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                Log.i("", "Cached crop file cleared.");
            } else {
                Log.e("", "Failed to clear cached crop file.");
            }
            return result;
        } else {
            Log.w("", "Trying to clear cached crop file but it does not exist.");
        }

        return false;
    }
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getMulti() {
        //Toast.makeText(this, "getMulti", Toast.LENGTH_SHORT).show();
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION})//一旦用户拒绝了
    public void multiDenied() {

      //  ToastUtil.toast("已拒绝一个或以上权限");
    }
    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE})//用户选择的不再询问
    public void multiNeverAsk() {
        //ToastUtil.toast("已拒绝一个或以上权限，并不再询问");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {

            case 100:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    corp( imageUri);

                }
                break;

            //截图
            case 101:
                if (resultCode == Activity.RESULT_OK && new File(buildUri().getPath()).exists()) {
                    String url = buildUri().getPath();
                    mImageView.setImageBitmap(returnBitmap(url));
                }
                break;
        }
    }

    private void corp(Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 200);
        cropIntent.putExtra("outputY", 200);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        Uri cropuri = buildUri();
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropuri);
        try {
            this.startActivityForResult(cropIntent, 101);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Bitmap returnBitmap(String url) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(url);
             bitmap = BitmapFactory.decodeStream(fis);

            return bitmap;
        } catch (Exception e){
            e.toString();

        }
        return bitmap;
    }
}
