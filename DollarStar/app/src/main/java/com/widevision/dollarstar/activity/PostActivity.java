package com.widevision.dollarstar.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.SetPostDao;
import com.widevision.dollarstar.dao.UploadGsonClass;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.PreferenceConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class PostActivity extends AppCompatActivity {


    private final int PICK_FROM_CAMERA = 2;
    private final int PICK_FROM_FILE = 1;
    private String mPhotoPath = "";
    private ImageView mProfileIMG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        mProfileIMG = (ImageView) findViewById(R.id.image);
        mProfileIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogImageSelector();
            }
        });
        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPost();
            }
        });
    }

    private void setPost() {
        String user_id = PreferenceConnector.readString(PostActivity.this, PreferenceConnector.LOGIN_UserId, "");
        SetPostDao setPostDao = new SetPostDao(user_id, new File(mPhotoPath),"","","","");
        setPostDao.query(new AsyncCallback<UploadGsonClass>() {
            @Override
            public void onOperationCompleted(UploadGsonClass result, Exception e) {
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        Toast.makeText(PostActivity.this, result.message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showSnackBar(result.message);
                    }
                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }

    private void dialogImageSelector() {
        final Dialog dialog = new Dialog(PostActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.add_photo_dialog);
        dialog.setCanceledOnTouchOutside(true);
        ImageView choose_gallery = (ImageView) dialog.findViewById(R.id.choose_gallery);
        ImageView choose_camera = (ImageView) dialog.findViewById(R.id.choose_camera);
        ImageView cross1 = (ImageView) dialog.findViewById(R.id.cross);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.choose_camera:
                        dialog.dismiss();
                        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(Environment.getExternalStorageDirectory(), "profilepic.png");
                        // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(intent1, PICK_FROM_CAMERA);
                        break;
                    case R.id.choose_gallery:
                        dialog.dismiss();
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(intent, PICK_FROM_FILE);
                        break;
                    case R.id.cross:
                        dialog.dismiss();
                        break;
                }
            }
        };

        cross1.setOnClickListener(onClickListener);
        choose_gallery.setOnClickListener(onClickListener);
        choose_camera.setOnClickListener(onClickListener);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case PICK_FROM_FILE:
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            Uri uri = data.getData();
                            try {
                                mPhotoPath = getRealPathFromURI(uri);
                                try {
                                    File fs = new File(mPhotoPath);
                                    FileInputStream fileInputStream = new FileInputStream(fs);
                                    Bitmap realImage = BitmapFactory.decodeStream(fileInputStream);
                                    mProfileIMG.setImageBitmap(realImage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } catch (Exception ignored) {

                        }
                    }
                    break;

                case PICK_FROM_CAMERA:

                    if (resultCode == Activity.RESULT_OK) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        //    mProfieImage.setImageBitmap(photo);

                        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                        Uri tempUri = getImageUri(getApplicationContext(), photo);

                        // CALL THIS METHOD TO GET THE ACTUAL PATH
                        mPhotoPath = getRealPathFromURI2(tempUri);
                        try {
                            File fs = new File(mPhotoPath);
                            FileInputStream fileInputStream = new FileInputStream(fs);
                            Bitmap realImage = BitmapFactory.decodeStream(fileInputStream);
                            mProfileIMG.setImageBitmap(realImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI2(Uri uri) {
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
