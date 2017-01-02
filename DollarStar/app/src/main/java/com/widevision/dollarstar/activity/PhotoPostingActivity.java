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
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SetPostDao;
import com.widevision.dollarstar.dao.UploadGsonClass;
import com.widevision.dollarstar.model.TestingLabel;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.MarshMallowPermission;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PhotoPostingActivity extends AppCompatActivity {

    private ImageView mBackBtn, mPostBtn, mImageView, mCamera;
    private LinearLayout mAddLocation, mAddTagPeople;
    private final int PLACE_PICKER_REQUEST = 3;
    private final int PICK_FROM_CAMERA = 2;
    private final int PICK_FROM_FILE = 1;
    private String mPhotoPath = "", addressStr = "";
    private String lat = "0", lng = "0";
    private MarshMallowPermission marshMallowPermission;
    public String photoFileName = "photo.jpg";
    public final String APP_TAG = "MyCustomApp";
    Uri file = null;
    private TextView addressTxt;
    public static String friendStr = "";
    public static ArrayList<PostGsonClass.Data> usersList;
    private ArrayList<PostGsonClass.Data> usersTagList = new ArrayList<>();
    private Extension extension;
    private LinearLayout mainRow;
    private ArrayList<LinearLayout> linearLayouts;
    private TestingLabel autoLabelUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_posting);

        mBackBtn = (ImageView) findViewById(R.id.back_BTN);
        mPostBtn = (ImageView) findViewById(R.id.action_post);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mCamera = (ImageView) findViewById(R.id.camera);
        mAddLocation = (LinearLayout) findViewById(R.id.add_location_TXT);
        mAddTagPeople = (LinearLayout) findViewById(R.id.add_tag_TXT);
        mainRow = (LinearLayout) findViewById(R.id.main);
        addressTxt = (TextView) findViewById(R.id.address);
        autoLabelUI = (TestingLabel) findViewById(R.id.label_view);

        extension = new Extension();
        marshMallowPermission = new MarshMallowPermission(PhotoPostingActivity.this);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(PhotoPostingActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mAddTagPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PhotoPostingActivity.this, TagPeopleActivity.class));
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mPhotoPath.isEmpty()) {
                    if (extension.executeStrategy(PhotoPostingActivity.this, "", ValidationTemplate.INTERNET)) {
                        setPost();
                    } else {
                        Toast.makeText(PhotoPostingActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogImageSelector();
            }
        });
        dialogImageSelector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            autoLabelUI.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (usersList != null && usersList.size() != 0) {
            for (int i = 0; i < usersList.size(); i++) {
                if (usersList.get(i).isSelected) {
                    usersTagList.add(usersList.get(i));
                    autoLabelUI.addLabel(usersList.get(i).first_name, usersList.get(i));
                }
            }
        }


        autoLabelUI.setOnRemoveLabelListener(new TestingLabel.OnRemoveLabelListener() {
            @Override
            public void onRemoveLabel(View view, int position) {

            }

            @Override
            public void onRemoveLabel(View view, Object position) {
                PostGsonClass.Data item = (PostGsonClass.Data) position;
                int index = usersList.indexOf(item);
                usersList.get(index).isSelected = false;
            }
        });
    }

    private void addItem(final int i) {
        final LinearLayout linearLayout = new LinearLayout(this);
        TextView textView = new TextView(this);
        ImageView imageView = new ImageView(this);

        textView.setTextColor(Color.BLACK);
        textView.setTextSize(15);
        textView.setText(usersList.get(i).first_name);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 0, 0);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setImageResource(R.drawable.close);
        imageView.setLayoutParams(layoutParams);

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(10, 10, 10, 10);
        linearLayout.setBackgroundColor(Color.parseColor("#f2f2f2"));
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = linearLayouts.get(i);
                mainRow.removeView(layout);
                linearLayouts.remove(i);
                usersList.get(i).isSelected = false;
            }
        });

        linearLayouts.add(linearLayout);
        mainRow.addView(linearLayout);
    }

    private void dialogImageSelector() {
        final Dialog dialog = new Dialog(PhotoPostingActivity.this);
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
                        if (!marshMallowPermission.checkPermissionForCamera()) {
                            marshMallowPermission.requestPermissionForCamera();
                        } else {
                            if (!marshMallowPermission.checkPermissionForExternalStorage()) {
                                marshMallowPermission.requestPermissionForExternalStorage();
                            } else {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                file = Uri.fromFile(getOutputMediaFile());
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
                                startActivityForResult(intent, PICK_FROM_CAMERA);
                            }
                        }

                        break;
                    case R.id.choose_gallery:
                        dialog.dismiss();
                        if (!marshMallowPermission.checkPermissionForGallery()) {
                            marshMallowPermission.requestPermissionForExternalStorage();
                        } else {
                            startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), "Choose an image"),
                                    PICK_FROM_FILE);
                        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DollarStar");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("CameraDemo", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    private static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case PICK_FROM_FILE:
                    if (resultCode == Activity.RESULT_OK) {
                        handleGalleryResult(data);
                    }
                    break;

                case PICK_FROM_CAMERA:

                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            Bitmap bitmap = decodeUri(PhotoPostingActivity.this, file, mImageView.getHeight());
                            mImageView.setImageBitmap(bitmap);
                            mPhotoPath = file.getPath();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else { // Result was a failure
                        Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                    }

                    break;

                case PLACE_PICKER_REQUEST:

                    if (resultCode == RESULT_OK) {
                        Place place = PlacePicker.getPlace(data, this);
                        String toastMsg = String.format("Place: %s", place.getAddress());
                        LatLng latLng = place.getLatLng();
                        lat = "" + latLng.latitude;
                        lng = "" + latLng.longitude;
                        addressStr = "" + place.getAddress();
                        addressTxt.setText(place.getName());
                        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGalleryResult(Intent data) {
        Uri selectedImage = data.getData();
        //mPhotoPath = getPath(selectedImage);
        mPhotoPath = getRealPathFromUri(PhotoPostingActivity.this, selectedImage);
        if (mPhotoPath != null) {
            setPic(mPhotoPath, mImageView);
        } else {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImage);
                mImageView.setImageBitmap(BitmapFactory.decodeStream(is));
                mPhotoPath = selectedImage.getPath();
                /*File file = getOutputMediaFile();
                //Convert bitmap to byte array

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 0 ignored for PNG, bos);
                byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                mPhotoPath = file.getPath();*/
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void setPic(String imagePath, ImageView destination) {
        int targetW = destination.getWidth();
        int targetH = destination.getHeight();
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        destination.setImageBitmap(bitmap);
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor;
        if (Build.VERSION.SDK_INT > 19) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, sel, new String[]{id}, null);
        } else {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setPost() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(PhotoPostingActivity.this);

        if (PhotoPostingActivity.usersList != null && PhotoPostingActivity.usersList.size() != 0) {
            boolean tag = false;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < PhotoPostingActivity.usersList.size(); i++) {
                if (PhotoPostingActivity.usersList.get(i).isSelected) {
                    tag = true;
                    result.append(PhotoPostingActivity.usersList.get(i).user_id);
                    result.append(",");
                }
            }
            if (tag) {
                friendStr = result.length() > 0 ? result.substring(0, result.length() - 1) : "";
            }
        }

        String user_id = PreferenceConnector.readString(PhotoPostingActivity.this, PreferenceConnector.LOGIN_UserId, "");
        SetPostDao setPostDao = new SetPostDao(user_id, new File(mPhotoPath), "" + lat, "" + lng, addressStr, friendStr);
        setPostDao.query(new AsyncCallback<UploadGsonClass>() {
            @Override
            public void onOperationCompleted(UploadGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        sendBroadcast(new Intent(RefreshService.REFRESH_TAG));
                        Toast.makeText(PhotoPostingActivity.this, "Post successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PhotoPostingActivity.this, result.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PhotoPostingActivity.this, getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}