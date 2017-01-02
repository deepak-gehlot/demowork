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
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetProfileDao;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.UpdateProfileDao;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.MarshMallowPermission;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class EditProfileActivity extends HideKeyActivity {

    private EditText mNameEDT, mContactEDT;
    private Spinner mGendetSPNR;
    private ImageView mProfileIMG;

    private Extension extension;
    private AQuery aQuery;

    private final int PICK_FROM_CAMERA = 2;
    private final int PICK_FROM_FILE = 1;
    private String mPhotoPath = "";
    private MarshMallowPermission marshMallowPermission;
    private final String[] items = {"Male", "Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        init();

        if (extension.executeStrategy(EditProfileActivity.this, "", ValidationTemplate.INTERNET)) {
            getProfile();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void init() {
        setupUI(findViewById(R.id.main_container));
        extension = new Extension();
        aQuery = new AQuery(EditProfileActivity.this);
        mNameEDT = (EditText) findViewById(R.id.name_EDT);
        mContactEDT = (EditText) findViewById(R.id.contact_number_EDT);
        mProfileIMG = (ImageView) findViewById(R.id.profileIMG);
        mGendetSPNR = (Spinner) findViewById(R.id.gender_SPNR);
        marshMallowPermission = new MarshMallowPermission(EditProfileActivity.this);
        mGendetSPNR.setAdapter(new ArrayAdapter<String>(EditProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, items) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.text_color));
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.parseColor("#3C4D5C"));
                return view;
            }
        });

        TextView back = (TextView) findViewById(R.id.back_BTN);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.choose_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogImageSelector();
            }
        });
    }

    private void getProfile() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(EditProfileActivity.this);
        String user_id = PreferenceConnector.readString(EditProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
        GetProfileDao getProfileDao = new GetProfileDao(user_id);
        getProfileDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        setProfile(result.data);
                    } else {
                        showSnackBar(result.message);
                    }
                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private void setProfile(GsonClass.Data data) {
        mNameEDT.setText(data.first_name);
        mContactEDT.setText(data.phone);
        int pos = Arrays.asList(items).indexOf(data.gender);
        mGendetSPNR.setSelection(pos);
        aQuery.id(mProfileIMG).image(data.profilePic, true, true, 0, R.drawable.placeholder, null, AQuery.FADE_IN);
    }

    private void handleGalleryResult(Intent data) {
        Uri selectedImage = data.getData();
        //mPhotoPath = getPath(selectedImage);
        mPhotoPath = getRealPathFromUri(EditProfileActivity.this, selectedImage);
        if (mPhotoPath != null) {
            setPic(mPhotoPath, mProfileIMG);
        } else {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImage);
                mProfileIMG.setImageBitmap(BitmapFactory.decodeStream(is));
                mPhotoPath = selectedImage.getPath();
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


    /*--method for on submit button click event--*/
    public void onClickSubmitButton(View view) {
        String name = mNameEDT.getText().toString();

        String number = mContactEDT.getText().toString();
        String gender = (String) mGendetSPNR.getSelectedItem();

        if (validate(name, gender, number)) {
            attamptSubmit(name, gender, number);
        }
    }

    private void attamptSubmit(String name, String gender, String number) {
        final ProgressLoaderHelper dialog = ProgressLoaderHelper.getInstance();
        dialog.showProgress(EditProfileActivity.this);
        String user_id = PreferenceConnector.readString(EditProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
        UpdateProfileDao registrationDao;
        if (!mPhotoPath.isEmpty()) {
            registrationDao = new UpdateProfileDao(user_id, name, gender, number, new File(mPhotoPath));
        } else {
            registrationDao = new UpdateProfileDao(user_id, name, gender, number);
        }
        registrationDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {
                dialog.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        Toast.makeText(EditProfileActivity.this, "Profile update successfully.", Toast.LENGTH_SHORT).show();
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

    private boolean validate(String name, String gender, String number) {
        boolean valid = false;
        if (name.isEmpty() || gender.isEmpty() || number.isEmpty()) {
            showSnackBar(getString(R.string.all_field_requied));
        } else if (name.isEmpty()) {
            showSnackBar("Name required.");
        } else if (gender.isEmpty() || gender.equals("I am")) {
            showSnackBar("Select gender.");
        } else if (number.isEmpty() || !extension.executeStrategy(EditProfileActivity.this, number, ValidationTemplate.isnumber)) {
            showSnackBar("Enter valid contact number.");
        } else if (!extension.executeStrategy(EditProfileActivity.this, "", ValidationTemplate.INTERNET)) {
            showSnackBar(getString(R.string.no_internet));
        } else {
            valid = true;
        }
        return valid;
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }

    private void dialogImageSelector() {
        final Dialog dialog = new Dialog(EditProfileActivity.this);
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
                                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                File f = new File(Environment.getExternalStorageDirectory(), "profilepic.png");
                                // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                startActivityForResult(intent1, PICK_FROM_CAMERA);
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