package com.annazou.myviews.views;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.annazou.myviews.R;

public class InputDialog {
    private AlertDialog mDialog;
    EditText mInputText;
    TextView mWarnText;
    Callbacks mCallback;

    public interface Callbacks{
        void onInputChanged(InputDialog dialog, CharSequence s);
        void onPositiveClicked(InputDialog dialog);
        void onNegativeClicked(InputDialog dialog);
    }

    public InputDialog(Context context, String title){
        final View addView = LayoutInflater.from(context).inflate(R.layout.input_dialog, null);
        mInputText = addView.findViewById(R.id.input_dialog_text);
        mWarnText = addView.findViewById(R.id.input_dialog_warn);

        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mCallback != null){
                    mCallback.onInputChanged(InputDialog.this, s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button positive = addView.findViewById(R.id.input_dialog_positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onPositiveClicked(InputDialog.this);
                }
            }
        });

        Button negative = addView.findViewById(R.id.input_dialog_negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onNegativeClicked(InputDialog.this);
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

    public String getInputText(){
        return mInputText.getText().toString();
    }

    public void setWarnText(String warn){
        mWarnText.setText(warn);
    }

    public void setCallback(Callbacks callback){
        mCallback = callback;
    }

    public EditText getInputTextView(){
        return mInputText;
    }
}
