package com.rya.droprefreshr;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rya.droprefreshr.view.RefreshListView;

import java.util.ArrayList;

public class DropRefreshr extends AppCompatActivity {

    private ArrayList<String> mListData;
    private RefreshListView lv_refresh;
    private InnerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_refreshr);

        initUI();

        initData();

        initAdapter();

    }

    private void initAdapter() {
        mAdapter = new InnerAdapter();
        lv_refresh.setAdapter(mAdapter);
    }

    private void initData() {
        mListData = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            mListData.add("云计算大数据 -- 数据号：" + i);
        }
    }

    private void initUI() {
        lv_refresh = (RefreshListView) findViewById(R.id.lv_refresh);

        lv_refresh.setOnDataRefreshing(new RefreshListView.OnDataRefreshing() {
            @Override
            public void onRefreshing() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            mListData.add(0, "新加载数据 >>> 数据模拟+");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                    lv_refresh.refreshComplete();
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onFooterRefreshing() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mListData.add("更多加载数据 >>> Footer+");
                                    mAdapter.notifyDataSetChanged();
                                    lv_refresh.footerRefreshComplete();
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }


    private class InnerAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = new TextView(getApplicationContext());
            } else {
                view = (TextView) convertView;
            }
            view.setText(mListData.get(position));
            view.setTextColor(Color.GRAY);
            view.setTextSize(20f);
            view.setHeight(90);

            return view;
        }
    }
}
