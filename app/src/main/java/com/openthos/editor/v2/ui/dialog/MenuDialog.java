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

package com.openthos.editor.v2.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.openthos.editor.v2.R;
import com.openthos.editor.v2.adapter.GroupMenuListAdapter;
import com.openthos.editor.v2.interfaces.MenuItemClickListener;
import com.openthos.editor.v2.view.menu.MenuItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljh on 18-1-3.
 */

public class MenuDialog extends Dialog implements AdapterView.OnItemClickListener {

    private static MenuDialog mMenuDialog;
    private ListView mMenuList;
    private GroupMenuListAdapter mAdapter;
    private List<MenuItemInfo> mDatas;
    private View mSelectView;
    private MenuItemClickListener mListener;

    public static MenuDialog getInstance(Context context) {
        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(context);
        }
        return mMenuDialog;
    }

    public MenuDialog(@NonNull Context context) {
        super(context, R.style.MenuDialogStyle);
        mDatas = new ArrayList<>();
        mAdapter = new GroupMenuListAdapter(context, mDatas);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_dialog);
        setCanceledOnTouchOutside(true);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mMenuList = (ListView) findViewById(R.id.menu_list);
    }

    private void initData() {
        mMenuList.setAdapter(mAdapter);
    }

    private void initListener() {
        mMenuList.setOnItemClickListener(this);
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
        mMenuList.setLayoutParams(new LinearLayout.LayoutParams(maxWidth, height));
    }

    @Override
    public void dismiss() {
        if (mSelectView != null) {
            mSelectView.setSelected(false);
        }
        super.dismiss();
    }

    public void setOnMenuItemClickListener(MenuItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onMenuItemClick(mDatas.get(position).getItemId());
        }
        dismiss();
    }
}
