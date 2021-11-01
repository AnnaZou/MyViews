package com.annazou.myviews.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

public class PopListMenu {
    Context mContext;
    AlertDialog mMenu;
    OnItemSelectListener mListener;
    String[] mList;

    int mGravity = -1;
    int mWidth = -1;

    public PopListMenu(Context context){
        mContext = context;
    }

    public interface OnItemSelectListener{
        void onItemSelected(DialogInterface dialog, int position);
    }

    public void setOnItemClickedListener(OnItemSelectListener listener){
        mListener = listener;
    }

    public void setListMenu(String[] list){
        mList = list;
    }

    private AlertDialog getMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(mList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onItemSelected(dialog, which);
            }
        }).setCancelable(true);
        AlertDialog dialog = builder.show();
        dialog.getListView().setPadding(0,0,0,0);
        if(mGravity != -1) {
            dialog.getWindow().setGravity(mGravity);
        }
        dialog.getWindow().setDimAmount(0);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if(mWidth != -1) {
            params.width = mWidth;
        }
        //params.x = 50;
        //params.y = 50;
        dialog.getWindow().setAttributes(params);
        return dialog;
    }

    public void setSizeGravity(int width, int gravity){
        mWidth = width;
        mGravity = gravity;
    }

    public void show(){
        if(mMenu == null){
            mMenu = getMenu();
        }
        mMenu.show();
    }
}
