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

package com.openthos.editor.v2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openthos.editor.v2.R;
import com.openthos.editor.v2.interfaces.OnMenuClickListener;
import com.openthos.editor.v2.ui.MainActivity;
import com.openthos.editor.v2.view.MenuListView;
import com.openthos.editor.v2.view.menu.MenuFactory;
import com.openthos.editor.v2.view.menu.MenuGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljh on 18-1-3.
 */

public class TopMenuAdapter extends RecyclerView.Adapter {
    private final MenuFactory mMenuFactory;
    private Context mContext;
    private List<MenuGroup> mMenuGroups;
    private MenuListView mMenuListView;

    public TopMenuAdapter(Context context) {
        mContext = context;
        mMenuFactory = MenuFactory.getInstance(context);
        mMenuGroups = new ArrayList<>();
        mMenuListView = ((MainActivity) mContext).getMenuList();
        for (MenuGroup group : MenuGroup.values()) {
            if (group.getNameResId() != 0) {
                mMenuGroups.add(group);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_menu_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        MenuGroup menuGroup = mMenuGroups.get(position);
        holder.menuText.setText(mContext.getResources().getString(menuGroup.getNameResId()));
        holder.menuText.setTag(position);//setTag
    }

    @Override
    public int getItemCount() {
        return mMenuGroups.size();
    }

    public void setOnMenuItemClickListener(OnMenuClickListener onMenuClickListener) {
        mMenuListView.setOnMenuClickListener(onMenuClickListener);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView menuText;

        public ViewHolder(View itemView) {
            super(itemView);
            menuText = (TextView) itemView.findViewById(R.id.menu_item_text);
            MenuTouchListener menuTouchListener = new MenuTouchListener();
            menuText.setOnTouchListener(menuTouchListener);

            menuText.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                            if (mMenuListView.isVisibility()) {
                                v.setSelected(true);
                                mMenuListView.show(v, mMenuFactory.getMenuItemInfos(true,
                                        mMenuGroups.get((Integer) v.getTag()), true));
                                mMenuListView.setCanCancel(false);
                            }
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            v.setSelected(false);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    private class MenuTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == null) {
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMenuListView.show(v, mMenuFactory.getMenuItemInfos(true,
                            mMenuGroups.get((Integer) v.getTag()), true));
                    mMenuListView.setCanCancel(false);
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    }
}