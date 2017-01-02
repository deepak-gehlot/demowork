package com.widevision.dollarstar.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetCommentDao;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SetCommentDao;
import com.widevision.dollarstar.dao.UploadGsonClass;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.util.ArrayList;

public class CommentListActivity extends HideKeyActivity {

    private Extension extension;
    private String user_id = "", post_id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);
        init();

        Bundle b = getIntent().getExtras();
        post_id = b.getString("post_id");
        user_id = PreferenceConnector.readString(CommentListActivity.this, PreferenceConnector.LOGIN_UserId, "");

        if (extension.executeStrategy(CommentListActivity.this, "", ValidationTemplate.INTERNET)) {
            getCommentList(user_id, post_id);
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void init() {
        extension = new Extension();
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


        TextView sendTxt = (TextView) findViewById(R.id.send_TXT);
        final EditText commentEdt = (EditText) findViewById(R.id.comment_EDT);
        sendTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentStr = commentEdt.getText().toString().trim();
                if (!commentStr.isEmpty()) {
                    attemptSendComment(commentStr);
                    commentEdt.setText("");
                    PostGsonClass.Data data = new PostGsonClass.Data();
                    data.comment_content = commentStr;
                }
            }
        });
    }

    private void attemptSendComment(String comment) {
        if (extension.executeStrategy(CommentListActivity.this, "", ValidationTemplate.INTERNET)) {
            SetCommentDao setCommentDao = new SetCommentDao(user_id, post_id, comment);
            setCommentDao.query(new AsyncCallback<UploadGsonClass>() {
                @Override
                public void onOperationCompleted(UploadGsonClass result, Exception e) {
                    if (result != null && e == null) {
                        if (!result.success.equals("1")) {
                            showSnackBar(result.message);
                        } else {
                            getCommentList(user_id, post_id);
                        }
                    } else {
                        showSnackBar(getString(R.string.wrong));
                    }
                }
            });
        } else {
            showSnackBar(getString(R.string.no_internet));
        }
    }

    private void getCommentList(String user_id, String post_id) {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(CommentListActivity.this);
        GetCommentDao getCommentDao = new GetCommentDao(user_id, post_id);
        getCommentDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_comment_view);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(new HomeViewAdapter(result.data));
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

    class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.ViewHolder> {

        private final AQuery aQuery;
        private final ArrayList<PostGsonClass.Data> list;

        public HomeViewAdapter(ArrayList<PostGsonClass.Data> list) {
            aQuery = new AQuery(CommentListActivity.this);
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            PostGsonClass.Data item = list.get(position);

            holder.comment_text.setText(item.comment_content);
            if (!item.comment_date.isEmpty()) {
                String date[] = item.comment_date.split(" ");
                holder.time_text.setText(date[0]);
            }
            holder.user_name.setText(item.comment_author);
            aQuery.id(holder.profile_image).image(item.profilePic, true, true, 80, R.drawable.placeholder, null, AQuery.FADE_IN);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final ImageView profile_image;
            public final TextView user_name;
            public final TextView time_text;
            public final TextView comment_text;

            public ViewHolder(View view) {
                super(view);
                profile_image = (ImageView) view.findViewById(R.id.profileIMG);
                user_name = (TextView) view.findViewById(R.id.user_nameTXT);
                time_text = (TextView) view.findViewById(R.id.timeTXT);
                comment_text = (TextView) view.findViewById(R.id.commentTXT);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG).show();
    }
}
