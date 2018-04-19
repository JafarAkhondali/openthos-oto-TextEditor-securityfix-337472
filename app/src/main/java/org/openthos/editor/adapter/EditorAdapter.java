/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openthos.editor.adapter;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import org.openthos.common.adapter.ViewPagerAdapter;
import org.openthos.editor.R;
import org.openthos.editor.bean.Command;
import org.openthos.editor.bean.TabInfo;
import org.openthos.editor.interfaces.SaveListener;
import org.openthos.editor.interfaces.TabCloseListener;
import org.openthos.editor.task.ClusterCommand;
import org.openthos.editor.ui.EditorDelegate;
import org.openthos.editor.ui.MainActivity;
import org.openthos.editor.ui.dialog.SaveConfirmDialog;
import org.openthos.editor.utils.ExtGrep;
import org.openthos.editor.view.EditorView;
import org.openthos.editor.widget.text.JsCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class EditorAdapter extends ViewPagerAdapter {
    private final Context context;
    /**
     * 数据源就是编辑的区域，
     * EditorDelegate就类似一个编辑区域的模型类。
     * */
    private List<EditorDelegate> mEditorDelegatesList = new ArrayList<>();
    private int currentPosition;

    public EditorAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getView(int position, ViewGroup pager) {
        EditorView view = (EditorView) LayoutInflater.from(context)
                                       .inflate(R.layout.editor_area, pager, false);
        setEditorView(position, view);
        return view;
    }

    @Override
    public int getCount() {
        return mEditorDelegatesList.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentPosition = position;
        setEditorView(position, (EditorView) object);
    }

    @Override
    public Parcelable saveState() {
        SavedState ss = new SavedState();
        ss.states = new EditorDelegate.SavedState[mEditorDelegatesList.size()];
        for (int i = mEditorDelegatesList.size() - 1; i >= 0; i--) {
            ss.states[i] = (EditorDelegate.SavedState) mEditorDelegatesList.get(i).onSaveInstanceState();
        }
        return ss;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (!(state instanceof SavedState))
            return;
        EditorDelegate.SavedState[] ss = ((SavedState) state).states;
        mEditorDelegatesList.clear();
        for (int i = 0; i < ss.length; i++) {
            mEditorDelegatesList.add(new EditorDelegate(ss[i]));
        }
        notifyDataSetChanged();
    }

    /**
     * @param file 一个路径或标题
     */
    public void newEditor(@Nullable File file, int line, int column, String encoding) {
        newEditor(true, file, line, column, encoding);
    }

    public void newEditor(boolean notify, @Nullable File file, int line, int column, String encoding) {
        mEditorDelegatesList.add(new EditorDelegate(mEditorDelegatesList.size(), file, line, column, encoding));
        if (notify)
            notifyDataSetChanged();
    }

    public void newEditor(String title, @Nullable CharSequence content) {
        mEditorDelegatesList.add(new EditorDelegate(mEditorDelegatesList.size(), title, content));
        notifyDataSetChanged();
    }

    public void newEditor(ExtGrep grep) {
        mEditorDelegatesList.add(new EditorDelegate(mEditorDelegatesList.size(), context.getString(R.string.find_title, grep.getRegex()), grep));
        notifyDataSetChanged();
    }

    /**
     * 当View被创建或是内存不足重建时，如果不更新list的内容，就会链接到旧的View
     *
     * @param index
     * @param editorView
     */
    public void setEditorView(int index, EditorView editorView) {
        if (index >= getCount()) {
            return;
        }
        EditorDelegate delegate = mEditorDelegatesList.get(index);
        if (delegate != null)
            delegate.setEditorView(editorView);
            //notifyDataSetChanged();  //不管是创建，还是重建，这里都不应该刷新
    }

    public EditorDelegate getCurrentEditorDelegate() {
        if (mEditorDelegatesList == null || mEditorDelegatesList.isEmpty() || currentPosition >= mEditorDelegatesList.size())
            return null;
        return mEditorDelegatesList.get(currentPosition);
    }

    public int countNoFileEditor() {
        int count = 0;
        for (EditorDelegate f : mEditorDelegatesList) {
            if (f.getPath() == null) {
                count++;
            }
        }
        return count;
    }

    public TabInfo[] getTabInfoList() {
        int size = mEditorDelegatesList.size();
        TabInfo[] arr = new TabInfo[size];
        EditorDelegate f;
        for (int i = 0; i < size; i++) {
            f = mEditorDelegatesList.get(i);
            arr[i] = new TabInfo(f.getTitle(), f.getPath(), f.isChanged());
        }
        return arr;
    }

    public boolean removeEditor(final int position, final TabCloseListener listener) {
        EditorDelegate f = mEditorDelegatesList.get(position);

        if (f.isChanged()) {
            new SaveConfirmDialog(context, f.getTitle(), new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    if (which == DialogAction.POSITIVE) {
                        Command command = new Command(Command.CommandEnum.SAVE);
                        command.object = new SaveListener() {
                            @Override
                            public void onSaved() {
                                doRemove(position, listener);
                            }
                        };
                        ((MainActivity) context).doCommand(command);
                    } else if (which == DialogAction.NEGATIVE) {
                        doRemove(position, listener);
                    } else {
                        dialog.dismiss();
                    }
                }
            }).show();
            return false;
        } else {
            doRemove(position, listener);
            return true;
        }
    }

    private void doRemove(final int position, final TabCloseListener listener) {
        EditorDelegate f = mEditorDelegatesList.get(position);

        final String encoding = f.getEncoding();
        final String path = f.getPath();

        if (f.mEditText != null) {
            f.mEditText.getCurrentPosition(new JsCallback<Integer[]>() {
                @Override
                public void onCallback(Integer[] data) {
                    remove(position);
                    if (listener != null && data != null)
                        listener.onClose(path, encoding, data[0], data[1]);
                }
            });
        } else {
            remove(position);
            if (listener != null)
                listener.onClose(path, encoding, 0, 0);
        }
    }

    private void remove(int position) {
        if (mEditorDelegatesList.isEmpty() || mEditorDelegatesList.size() <= position)
            return;
        EditorDelegate delegate = mEditorDelegatesList.remove(position);
        delegate.setRemoved();
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return ((EditorView) object).isRemoved() ? POSITION_NONE : POSITION_UNCHANGED;
    }

    public ClusterCommand makeClusterCommand() {
        return new ClusterCommand(new ArrayList<>(mEditorDelegatesList));
    }

    public boolean removeAll(TabCloseListener tabCloseListener) {
        int position = mEditorDelegatesList.size() - 1;
        return position < 0 || removeEditor(position, tabCloseListener);
    }

    public EditorDelegate getItem(int i) {
        //TabManager调用时，可能程序已经退出，updateToolbar时就不需要做处理了
        if (i >= mEditorDelegatesList.size())
            return null;
        return mEditorDelegatesList.get(i);
    }

    public static class SavedState implements Parcelable {
        EditorDelegate.SavedState[] states;

        protected SavedState() {
        }

        protected SavedState(Parcel in) {
//            states = in.readParcelableArray();
            states = in.createTypedArray(EditorDelegate.SavedState.CREATOR);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelableArray(states, flags);
        }
    }
}
