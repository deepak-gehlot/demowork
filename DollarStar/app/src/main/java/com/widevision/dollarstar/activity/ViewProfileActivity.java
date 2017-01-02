package com.widevision.dollarstar.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetMyPostDao;
import com.widevision.dollarstar.dao.GetProfileDao;
import com.widevision.dollarstar.dao.GetTaggedPhotoDao;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.util.ArrayList;
import java.util.Collections;

public class ViewProfileActivity extends HideKeyActivity {

    private Extension extension;
    private String user_id = "";
    private int width = 0;
    private ImageView mProfileImg, mGalleryIcon, mListIcon, mMapIcon, mProfileIcon, mActionCamera;
    private TextView mUserNameTxt, post_count_TXT, mNoFollowingTXT, mNoFollowerTXT;
    private AQuery aQuery;
    private ArrayList<PostGsonClass.Data> dataArrayList = new ArrayList<>();
    private ArrayList<PostGsonClass.Data> taggedArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();

        mListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extension.executeStrategy(ViewProfileActivity.this, "", ValidationTemplate.INTERNET)) {
                    mGalleryIcon.setImageResource(R.drawable.gallery);
                    mListIcon.setImageResource(R.drawable.menuactive);
                    mProfileIcon.setImageResource(R.drawable.friendlist);
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(new HomeViewAdapter(dataArrayList, ((width / 3) + (width / 6))));
                } else {
                    showSnackBar(getString(R.string.no_internet));
                }
            }
        });
        mGalleryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extension.executeStrategy(ViewProfileActivity.this, "", ValidationTemplate.INTERNET)) {
                    mGalleryIcon.setImageResource(R.drawable.galleryactive);
                    mListIcon.setImageResource(R.drawable.menu);
                    mProfileIcon.setImageResource(R.drawable.friendlist);
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(new HomeViewAdapter(dataArrayList, ((width / 2) - (width / 6))));
                } else {
                    showSnackBar(getString(R.string.no_internet));
                }
            }
        });
        mMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extension.executeStrategy(ViewProfileActivity.this, "", ValidationTemplate.INTERNET)) {
                    Bundle translateBundle = ActivityOptions.makeCustomAnimation(ViewProfileActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                    Intent intent = new Intent(ViewProfileActivity.this, MapsActivity.class).putExtra("post_list", dataArrayList);
                    startActivity(intent, translateBundle);
                } else {
                    showSnackBar(getString(R.string.no_internet));
                }
            }
        });
        mProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extension.executeStrategy(ViewProfileActivity.this, "", ValidationTemplate.INTERNET)) {
                    getTaggedPhoto();
                } else {
                    showSnackBar(getString(R.string.no_internet));
                }
            }
        });
        mActionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle translateBundle = ActivityOptions.makeCustomAnimation(ViewProfileActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                Intent intent = new Intent(ViewProfileActivity.this, PhotoPostingActivity.class);
                startActivity(intent, translateBundle);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (extension.executeStrategy(ViewProfileActivity.this, "", ValidationTemplate.INTERNET)) {
            getProfile();
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void init() {
        setupUI(findViewById(R.id.main_container));
        extension = new Extension();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mActionCamera = (ImageView) findViewById(R.id.action_camera);
        mProfileImg = (ImageView) findViewById(R.id.profileIMG);
        mGalleryIcon = (ImageView) findViewById(R.id.gallery_icon);
        mListIcon = (ImageView) findViewById(R.id.list_icon);
        mMapIcon = (ImageView) findViewById(R.id.map_icon);
        mProfileIcon = (ImageView) findViewById(R.id.profile_icon);
        mNoFollowerTXT = (TextView) findViewById(R.id.number_of_follower);
        mNoFollowingTXT = (TextView) findViewById(R.id.number_of_following);

        mUserNameTxt = (TextView) findViewById(R.id.user_nameTXT);
        post_count_TXT = (TextView) findViewById(R.id.post_count_TXT);

        aQuery = new AQuery(ViewProfileActivity.this);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.edit_profile_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle translateBundle = ActivityOptions.makeCustomAnimation(ViewProfileActivity.this, R.anim.slide_out_left, R.anim.slide_in_left).toBundle();
                Intent i = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                startActivity(i, translateBundle);
            }
        });

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        user_id = PreferenceConnector.readString(ViewProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
    }

    private void getProfile() {

        String user_id = PreferenceConnector.readString(ViewProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
        GetProfileDao getProfileDao = new GetProfileDao(user_id);
        getProfileDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {

                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        aQuery.id(mProfileImg).image(result.data.profilePic, true, true, (width / 2), R.drawable.placeholder);
                        mUserNameTxt.setText(result.data.first_name);
                        if (result.data.followers != null && !result.data.followers.isEmpty()) {
                            String following[] = result.data.followers.split(",");
                            mNoFollowerTXT.setText("" + following.length);
                        }
                        if (result.data.following != null && !result.data.following.isEmpty()) {
                            String following[] = result.data.following.split(",");
                            mNoFollowingTXT.setText("" + following.length);
                        }
                    } else {
                        showSnackBar(result.message);
                    }
                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private void getPost() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(ViewProfileActivity.this);
        GetMyPostDao getPostDao = new GetMyPostDao(user_id);
        getPostDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            dataArrayList = result.data;
                            Collections.reverse(dataArrayList);
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(new HomeViewAdapter(dataArrayList, ((width / 2) - (width / 6))));
                            post_count_TXT.setText("" + result.data.size());
                        } else {
                            showSnackBar("No post to show.");
                        }
                    } else {
                        showSnackBar(result.message);
                    }
                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private void getTaggedPhoto() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(ViewProfileActivity.this);
        GetTaggedPhotoDao photoDao = new GetTaggedPhotoDao(user_id);
        photoDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (e == null && result != null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            mGalleryIcon.setImageResource(R.drawable.gallery);
                            mListIcon.setImageResource(R.drawable.menu);
                            mProfileIcon.setImageResource(R.drawable.friendlistactive);
                            taggedArrayList = result.data;
                            Collections.reverse(taggedArrayList);
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(new HomeViewAdapter(taggedArrayList, ((width / 2) - (width / 6))));
                        } else {
                            showSnackBar("No tagged photo to show.");
                        }
                    } else {
                        showSnackBar(getString(R.string.wrong));
                    }
                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.ViewHolder> {

        private final AQuery aQuery;
        private final ArrayList<PostGsonClass.Data> list;
        private final LinearLayout.LayoutParams layoutParams;

        public HomeViewAdapter(ArrayList<PostGsonClass.Data> list, int height) {
            aQuery = new AQuery(ViewProfileActivity.this);
            this.list = list;
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_post_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            PostGsonClass.Data item = list.get(position);
            holder.image.setLayoutParams(layoutParams);
            aQuery.id(holder.image).image(item.post_excerpt, true, true, 300, R.drawable.placeholder, null, AQuery.FADE_IN);
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final ImageView image;


            public ViewHolder(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.image);
            }
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}