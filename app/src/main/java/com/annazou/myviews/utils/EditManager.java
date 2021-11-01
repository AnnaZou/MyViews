package com.annazou.myviews.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class EditManager {
    private static final int MAX_RECORD = 20;

    private Editable mText;
    private EditText mEditTextView;

    private List<EditRecord> mRecord;
    private int mCurState;

    private EditRecord mNewRecord;
    private boolean mApplyingRecord;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mNewRecord = new EditRecord();
            mNewRecord.start = start;
            mNewRecord.toBeReplace = s.subSequence(start, start + count);
            mNewRecord.after = after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mNewRecord.replace = s.subSequence(start, start + count);
            mNewRecord.before = before;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!mApplyingRecord) {
                boolean isContinueDelete = false;
                if(mCurState >= 0 && mCurState == mRecord.size() - 1){
                    EditRecord lastRecord = mRecord.get(mCurState);
                    if(mNewRecord.start + mNewRecord.before == lastRecord.start && lastRecord.after == 0 && mNewRecord.after == 0){
                        lastRecord.start = mNewRecord.start;
                        lastRecord.before = lastRecord.before + mNewRecord.before;
                        lastRecord.after = 0;
                        String oldText = lastRecord.toBeReplace.toString() + mNewRecord.toBeReplace.toString();
                        lastRecord.toBeReplace = oldText;
                        mRecord.set(mCurState, lastRecord);
                        isContinueDelete = true;
                    }
                }
                if(!isContinueDelete) {
                    addRecord(mNewRecord);
                }
                mText = s;
            } else {
                mApplyingRecord = false;
            }
        }
    };

    public interface EditRecordCallback{
        void onRedoEnabled(boolean enable);
        void onUndoEnabled(boolean enable);
    }
    private EditRecordCallback mCallback;

    public EditManager(EditText editText){
        mEditTextView = editText;
        mText = mEditTextView.getText();
        mCurState = -1;
        mRecord = new ArrayList<>();
        mEditTextView.addTextChangedListener(mTextWatcher);
    }

    public void addRecord(EditRecord record){
        if(mRecord.size() > 0 && mCurState < mRecord.size() - 1){
            for(int i = mRecord.size() - 1; i > mCurState; i--){
                mRecord.remove(i);
            }
        }
        if (mRecord.size() == MAX_RECORD){
            mRecord.remove(0);
        }
        mRecord.add(record);
        mCurState = mRecord.size() - 1;
        if(mCallback != null){
            mCallback.onUndoEnabled(true);
            mCallback.onRedoEnabled(false);
        }
    }

    public int undo(){
        mApplyingRecord = true;
        EditRecord record = mRecord.get(mCurState);
        mText = mText.replace(record.start, record.start + record.after, record.toBeReplace);
        if(mCallback != null){
            if(mCurState == 0) mCallback.onUndoEnabled(false);
            mCallback.onRedoEnabled(true);
        }
        mCurState--;
        mEditTextView.setSelection(record.start + record.before);
        return record.start;
    }

    public int redo(){
        mApplyingRecord = true;
        mCurState++;
        EditRecord record = mRecord.get(mCurState);
        mText = mText.replace(record.start, record.start + record.before, record.replace);
        if(mCallback != null){
            if(mCurState == mRecord.size() - 1) mCallback.onRedoEnabled(false);
            mCallback.onUndoEnabled(true);
        }
        mEditTextView.setSelection(record.start + record.after);
        return record.start;
    }

    public boolean redoable(){
        return mCurState < mRecord.size() - 1;
    }

    public boolean undoable(){
        return mRecord.size() > 0 && mCurState > 0;
    }

    public void setEditRecordCallback(EditRecordCallback callback){
        mCallback = callback;
    }

    class EditRecord{
        CharSequence toBeReplace;
        CharSequence replace;
        // start index of change
        int start;
        // length of text to be replaced;
        int before;
        // length of new text
        int after;

        @Override
        public String toString() {
            return "old =" + toBeReplace + " new =" + replace + " start =" + start + " before =" + before + " after =" + after;
        }
    }
}
