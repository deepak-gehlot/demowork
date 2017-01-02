package com.widevision.dollarstar.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.widevision.dollarstar.R;
import com.widevision.dollarstar.SweetAlert.SweetAlertDialog;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.RegistrationDao;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class RegistrationActivity extends HideKeyActivity {

    private EditText mNameEDT, mEmailEDT, mContactEDT, mPasswordEDT, mConfirmPasswordEDT;
    private Spinner mGendetSPNR;
    private ImageView mProfileIMG;

    private Extension extension;

    private final int PICK_FROM_CAMERA = 2;
    private final int PICK_FROM_FILE = 1;
    private String mPhotoPath = "";

    private final String[] items = {"Male", "Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        init();
    }

    private void init() {
        setupUI(findViewById(R.id.main_container));
        extension = new Extension();
        mNameEDT = (EditText) findViewById(R.id.name_EDT);
        mEmailEDT = (EditText) findViewById(R.id.email_EDT);
        mPasswordEDT = (EditText) findViewById(R.id.password_EDT);
        mConfirmPasswordEDT = (EditText) findViewById(R.id.confirm_password_EDT);
        mContactEDT = (EditText) findViewById(R.id.contact_number_EDT);
        mProfileIMG = (ImageView) findViewById(R.id.profileIMG);
        mGendetSPNR = (Spinner) findViewById(R.id.gender_SPNR);

        mGendetSPNR.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, items) {
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
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
       /* findViewById(R.id.profileIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogImageSelector();
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    /*--method for on submit button click event--*/
    public void onClickSubmitButton(View view) {
        String name = mNameEDT.getText().toString();
        String email = mEmailEDT.getText().toString();
        String password = mPasswordEDT.getText().toString();
        String confirmPassword = mConfirmPasswordEDT.getText().toString();
        String number = mContactEDT.getText().toString();
        String gender = (String) mGendetSPNR.getSelectedItem();
        if (validate(name, email, password, confirmPassword, gender, number)) {
            //String[] userName = email.split("@");
            attamptSubmit(name, email, email, password, gender, number);
        }
    }

    private void attamptSubmit(String name, String email, String userName, String password, String gender, String number) {
        final ProgressLoaderHelper dialog = ProgressLoaderHelper.getInstance();
        dialog.showProgress(RegistrationActivity.this);
        RegistrationDao registrationDao = new RegistrationDao(name, userName, email, gender, password, number);
        registrationDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {
                dialog.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.IS_LOGIN, "Yes");
                        PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.LOGIN_UserId, result.id);
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(RegistrationActivity.this, R.anim.slide_out_left, R.anim.slide_in_left).toBundle();
                        Intent i = new Intent(RegistrationActivity.this, HomeActivity.class);
                        startActivity(i, translateBundle);
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

    private void dialogImageSelector() {
        final Dialog dialog = new Dialog(RegistrationActivity.this);
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

    private boolean validate(String name, String email, String password, String confirmPassword, String gender, String number) {
        boolean valid = false;
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender.isEmpty() || number.isEmpty()) {
            showSnackBar(getString(R.string.all_field_requied));
        } else if (name.isEmpty()) {
            showSnackBar("Name required.");
        } else if (email.isEmpty()) {
            showSnackBar("Email required.");
        } else if (!extension.executeStrategy(RegistrationActivity.this, email, ValidationTemplate.EMAIL)) {
            showSnackBar("Enter valid email id.");
        } else if (password.isEmpty()) {
            showSnackBar("Enter password.");
        } else if (confirmPassword.isEmpty()) {
            showSnackBar("Confirm password.");
        } else if (!confirmPassword.equals(password)) {
            showSnackBar("Password not match.");
        } else if (password.length() < 4) {
            showSnackBar("Password length should be at least 4 digits");
        } else if (gender.isEmpty() || gender.equals("I am")) {
            showSnackBar("Select gender.");
        } else if (number.isEmpty() || number.length() < 9) {
            showSnackBar("Contact number length should be at least 10 digits.");
        } else if (number.isEmpty() || !extension.executeStrategy(RegistrationActivity.this, number, ValidationTemplate.isnumber)) {
            showSnackBar("Enter valid contact number.");
        } else if (!extension.executeStrategy(RegistrationActivity.this, "", ValidationTemplate.INTERNET)) {
            showSnackBar(getString(R.string.no_internet));
        } else {
            valid = true;
        }
        return valid;
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
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
}
