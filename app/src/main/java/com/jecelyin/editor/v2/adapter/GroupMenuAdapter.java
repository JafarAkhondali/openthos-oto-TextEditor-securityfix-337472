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

package com.jecelyin.editor.v2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jecelyin.editor.v2.R;
import com.jecelyin.editor.v2.dialog.MenuDialog;
import com.jecelyin.editor.v2.dialog.MenuItemClickListener;
import com.jecelyin.editor.v2.view.menu.MenuFactory;
import com.jecelyin.editor.v2.view.menu.MenuGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljh on 18-1-3.
 */

public class GroupMenuAdapter extends RecyclerView.Adapter {
    private final MenuFactory mMenuFactory;
    private MenuItemClickListener mListener;
    private Context mContext;
    private List<MenuGroup> mMenuGroups;
    private View lastView;

    public GroupMenuAdapter(Context context) {
        mContext = context;
        mMenuFactory = MenuFactory.getInstance(context);
        mMenuGroups = new ArrayList<>();
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

    public void setOnMenuItemClickListener(MenuItemClickListener listener) {
        mListener = listener;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView menuText;
        public ViewHolder(View itemView) {
            super(itemView);
            menuText = (TextView) itemView.findViewById(R.id.menu_item_text);
            menuText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //v.setSelected(true);
                            //if (lastView !=null && lastView != v) {
                            //    lastView.setSelected(false);
                            //}
                            //lastView = v;
                            MenuDialog menuDialog = MenuDialog.getInstance(mContext);
                            if (menuDialog.isShowing()) {
                                menuDialog.dismiss();
                            } else {
                                menuDialog.show(mMenuFactory.getMenuItemInfos(mMenuGroups.
                                                             get((Integer) v.getTag())), v);//getTag
                            }
                            menuDialog.setOnMenuItemClickListener(mListener);
                            break;
                    }
                    return false;
                }
            });
        }
    }
}
