package com.esajee.live;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.esajee.accounts.DividerItemDecoration;
import com.esajee.accounts.GeneralRecyclerAdapter;
import com.esajee.accounts.GetJsonResponse;
import com.esajee.accounts.JsonResponseDelegete;
import com.esajee.accounts.ListData;
import com.esajee.accounts.Url;
import com.esajee.accounts.ViewHolderRecycler;
import com.esajeespecial.main.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Syed Irfan on 3/7/2016.
 */
public class SingleListSearchFragment extends ParentFragment {

    @view
    public RecyclerView searchList;
    @view
    CardView centerProgressCard;
    TabHost tabHost;

    public RelativeLayout progressLayout;

    View myFragmentView;

    LinearLayoutManager mLayoutManager;
    public String searchCategory = "";
    public String searchQuery = "";
    protected String tabCategory = "";
    String toSaveUrl = "";
    boolean viewCreated = false;
    int page = 0;
    protected boolean isEnd = false;
    protected boolean isAppend = false;
    boolean isLoading = false;
    JSONObject params;

    GeneralRecyclerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.singlelist_search_fragment, container, false);
        initAnnotation(this);
        progressLayout = (RelativeLayout) myFragmentView.findViewById(R.id.progressLayout);
        centerProgressCard = (CardView) myFragmentView.findViewById(R.id.centerProgressCard);
        searchList = (RecyclerView) myFragmentView.findViewById(R.id.searchList);
        tabHost=(TabHost)myFragmentView.findViewById(R.id.tabHost);
        tabHost.setup();

        return myFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!viewCreated) {
            onNothingSelected();
            page = 0;
            getItems(searchQuery);
            viewCreated = true;
        }
    }

    protected void getItems(String search) {
        isLoading = true;
        String pageString = "";
        try {

            params = new JSONObject();
            pageString = "&pageStart=" + page + "&pageEnd=" + (page + 10);
            params.put("pageStart", page);
            page = page + 10;
            params.put("pageEnd", page + 10);
            //   String filter = getArguments().getString("json");

            if (!search.equals("")) {
                params.put("searchText", search);
                search = "&searchText=" + URLEncoder.encode(search, "UTF-8");

            }

            Log.i("ViewItem searchItem", params.toString());
            Log.i("ViewItem params", params.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // progressBar.setVisibility(View.GONE);
        }
        if (!isEnd) {
            toSaveUrl = Url.mainSearchUrl + search + searchCategory + tabCategory + pageString;
            Log.i("ViewItems S url", toSaveUrl);
            new GetJsonResponse(toSaveUrl, new JsonResponseDelegete() {

                @Override
                public void onResponse(String json) {
                    // TODO Auto-generated method stub
                    progressLayout.setVisibility(View.GONE);
                    centerProgressCard.setVisibility(View.GONE);
                    setItems(json);
                    Log.i("ViewItem data", json);
                    isLoading = false;
                }

            }, getActivity(), false);
        }
    }

    protected void setItems(String json) {

        try {
            isEnd = false;
            if (!isAppend) {
                data = new ArrayList<ListData<JSONObject>>();
            }
            JSONArray dataArray = new JSONArray(json);
            if (dataArray.length() < 10) {
                isEnd = true;
            }
            System.out.println("items : " + json);
            for (int i = 0; i < dataArray.length(); i++) {
                try {
                    JSONObject item = (JSONObject) dataArray.get(i);
                    if (item != null) {
                        data.add(new ListData<JSONObject>(item));
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.i("ViewItem SetIem", e.toString());
                    e.printStackTrace();
                }
            }
            if (data.size() > 100) {
                isEnd = true;
            }

            initData(data, isAppend);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void initData(ArrayList<ListData<JSONObject>> arr, boolean onlyNotify) {
        // TODO Auto-generated method stub
        // super.initData(arr, onlyNotify);
        centerProgressCard.setVisibility(View.GONE);
        mLayoutManager = new LinearLayoutManager(getActivity());
        searchList.setLayoutManager(mLayoutManager);


        if (mAdapter == null) {
            mAdapter = new GeneralRecyclerAdapter(arr, this);
            searchList.setAdapter(mAdapter);
            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
            searchList.addItemDecoration(itemDecoration);
            centerProgressCard.setVisibility(View.GONE);
        } else {
            if (!onlyNotify) {
                mAdapter.setData(arr);
                searchList.setAdapter(mAdapter);
                //  gridView.setAdapter(adapter);

                // showToast("not only notified");
            }
            if (isEnd) {
                progressLayout.setVisibility(View.GONE);
            }
            mAdapter.notifyDataSetChanged();

            // gridView.setEmptyView(null);
        }
    }

    protected void loadData(String query) {
        // TODO Auto-generated method stub
        Log.i("ViewItems", query);

        if (query.length() == 0) {
            this.searchQuery = "";
            if (!isLoading) {
                isLoading = true;
                getItems(query);
            }
            return;
        }
        if (!this.searchQuery.equals(query)) {
            page = 0;
            isAppend = false;
        }
        if (!isLoading) {
            isLoading = true;
            NewSearchItem fr = (NewSearchItem) this.getParentFragment();
            fr.searchItem = query;
            getItems(query);
        }

    }


    public RecyclerView.ViewHolder setRecyclerLayout(ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlelist_custom_item, null);
        ViewHolderRecycler holder = new ViewHolderRecycler(view, getActivity());
        return holder;
    }

    public void getRcyclerItem(Object data, RecyclerView.ViewHolder holderArrive, final int position) {
        final ViewHolderRecycler holder = (ViewHolderRecycler) holderArrive;
        ListData itemData = (ListData) data;
        try {
            String imageUrl = ((JSONObject) itemData.data).optString("image");
            if (((JSONObject) itemData.data).has(getString(R.string.cloudnaryImageTag))) {
                imageUrl = ((JSONObject) itemData.data).optString(getString(R.string.cloudnaryImageTag));
            } else if (imageUrl.equals("")) {
                imageUrl = Url.mdevAddress + "images/image-not-found.jpg";
            }

            Picasso.with(getActivity()).load(imageUrl).into(holder.vhImageViews.get(string(R.string.tag_primary_imageView)));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
