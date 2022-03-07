package com.annazou.myviews.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.annazou.myviews.R;

import java.util.List;

public class ListDialog {
    private AlertDialog mDialog;
    private ListView mListView;
    private Callbacks mCallback;
    private BaseAdapter mAdapter;

    public interface Callbacks{
        void onItemSelected(ListDialog dialog, int which);
        void onNegativeClicked(ListDialog dialog);
    }

    public ListDialog(Context context, String title){
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

    public void show(){
        if(mAdapter == null) return;
        mDialog.show();
    }

    public void dismiss(){
        if(mDialog.isShowing()) mDialog.dismiss();
    }
}
