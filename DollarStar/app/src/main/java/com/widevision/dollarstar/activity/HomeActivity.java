package com.widevision.dollarstar.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetPostDao;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SetDisLikeDao;
import com.widevision.dollarstar.dao.SetLikeDao;
import com.widevision.dollarstar.dao.UploadGsonClass;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Constant;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.Util;
import com.widevision.dollarstar.util.ValidationTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Extension extension;
    private ImageView mSettingBtn, mSearchBtn, mShareBtn, mRatingBtn;
    private String user_id = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeAdapter homeAdapter;
    private ListView recyclerView;
    private ArrayList<PostGsonClass.Data> dataArrayList;
    private RefreshPostReceiver refreshPostReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();

        mSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPopup();
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent, translateBundle);
            }
        });

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*dialogImageSelector();*/
                Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                Intent intent = new Intent(HomeActivity.this, PhotoPostingActivity.class);
                startActivity(intent, translateBundle);
            }
        });

        mRatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                Intent intent = new Intent(HomeActivity.this, RattingPostActivity.class);
                startActivity(intent, translateBundle);
            }
        });

        if (extension.executeStrategy(HomeActivity.this, "", ValidationTemplate.INTERNET)) {
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }


    private void addPopup() {
        final Dialog dialog = new Dialog(HomeActivity.this, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.setting_popup);
        dialog.setCanceledOnTouchOutside(true);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.action_profile:
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        Intent intent = new Intent(HomeActivity.this, ViewProfileActivity.class);
                        startActivity(intent, translateBundle);
                        break;

                    case R.id.action_share:
                        Util.actionShare(HomeActivity.this);
                        break;

                    case R.id.action_refresh:
                        getPost();
                        break;

                    case R.id.action_logout:
                        attemptLogout();
                        break;

                    case R.id.upload_BTN_popup:
                        Bundle translateBundle1 = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        Intent intent1 = new Intent(HomeActivity.this, PostActivity.class);
                        startActivity(intent1, translateBundle1);
                        break;
                }
                dialog.dismiss();
            }
        };

        LinearLayout action_profile = (LinearLayout) dialog.findViewById(R.id.action_profile);
        LinearLayout action_share = (LinearLayout) dialog.findViewById(R.id.action_share);
        LinearLayout action_refresh = (LinearLayout) dialog.findViewById(R.id.action_refresh);
        LinearLayout action_logout = (LinearLayout) dialog.findViewById(R.id.action_logout);
        ImageView cross1 = (ImageView) dialog.findViewById(R.id.cross);

        cross1.setOnClickListener(onClickListener);
        action_profile.setOnClickListener(onClickListener);
        action_share.setOnClickListener(onClickListener);
        action_refresh.setOnClickListener(onClickListener);
        action_logout.setOnClickListener(onClickListener);
        dialog.show();
    }

    private void init() {
        extension = new Extension();
        mSettingBtn = (ImageView) findViewById(R.id.setting_BTN);
        mSearchBtn = (ImageView) findViewById(R.id.search_BTN);
        mShareBtn = (ImageView) findViewById(R.id.share_BTN);
        mRatingBtn = (ImageView) findViewById(R.id.ratting_BTN);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (ListView) findViewById(R.id.recycle_home_view);
        user_id = PreferenceConnector.readString(HomeActivity.this, PreferenceConnector.LOGIN_UserId, "");

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.BLACK, Color.CYAN);

        refreshPostReceiver = new RefreshPostReceiver();
        registerReceiver(refreshPostReceiver, new IntentFilter(RefreshService.REFRESH_TAG));
        Intent intent = new Intent(HomeActivity.this, RefreshService.class);
        startService(intent);
    }

    private void getPost() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(HomeActivity.this);
        GetPostDao getPostDao = new GetPostDao(user_id);
        getPostDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            Collections.reverse(result.data);
                            dataArrayList = result.data;
                            if (homeAdapter != null) {
                                homeAdapter.notifyDataSetChanged();
                            } else {
                                homeAdapter = new HomeAdapter();
                                recyclerView.setAdapter(homeAdapter);
                            }
                        } else {
                            showSnackBar("No more post to show.");
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

    private void getPostRefresh() {
        GetPostDao getPostDao = new GetPostDao(user_id);
        getPostDao.query(    new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            Collections.reverse(result.data);
                            dataArrayList = result.data;
                            if (homeAdapter != null) {
                                homeAdapter.notifyDataSetChanged();
                            } else {
                                homeAdapter = new HomeAdapter();
                                recyclerView.setAdapter(homeAdapter);
                            }
                        }
                    }
                }
            }
        });
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        if (extension.executeStrategy(HomeActivity.this, "", ValidationTemplate.INTERNET)) {
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    class HomeAdapter extends BaseAdapter {

        private final AQuery aQuery;
        private ViewHolder holder;

        public HomeAdapter() {
            aQuery = new AQuery(HomeActivity.this);
        }

        @Override
        public int getCount() {
            return dataArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_list_row, viewGroup, false);
                holder = new ViewHolder();
                holder.profile_image = (ImageView) view.findViewById(R.id.profileIMG);
                holder.likeIcon = (ImageView) view.findViewById(R.id.like_icon);
                holder.post_image = (ImageView) view.findViewById(R.id.post_image_view);
                holder.menuIcon = (ImageView) view.findViewById(R.id.action_menu);
                holder.like_layout = (LinearLayout) view.findViewById(R.id.like_layout);
                holder.share_layout = (LinearLayout) view.findViewById(R.id.share_layout);
                holder.comment_layout = (LinearLayout) view.findViewById(R.id.comment_layout);
                holder.user_name = (TextView) view.findViewById(R.id.user_nameTXT);
                holder.like_count = (TextView) view.findViewById(R.id.like_countTXT);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            view.findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user_id.equals(dataArrayList.get(position).post_author)) {
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(HomeActivity.this, ViewProfileActivity.class), translateBundle);
                    } else {
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(HomeActivity.this, ViewFriendProfileActivity.class).putExtra("user_id", dataArrayList.get(position).post_author), translateBundle);
                    }
                }
            });

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.like_layout:
                            if (dataArrayList.get(position).post_like.isEmpty()) {
                                setLike(dataArrayList.get(position).ID);
                                dataArrayList.get(position).post_like = "" + user_id;
                                notifyDataSetChanged();
                            } else if (!Arrays.asList(dataArrayList.get(position).post_like.split(",")).contains(user_id)) {
                                setLike(dataArrayList.get(position).ID);
                                List<String> list = new LinkedList(Arrays.asList(dataArrayList.get(position).post_like.split(",")));
                                list.add(user_id);
                                dataArrayList.get(position).post_like = StringUtils.join(list, ",");
                                notifyDataSetChanged();
                            } else if (Arrays.asList(dataArrayList.get(position).post_like.split(",")).contains(user_id)) {
                                setDisLike(dataArrayList.get(position).ID);
                                List<String> list = new LinkedList(Arrays.asList(dataArrayList.get(position).post_like.split(",")));
                                list.remove(user_id);
                                dataArrayList.get(position).post_like = StringUtils.join(list, ",");
                                notifyDataSetChanged();
                            }
                            break;

                        case R.id.comment_layout:
                            Bundle translateBundle1 = ActivityOptions.makeCustomAnimation(HomeActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                            Intent intent1 = new Intent(HomeActivity.this, CommentListActivity.class);
                            intent1.putExtra("post_id", dataArrayList.get(position).ID);
                            startActivity(intent1, translateBundle1);
                            break;

                        case R.id.share_layout:
                            shareImage(dataArrayList.get(position).post_excerpt, holder.post_image);
                            break;

                        case R.id.post_image_view:
                            Bundle bundle = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
                            startActivity(new Intent(HomeActivity.this, ViewImage.class).putExtra("image_url", dataArrayList.get(position).post_excerpt), bundle);
                            break;

                        case R.id.action_menu:
                            postActionPopup(dataArrayList.get(position).post_excerpt);
                            break;
                    }
                }
            };

            holder.comment_layout.setOnClickListener(onClickListener);
            holder.like_layout.setOnClickListener(onClickListener);
            holder.share_layout.setOnClickListener(onClickListener);
            holder.post_image.setOnClickListener(onClickListener);
            holder.menuIcon.setOnClickListener(onClickListener);

            aQuery.id(holder.post_image).image(dataArrayList.get(position).post_excerpt, true, true, 300, R.drawable.placeholder, null, AQuery.FADE_IN);
            aQuery.id(holder.profile_image).image(dataArrayList.get(position).profilePic, true, true, 80, R.drawable.placeholder, null, AQuery.FADE_IN);

            if (!dataArrayList.get(position).post_like.isEmpty()) {
                String like_count[] = dataArrayList.get(position).post_like.split(",");
                holder.like_count.setText("Total Likes " + like_count.length);
                if (Arrays.asList(dataArrayList.get(position).post_like.split(",")).contains(user_id)) {
                    holder.likeIcon.setImageResource(R.drawable.likeactive);
                } else {
                    holder.likeIcon.setImageResource(R.drawable.like);
                }
            } else {
                holder.like_count.setText("");
                holder.likeIcon.setImageResource(R.drawable.like);
            }
            holder.user_name.setText(dataArrayList.get(position).first_name);

            return view;
        }

        private void shareImage(final String imageUrl, ImageView image) {
            final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
            loaderHelper.showProgress(HomeActivity.this);
            try {
                Bitmap bm = aQuery.id(image).getCachedImage(imageUrl, 0);
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "DollarStar_" + Constant.getMobileDateTime(Constant.dateTimePattern), null);
                Uri imageUri = Uri.parse(pathofBmp);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(Intent.createChooser(intent, "Share Image"));
            } catch (Exception e) {
                e.printStackTrace();
                loaderHelper.dismissProgress();
            }
            loaderHelper.dismissProgress();
        }

        private void setLike(String postId) {
            SetLikeDao setLikeDao = new SetLikeDao(user_id, postId);
            setLikeDao.query(new AsyncCallback<UploadGsonClass>() {
                @Override
                public void onOperationCompleted(UploadGsonClass result, Exception e) {
                }
            });
        }

        private void setDisLike(String postId) {
            SetDisLikeDao setLikeDao = new SetDisLikeDao(user_id, postId);
            setLikeDao.query(new AsyncCallback<UploadGsonClass>() {
                @Override
                public void onOperationCompleted(UploadGsonClass result, Exception e) {
                }
            });
        }

        class ViewHolder {
            public ImageView profile_image, post_image, likeIcon, menuIcon;
            public LinearLayout like_layout, comment_layout, share_layout;
            public TextView user_name, like_count;
        }
    }

    private void postActionPopup(final String copyUrl) {
        final Dialog dialog = new Dialog(HomeActivity.this, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.post_actions_popup);
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //showSnackBar("work in progress.");
                switch (view.getId()) {
                    case R.id.action_report:

                        break;

                    case R.id.action_faq:

                        break;

                    case R.id.action_copy_url:
                        copyToClipBord(copyUrl);
                        break;

                    case R.id.action_postnotification:

                        break;
                }
            }
        };

        TextView actionReport = (TextView) dialog.findViewById(R.id.action_report);
        TextView actionCopyUrl = (TextView) dialog.findViewById(R.id.action_copy_url);
        TextView actionPostNotification = (TextView) dialog.findViewById(R.id.action_postnotification);
        TextView actionFaq = (TextView) dialog.findViewById(R.id.action_faq);

        actionReport.setOnClickListener(onClickListener);
        actionCopyUrl.setOnClickListener(onClickListener);
        actionPostNotification.setOnClickListener(onClickListener);
        actionFaq.setOnClickListener(onClickListener);
        dialog.show();
    }

    private void copyToClipBord(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", url);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(HomeActivity.this, "Url Copied", Toast.LENGTH_SHORT).show();
    }

    private void attemptLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setMessage("Are you sure, you want to logout ?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int ii) {
                if (Constant.isMyServiceRunning(RefreshService.class, HomeActivity.this)) {
                    stopService(new Intent(HomeActivity.this, RefreshService.class));
                }
                dialogInterface.dismiss();
                PreferenceConnector.writeString(HomeActivity.this, PreferenceConnector.IS_LOGIN, "No");
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
        builder.create().show();
    }

    private class RefreshPostReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getPostRefresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(refreshPostReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Constant.isMyServiceRunning(RefreshService.class, HomeActivity.this)) {
            stopService(new Intent(HomeActivity.this, RefreshService.class));
        }
    }
}