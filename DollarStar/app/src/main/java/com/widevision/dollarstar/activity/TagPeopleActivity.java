package com.widevision.dollarstar.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GetMyFriendDao;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Constant;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import java.util.ArrayList;

public class TagPeopleActivity extends AppCompatActivity {

    private RelativeLayout mainContainer;
    private LinearLayout horiLayout;
    private ListView mListView;
    private ImageView mBackBtn, mPostBtn;
    private Extension extension;
    private ProgressLoaderHelper loaderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_people);

        mBackBtn = (ImageView) findViewById(R.id.back_BTN);
        mPostBtn = (ImageView) findViewById(R.id.action_post);
        mListView = (ListView) findViewById(R.id.list_view);
        mainContainer = (RelativeLayout) findViewById(R.id.main_container);
        horiLayout = (LinearLayout) findViewById(R.id.hori_layout);

        extension = new Extension();
        loaderHelper = ProgressLoaderHelper.getInstance();

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (extension.executeStrategy(TagPeopleActivity.this, "", ValidationTemplate.INTERNET)) {
            getUsers();
        } else {
            Constant.showAlert(mainContainer, getString(R.string.no_internet));
        }
    }


    private void getUsers() {
        loaderHelper.showProgress(TagPeopleActivity.this);
        String user_id = PreferenceConnector.readString(TagPeopleActivity.this, PreferenceConnector.LOGIN_UserId, "");
        GetMyFriendDao friendDao = new GetMyFriendDao(user_id);
        friendDao.query(new AsyncCallback<PostGsonClass>() {
            @Override
            public void onOperationCompleted(PostGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (result != null && e == null) {
                    if (result.success.equals("1")) {
                        if (result.data != null && result.data.size() != 0) {
                            PhotoPostingActivity.usersList = result.data;
                            FriendListAdapter friendListAdapter = new FriendListAdapter();
                            mListView.setAdapter(friendListAdapter);
                        } else {
                            Constant.showAlert(mainContainer, "You do not have any Followers and are not following anyone, please start following to tag members.");
                        }
                    } else {
                        Constant.showAlert(mainContainer, "You do not have any Followers and are not following anyone, please start following to tag members.");
                    }
                } else {
                    Constant.showAlert(mainContainer, "You do not have any Followers and are not following anyone, please start following to tag members");
                }
            }
        });
    }


/*  <LinearLayout
                android:id="@+id/main"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="#f2f2f2"
                android:gravity="center_vertical"
                android:orientation="horizontal"
                android:padding="5dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Deepak GEhlot"
                    android:textColor="#000000"
                    android:textSize="16sp" />

                <ImageView
                    android:id="@+id/close"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="3dp"
                    android:padding="10dp"
                    android:src="@drawable/close" />
            </LinearLayout>*/


    private class FriendListAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private LayoutInflater layoutInflater;

        public FriendListAdapter() {
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return PhotoPostingActivity.usersList.size();
        }

        @Override
        public Object getItem(int i) {
            return PhotoPostingActivity.usersList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = layoutInflater.inflate(R.layout.tag_people_row, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.mCheck = (CheckBox) view.findViewById(R.id.checkbox);
                viewHolder.mUserNameTxt = (TextView) view.findViewById(R.id.user_nameTXT);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            if (PhotoPostingActivity.usersList.get(i).first_name != null && !PhotoPostingActivity.usersList.get(i).first_name.isEmpty()) {
                viewHolder.mUserNameTxt.setText(PhotoPostingActivity.usersList.get(i).first_name);
            } else {
                viewHolder.mUserNameTxt.setText("Not available");
            }

            viewHolder.mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    PhotoPostingActivity.usersList.get(i).isSelected = b;
                    notifyDataSetChanged();
                }
            });

            viewHolder.mCheck.setChecked(PhotoPostingActivity.usersList.get(i).isSelected);
            viewHolder.mCheck.setSelected(PhotoPostingActivity.usersList.get(i).isSelected);

            return view;
        }

        private class ViewHolder {
            private TextView mUserNameTxt;
            private CheckBox mCheck;
        }
    }
}
