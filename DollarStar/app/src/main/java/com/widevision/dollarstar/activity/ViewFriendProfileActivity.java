package com.widevision.dollarstar.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SetFollowerDao;
import com.widevision.dollarstar.dao.UnfollowDao;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.util.ArrayList;
import java.util.Arrays;

public class ViewFriendProfileActivity extends HideKeyActivity {

    private Extension extension;
    private String user_id = "";
    private int width = 0;
    private ImageView mProfileImg, mGalleryIcon, mListIcon, mMapIcon, mProfileIcon;
    private TextView mUserNameTxt, post_count_TXT, mFollowTXT, mNoFollowingTXT, mNoFollowerTXT;
    private AQuery aQuery;
    private ArrayList<PostGsonClass.Data> dataArrayList = new ArrayList<>();
    private String following[];
    private String profileUrl = "", userName = "";
    private LinearLayout followContainer;
    private ImageView followImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();

        if (extension.executeStrategy(ViewFriendProfileActivity.this, "", ValidationTemplate.INTERNET)) {
            getProfile();
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }

        mListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGalleryIcon.setImageResource(R.drawable.gallery);
                mListIcon.setImageResource(R.drawable.menuactive);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(new HomeViewAdapter(dataArrayList, ((width / 3) + (width / 6))));
            }
        });
        mGalleryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGalleryIcon.setImageResource(R.drawable.galleryactive);
                mListIcon.setImageResource(R.drawable.menu);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view_mypost);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(new HomeViewAdapter(dataArrayList, ((width / 2) - (width / 6))));
            }
        });

        mMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("Work in progress.");
            }
        });

        mProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("work in progress");
            }
        });
    }

    private void init() {
        setupUI(findViewById(R.id.main_container));
        extension = new Extension();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mProfileImg = (ImageView) findViewById(R.id.profileIMG);
        mGalleryIcon = (ImageView) findViewById(R.id.gallery_icon);
        mListIcon = (ImageView) findViewById(R.id.list_icon);
        mMapIcon = (ImageView) findViewById(R.id.map_icon);
        mProfileIcon = (ImageView) findViewById(R.id.profile_icon);
        followImage = (ImageView) findViewById(R.id.image);

        followContainer = (LinearLayout) findViewById(R.id.container_follow_btn);
        mUserNameTxt = (TextView) findViewById(R.id.user_nameTXT);
        mFollowTXT = (TextView) findViewById(R.id.edit_profile_BTN);
        post_count_TXT = (TextView) findViewById(R.id.post_count_TXT);
        mNoFollowerTXT = (TextView) findViewById(R.id.number_of_follower);
        mNoFollowingTXT = (TextView) findViewById(R.id.number_of_following);
        findViewById(R.id.action_camera).setVisibility(View.GONE);
        aQuery = new AQuery(ViewFriendProfileActivity.this);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mMapIcon.setVisibility(View.GONE);
        mProfileIcon.setVisibility(View.GONE);
        findViewById(R.id.action_layout).setVisibility(View.GONE);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getString("user_id");
        }

        mFollowTXT.setText("Follow");
        followImage.setVisibility(View.VISIBLE);
        followImage.setImageResource(R.drawable.pluse_follow);
        followImage.setColorFilter(getResources().getColor(R.color.golden));
        followContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mFollowTXT.getText().toString().equals("Following")) {
                    if (following != null && following.length != 0) {
                        mNoFollowerTXT.setText("" + (following.length + 1));
                    } else {
                        mNoFollowerTXT.setText("1");
                    }
                    mFollowTXT.setText("Following");
                    followImage.setVisibility(View.VISIBLE);
                    followContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_profile_gradient_selected));
                    mFollowTXT.setTextColor(Color.WHITE);
                    followImage.setImageResource(R.drawable.right_follow);
                    followImage.setColorFilter(Color.parseColor("#ffffff"));
                    String login_user_id = PreferenceConnector.readString(ViewFriendProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
                    SetFollowerDao setFollowerDao = new SetFollowerDao(login_user_id, user_id);
                    setFollowerDao.query(new AsyncCallback<GsonClass>() {
                        @Override
                        public void onOperationCompleted(GsonClass result, Exception e) {
                        }
                    });
                } else {
                    unfollowPopup();
                }
            }
        });
    }

    private void getProfile() {
        GetProfileDao getProfileDao = new GetProfileDao(user_id);
        getProfileDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {

                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        profileUrl = result.data.profilePic;
                        userName = result.data.first_name;
                        aQuery.id(mProfileImg).image(result.data.profilePic, true, true, 0, R.drawable.placeholder, null, AQuery.FADE_IN);
                        mUserNameTxt.setText(result.data.first_name);
                        if (result.data.followers != null && !result.data.followers.isEmpty()) {
                            following = result.data.followers.split(",");
                            mNoFollowerTXT.setText("" + following.length);
                            if (Arrays.asList(following).contains(PreferenceConnector.readString(ViewFriendProfileActivity.this, PreferenceConnector.LOGIN_UserId, ""))) {
                                mFollowTXT.setText("Following");
                                followImage.setVisibility(View.VISIBLE);
                                followContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_profile_gradient_selected));
                                mFollowTXT.setTextColor(Color.WHITE);
                                followImage.setImageResource(R.drawable.right_follow);
                                followImage.setColorFilter(Color.parseColor("#ffffff"));
                            }
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
        loaderHelper.showProgress(ViewFriendProfileActivity.this);
        GetMyPostDao getPostDao = new GetMyPostDao(user_id);
        getPostDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            dataArrayList = result.data;
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

    private void unfollowPopup() {
        final Dialog dialog = new Dialog(ViewFriendProfileActivity.this, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.unfollow_popup);
        dialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView messageTxt = (TextView) dialog.findViewById(R.id.message_txt);
        TextView cancelTxt = (TextView) dialog.findViewById(R.id.action_close);
        TextView unfollowTxt = (TextView) dialog.findViewById(R.id.action_unfollow);
        ImageView profileImg = (ImageView) dialog.findViewById(R.id.profileIMG);
        AQuery aQuery = new AQuery(ViewFriendProfileActivity.this);
        aQuery.id(profileImg).image(profileUrl, true, true, 100, R.drawable.placeholder);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.action_close:
                        dialog.dismiss();
                        break;
                    case R.id.action_unfollow:
                        dialog.dismiss();
                        unFollow();
                        mFollowTXT.setText("Follow");
                        mFollowTXT.setTextColor(getResources().getColor(R.color.golden));
                        followImage.setVisibility(View.VISIBLE);
                        followImage.setImageResource(R.drawable.pluse_follow);
                        followImage.setColorFilter(getResources().getColor(R.color.golden));
                        followContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_profile_gradient));
                        break;
                }
            }
        };

        messageTxt.setText(Html.fromHtml("Unfollow <b>@" + userName + "</b>?"));
        cancelTxt.setOnClickListener(onClickListener);
        unfollowTxt.setOnClickListener(onClickListener);

        dialog.show();
    }


    private void unFollow() {
        String loginId = PreferenceConnector.readString(ViewFriendProfileActivity.this, PreferenceConnector.LOGIN_UserId, "");
        UnfollowDao unfollowDao = new UnfollowDao(loginId, user_id);
        unfollowDao.query(new AsyncCallback<GsonClass>() {
            @Override
            public void onOperationCompleted(GsonClass result, Exception e) {
                if (result != null && e == null) {

                } else {
                    showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.ViewHolder> {

        private final AQuery aQuery;
        private final ArrayList<PostGsonClass.Data> list;
        private final LinearLayout.LayoutParams layoutParams;

        public HomeViewAdapter(ArrayList<PostGsonClass.Data> list, int height) {
            aQuery = new AQuery(ViewFriendProfileActivity.this);
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
            //holder.image.setLayoutParams(layoutParams);
            aQuery.id(holder.image).image(item.post_excerpt, true, true, (width / 2), R.drawable.placeholder, null, AQuery.FADE_IN);
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
                image.setLayoutParams(layoutParams);
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