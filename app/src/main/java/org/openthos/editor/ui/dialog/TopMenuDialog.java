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

package org.openthos.editor.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.openthos.editor.R;
import org.openthos.editor.interfaces.OnMenuClickListener;
import org.openthos.editor.view.menu.MenuItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljh on 18-1-3.
 */

public class TopMenuDialog extends Dialog {

    private static TopMenuDialog mMenuDialog;
    private ListView mMenuList;
    private GroupMenuListAdapter mAdapter;
    private List<MenuItemInfo> mDatas;
    private View mSelectView;
    private OnMenuClickListener mOnMenuClickListener;

    public static TopMenuDialog getInstance(Context context) {
        if (mMenuDialog == null) {
            mMenuDialog = new TopMenuDialog(context);
        }
        return mMenuDialog;
    }

    public TopMenuDialog(@NonNull Context context) {
        super(context, R.style.MenuDialogStyle);
        mDatas = new ArrayList<>();
        mAdapter = new GroupMenuListAdapter(context, mDatas);
        create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_dialog);
        setCanceledOnTouchOutside(true);
        initView();
        initData();
    }

    private void initView() {
        mMenuList = (ListView) findViewById(R.id.menu_list);
    }

    private void initData() {
        mMenuList.setAdapter(mAdapter);
    }

    public void show(List<MenuItemInfo> datas, View view) {
        mSelectView = view;
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0.0f;
        lp.x = location[0];
        lp.y = location[1] + view.getMeasuredHeight();
        dialogWindow.setAttributes(lp);
        show();
        mAdapter.refresh(datas);
        int maxWidth = 0;
        int height = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View views = mAdapter.getView(i, null, null);
            views.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            maxWidth = Math.max(views.getMeasuredWidth(), maxWidth);
            height = height + views.getMeasuredHeight();
        }
        int paddingOne = getContext().getResources().getDimensionPixelSize(R.dimen.padding_one);
        maxWidth = maxWidth + paddingOne * 2;
        height = height + paddingOne * 2;
        mMenuList.setLayoutParams(new LinearLayout.LayoutParams(maxWidth, height));
    }

    public void showLocationDialog(List<MenuItemInfo> data, int x, int y) {
        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSLUCENT;
        lp.dimAmount = 0.0f;
        lp.x = x;
        lp.y = y;
        lp.width = 150;
        dialogWindow.setAttributes(lp);
        show();
        mAdapter.refresh(data);

        int maxWidth = 0;
        int height = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View views = mAdapter.getView(i, null, null);
            views.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            maxWidth = Math.max(views.getMeasuredWidth(), maxWidth);
            height = height + views.getMeasuredHeight();
        }
        int paddingOne = getContext().getResources().getDimensionPixelSize(R.dimen.padding_one);
        maxWidth = maxWidth + paddingOne * 2;
        height = height + paddingOne * 2;
        mMenuList.setLayoutParams(new LinearLayout.LayoutParams(maxWidth, height));
    }

    @Override
    public void dismiss() {
        if (mSelectView != null) {
            mSelectView.setSelected(false);
        }
        super.dismiss();
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        Log.i("Smaster", "position setOnMenuClickListener");
        mOnMenuClickListener = onMenuClickListener;
    }

    private class GroupMenuListAdapter extends BaseAdapter {
        private Context mContext;
        private List<MenuItemInfo> mDatas;

        public GroupMenuListAdapter(Context context, List<MenuItemInfo> datas) {
            mContext = context;
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.group_menu_list_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MenuItemInfo itemInfo = mDatas.get(position);
            Drawable drawable = mContext.getResources().getDrawable(itemInfo.getIconResId());
            holder.icon.setImageDrawable(drawable);
            holder.text.setText(mContext.getResources().getString(itemInfo.getTitleResId()));
            holder.layout.setTag(itemInfo);
            return convertView;
        }

        public void refresh(List<MenuItemInfo> datas) {
            if (datas != null) {
                mDatas.clear();
                mDatas.addAll(datas);
                notifyDataSetChanged();
            }
        }

        private class ViewHolder implements View.OnHoverListener, View.OnTouchListener {
            private LinearLayout layout;
            private ImageView icon;
            private TextView text;

            public ViewHolder(View view) {
                layout = (LinearLayout) view.findViewById(R.id.layout);
                icon = (ImageView) view.findViewById(R.id.img_icon);
                text = (TextView) view.findViewById(R.id.menu_item_text);
                layout.setOnHoverListener(this);
                layout.setOnTouchListener(this);
            }

            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.setSelected(true);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setSelected(false);
                        break;
                }
                return false;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onMenuItemClick((MenuItemInfo) v.getTag());
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        }
    }
}
