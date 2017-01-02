package com.widevision.dollarstar.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.GsonClass;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.dao.SearchDao;
import com.widevision.dollarstar.dao.SearchGsonClass;
import com.widevision.dollarstar.model.HideKeyActivity;
import com.widevision.dollarstar.model.SimpleDividerItemDecoration;
import com.widevision.dollarstar.model.VerticalSpaceItemDecoration;
import com.widevision.dollarstar.util.AsyncCallback;
import com.widevision.dollarstar.util.Constant;
import com.widevision.dollarstar.util.Extension;
import com.widevision.dollarstar.util.PreferenceConnector;
import com.widevision.dollarstar.util.ProgressLoaderHelper;
import com.widevision.dollarstar.util.ValidationTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends HideKeyActivity {

    private String user_id = "";
    private Extension extension;
    private EditText mSearchEdt;
    private RecyclerView recyclerView;
    private ArrayList<SearchGsonClass.Data> resultForSearch, tempSearchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();

        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    Constant.hideSoftKeyboard(SearchActivity.this);
                    if (!mSearchEdt.getText().toString().isEmpty()) {
                        search(mSearchEdt.getText().toString());
                    }
                }
                return false;
            }
        });

        mSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String textToGet = mSearchEdt.getText().toString().trim();
                tempSearchList = new ArrayList<>();
                if (textToGet.length() == 0) {
                    resultForSearch = new ArrayList<>();
                } else if (textToGet.length() == 1) {
                    if (extension.executeStrategy(SearchActivity.this, "", ValidationTemplate.INTERNET)) {
                        SearchDao profileSender = new SearchDao(user_id, textToGet);
                        profileSender.query(new AsyncCallback<SearchGsonClass>() {
                            @Override
                            public void onOperationCompleted(SearchGsonClass result, Exception e) {
                                if (result != null && result.data != null) {
                                    resultForSearch = result.data;
                                    recyclerView.setVisibility(View.VISIBLE);
                                    findViewById(R.id.hint_text).setVisibility(View.GONE);
                                    recyclerView.setAdapter(new HomeViewAdapter(resultForSearch));
                                    //     mNameEdt.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.search_adapter, R.id.searchText, alist));
                                }
                            }
                        });
                    }
                } else {
                    if (resultForSearch != null) {
                        for (SearchGsonClass.Data result : resultForSearch) {
                            if (StringUtils.containsIgnoreCase(result.frist_name, textToGet)) {
                                tempSearchList.add(result);
                            }
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        findViewById(R.id.hint_text).setVisibility(View.GONE);
                        recyclerView.setAdapter(new HomeViewAdapter(tempSearchList));
                        //         mNameEdt.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.search_adapter, R.id.searchText, alist));
                    }
                }
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
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        user_id = PreferenceConnector.readString(SearchActivity.this, PreferenceConnector.LOGIN_UserId, "");
        mSearchEdt = (EditText) findViewById(R.id.search_edit);
        recyclerView = (RecyclerView) findViewById(R.id.search_recycle_view);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(SearchActivity.this));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void search(String text) {
        final ProgressLoaderHelper loaderHelper = ProgressLoaderHelper.getInstance();
        loaderHelper.showProgress(SearchActivity.this);
        SearchDao searchDao = new SearchDao(user_id, text);
        searchDao.query(new AsyncCallback<SearchGsonClass>() {
            @Override
            public void onOperationCompleted(SearchGsonClass result, Exception e) {
                loaderHelper.dismissProgress();
                if (e == null && result != null) {
                    if (result.success.equals("1")) {
                        findViewById(R.id.hint_text).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new HomeViewAdapter(result.data));
                    } else {
                        showSnackBar("No user found.");
                    }
                } else {
                    showSnackBar("No user found.");
                    //showSnackBar(getString(R.string.wrong));
                }
            }
        });
    }

    private class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.ViewHolder> {

        private AQuery aQuery;
        private ArrayList<SearchGsonClass.Data> list;

        public HomeViewAdapter(ArrayList<SearchGsonClass.Data> list) {
            aQuery = new AQuery(SearchActivity.this);
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final SearchGsonClass.Data item = list.get(position);

            holder.user_name.setText(item.frist_name);
            aQuery.id(holder.profile_image).image(item.profilePic, true, true, 80, R.drawable.placeholder, null, AQuery.FADE_IN);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.user_id.equals(PreferenceConnector.readString(SearchActivity.this, PreferenceConnector.LOGIN_UserId, ""))) {
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(SearchActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(SearchActivity.this, ViewProfileActivity.class).putExtra("user_id", item.user_id), translateBundle);
                    } else {
                        Bundle translateBundle = ActivityOptions.makeCustomAnimation(SearchActivity.this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                        startActivity(new Intent(SearchActivity.this, ViewFriendProfileActivity.class).putExtra("user_id", item.user_id), translateBundle);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView profile_image;
            public TextView user_name;

            public ViewHolder(View view) {
                super(view);
                profile_image = (ImageView) view.findViewById(R.id.profileIMG);
                user_name = (TextView) view.findViewById(R.id.user_nameTXT);
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