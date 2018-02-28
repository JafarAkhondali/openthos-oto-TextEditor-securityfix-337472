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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.editor.v2.R;
import com.openthos.editor.v2.view.menu.MenuItemInfo;

import java.util.List;

/**
 * Created by ljh on 18-1-3.
 */

public class GroupMenuListAdapter extends BaseAdapter {
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
        return convertView;
    }

    public void refresh(List<MenuItemInfo> datas) {
        if (datas != null) {
            mDatas.clear();
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    private class ViewHolder implements View.OnHoverListener {
        private ImageView icon;
        private TextView text;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.img_icon);
            text = (TextView) view.findViewById(R.id.menu_item_text);
            view.setOnHoverListener(this);
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            ViewHolder holder = (ViewHolder) v.getTag();
            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundColor(
                            mContext.getResources().getColor(android.R.color.holo_blue_bright));
                    holder.text.setSelected(true);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundColor(
                            mContext.getResources().getColor(R.color.gray));
                    holder.text.setSelected(false);
                    break;
            }
            return false;
        }
    }
}
