package com.widevision.dollarstar.activity;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetPostRatingDao;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SetDisLikeDao;
import com.widevision.dollarstar.dao.SetLikeDao;
import com.widevision.dollarstar.dao.UploadGsonClass;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Constant;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RattingPostActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Extension extension;
    private String user_id = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeAdapter homeAdapter;
    private ListView recyclerView;
    private ArrayList<PostGsonClass.Data> dataArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratting_post);
        init();

        if (extension.executeStrategy(RattingPostActivity.this, "", ValidationTemplate.INTERNET)) {
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        extension = new Extension();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (ListView) findViewById(R.id.recycle_home_view);
        user_id = PreferenceConnector.readString(RattingPostActivity.this, PreferenceConnector.LOGIN_UserId, "");
        RelativeLayout mainContainer = (RelativeLayout) findViewById(R.id.main_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.BLACK, Color.CYAN);
    }

    @Override
    public void onRefresh() {
        if (extension.executeStrategy(RattingPostActivity.this, "", ValidationTemplate.INTERNET)) {
            getPost();
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void getPost() {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(RattingPostActivity.this);
        GetPostRatingDao getPostDao = new GetPostRatingDao();
        getPostDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            dataArrayList = result.data;
                            if (homeAdapter != null) {
                                homeAdapter.notifyDataSetChanged();
                            } else {
                                homeAdapter = new HomeAdapter();
                                recyclerView.setAdapter(homeAdapter);
                            }
                        } else {
                            showSnackBar(result.message);
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


    class HomeAdapter extends BaseAdapter {

        private final AQuery aQuery;
        private ViewHolder holder;

        public HomeAdapter() {
            aQuery = new AQuery(RattingPostActivity.this);
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
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(RattingPostActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(RattingPostActivity.this, ViewProfileActivity.class), translateBundle);
                    } else {
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(RattingPostActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(RattingPostActivity.this, ViewFriendProfileActivity.class).putExtra("user_id", dataArrayList.get(position).post_author), translateBundle);
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
                            Bundle translateBundle1 = ActivityOptions.makeCustomAnimation(RattingPostActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                            Intent intent1 = new Intent(RattingPostActivity.this, CommentListActivity.class);
                            intent1.putExtra("post_id", dataArrayList.get(position).ID);
                            startActivity(intent1, translateBundle1);
                            break;

                        case R.id.share_layout:
                            shareImage(dataArrayList.get(position).post_excerpt, holder.post_image);
                            break;

                        case R.id.post_image_view:
                            Bundle bundle = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
                            startActivity(new Intent(RattingPostActivity.this, ViewImage.class).putExtra("image_url", dataArrayList.get(position).post_excerpt), bundle);
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
            loaderHelper.showProgress(RattingPostActivity.this);
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

        private void postActionPopup(final String copyUrl) {
            final Dialog dialog = new Dialog(RattingPostActivity.this, R.style.DialogSlideAnim);
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
                    //  showSnackBar("work in progress.");
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
            Toast.makeText(RattingPostActivity.this, "Url Copied", Toast.LENGTH_SHORT).show();
        }

        class ViewHolder {
            public ImageView profile_image, post_image, likeIcon, menuIcon;
            public LinearLayout like_layout, comment_layout, share_layout;
            public TextView user_name, like_count;
        }
    }


    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }
}
