package com.annazou.myviews.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.annazou.myviews.R;

public class ListDialog {
    private Context mContext;
    private AlertDialog mDialog;
    private ListView mListView;
    private Callbacks mCallback;
    private BaseAdapter mAdapter;

    private String mTitle;

    public interface Callbacks{
        void onItemSelected(ListDialog dialog, int which);
        void onNegativeClicked(ListDialog dialog);
    }

    public interface AdapterCallback{
        int getCount();
        void onGetView(TextView item, int position);
    }

    public ListDialog(Context context, String title){
        mContext = context;
        mTitle = title;
        final View addView = LayoutInflater.from(context).inflate(R.layout.list_dialog, null);
        mListView = addView.findViewById(R.id.list_dialog_list);

        mListView.setDivider(null);
        mListView.setDividerHeight(0);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onItemSelected(ListDialog.this, position);
            }
        });

        Button negative = addView.findViewById(R.id.list_dialog_negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onNegativeClicked(ListDialog.this);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setView(addView);
        mDialog = builder.create();
    }

    public AlertDialog getDialog(){
        return mDialog;
    }

    public void setCallback(Callbacks callback){
        mCallback = callback;
    }

    public void setListAdapter(BaseAdapter adapter){
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
    }

    public void setListAdapter(AdapterCallback callback){
        setListAdapter(new ListAdapter(callback));
    }

    public void show(){
        if(mAdapter == null) return;
        mDialog.show();
    }

    public void dismiss(){
        if(mDialog.isShowing()) mDialog.dismiss();
    }

    private class ListAdapter extends BaseAdapter{
        AdapterCallback adapterCallback;
        public ListAdapter(AdapterCallback callback){
            adapterCallback = callback;
        }

        @Override
        public int getCount() {
            return adapterCallback.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_dialog_item, null);
            }
            TextView name = convertView.findViewById(R.id.list_item_name);
            adapterCallback.onGetView(name, position);
            return convertView;
        }
    }
}
