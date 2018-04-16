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

package com.openthos.editor.v2.ui;

import android.database.DataSetObserver;
import android.support.v4.view.GravityCompat;
import android.view.View;

import com.openthos.editor.v2.Pref;
import com.openthos.editor.v2.R;
import com.openthos.editor.v2.adapter.EditorAdapter;
import com.openthos.editor.v2.adapter.TabAdapter;
import com.openthos.editor.v2.bean.TabInfo;
import com.openthos.editor.v2.interfaces.TabCloseListener;
import com.openthos.common.utils.DBHelper;
import com.openthos.editor.v2.utils.ExtGrep;
import com.openthos.editor.v2.view.EditorView;
import com.openthos.editor.v2.view.TabViewPager;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class TabManager implements TabViewPager.OnPageChangeListener {
    private final MainActivity mainActivity;
    private final TabAdapter mTabAdapter;
    private EditorAdapter mEditorAdapter;
    private boolean exitApp;

    public TabManager(MainActivity activity) {
        this.mainActivity = activity;
        this.mTabAdapter = new TabAdapter();
        mTabAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabMenuViewsClick(v);
            }
        });

        mainActivity.mTabRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(activity.getContext()).build());
        mainActivity.mTabRecyclerView.setAdapter(mTabAdapter);

        initEditor();

        mainActivity.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mainActivity.mTabViewPager.setOnPageChangeListener(this);
        //setCurrentTab(0); //fix can't set last open tab
    }

    /**
     * 横向Tab的点击事件的实现
     */
    private void onTabMenuViewsClick(View v) {
        switch (v.getId()) {
            case R.id.close_image_view:
                closeTab((int) v.getTag());
                break;
            case R.id.add_image_view:
                newTab();
                break;
            default:
                int position = (int) v.getTag();
                mainActivity.closeMenu();
                setCurrentTab(position);
                break;
        }
    }

    private void initEditor() {
        mEditorAdapter = new EditorAdapter(mainActivity);
        /**优先，避免TabAdapter获取不到正确的CurrentItem
         * <com.openthos.editor_area.v2.view.TabViewPager
         * android:id="@+id/tab_pager"
         * android:layout_width="match_parent"
         * android:layout_height="match_parent"
         * android:visibility="gone"/>
         *  在main_activity.xml中
         * */
        mainActivity.mTabViewPager.setAdapter(mEditorAdapter);

        if (Pref.getInstance(mainActivity).isOpenLastFiles()) {
            ArrayList<DBHelper.RecentFileItem> recentFiles = DBHelper.getInstance(mainActivity).getRecentFiles(true);

            File f;
            for (DBHelper.RecentFileItem item : recentFiles) {
                f = new File(item.path);
                if (!f.isFile())
                    continue;
                mEditorAdapter.newEditor(false, f, item.line, item.column, item.encoding);
                setCurrentTab(mEditorAdapter.getCount() - 1); //fixme: auto load file, otherwise click other tab will crash by search result
            }
            mEditorAdapter.notifyDataSetChanged();
            updateTabList();

            int lastTab = Pref.getInstance(mainActivity).getLastTab();
            setCurrentTab(lastTab);
        }

        mEditorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                updateTabList();

                if (!exitApp && mEditorAdapter.getCount() == 0) {
                    newTab();
                }
            }
        });

        if (mEditorAdapter.getCount() == 0)
            mEditorAdapter.newEditor(mainActivity.getString(R.string.new_filename, mEditorAdapter.countNoFileEditor() + 1), null);
    }

    public void newTab() {
        mEditorAdapter.newEditor(createNewTabName(), null);
        setCurrentTab(mEditorAdapter.getCount() - 1);//索引是从０开始,自然最后的索引是: getCount - 1
    }

    public boolean newTab(CharSequence content) {
        mEditorAdapter.newEditor(createNewTabName(), content);
        setCurrentTab(mEditorAdapter.getCount() - 1);
        return true;
    }

    public boolean newTab(ExtGrep grep) {
        mEditorAdapter.newEditor(grep);
        setCurrentTab(mEditorAdapter.getCount() - 1);
        return true;
    }

    public boolean newTab(File path, String encoding) {
        return newTab(path, 0, 0, encoding);
    }

    public boolean newTab(File path, int line, int column, String encoding) {
        int count = mEditorAdapter.getCount();
        for (int i = 0; i < count; i++) {
            EditorDelegate fragment = mEditorAdapter.getItem(i);
            if (fragment.getPath() == null)
                continue;
            if (fragment.getPath().equals(path.getPath())) {
                setCurrentTab(i);
                return false;
            }
        }
        mEditorAdapter.newEditor(path, line, column, encoding);
        setCurrentTab(count);
        return true;
    }

    private String createNewTabName() {
        boolean isHave = false;
        TabInfo[] tabInfoList = mTabAdapter.getTabInfoList();
        if (tabInfoList == null || tabInfoList.length == 0) {
            return mainActivity.getString(R.string.new_filename, 1);
        }
        for (int i = 1; ; i++) {
            String newFileName = mainActivity.getString(R.string.new_filename, i);
            for (TabInfo tabInfo : tabInfoList) {
                if (newFileName.equals(tabInfo.getTitle())) {
                    isHave = true;
                    break;
                }
            }
            if (!isHave) {
                return newFileName;
            }
            isHave = false;
        }
    }

    public void setCurrentTab(final int index) {
        mainActivity.mTabViewPager.setCurrentItem(index);
        mTabAdapter.setCurrentTab(index);
        updateToolbar();
    }

    public int getTabCount() {
        if (mTabAdapter == null)
            return 0;
        return mTabAdapter.getItemCount();
    }

    public int getCurrentTab() {
        return mainActivity.mTabViewPager.getCurrentItem();
    }

    public void closeTab(int position) {
        mEditorAdapter.removeEditor(position, new TabCloseListener() {
            @Override
            public void onClose(String path, String encoding, int line, int column) {
                DBHelper.getInstance(mainActivity).updateRecentFile(path, false);
                int currentTab = getCurrentTab();
                if (getTabCount() != 0) {
                    setCurrentTab(currentTab); //设置title等等
                }
                //mTabAdapter.setCurrentTab(currentTab);
            }
        });
    }

    public EditorAdapter getEditorAdapter() {
        return mEditorAdapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTabAdapter.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateTabList() {
        mTabAdapter.setTabInfoList(mEditorAdapter.getTabInfoList());
        mTabAdapter.notifyDataSetChanged();
    }

    public void updateEditorView(int index, EditorView editorView) {
        mEditorAdapter.setEditorView(index, editorView);
    }

    public void onDocumentChanged(int index) {
        updateTabList();
        updateToolbar();
    }

    private void updateToolbar() {
        EditorDelegate delegate = mEditorAdapter.getItem(getCurrentTab());
        if (delegate == null)
            return;
        mainActivity.mToolbar.setTitle(delegate.getToolbarText());
    }

    public boolean closeAllTabAndExitApp() {
        EditorDelegate.setDisableAutoSave(true);
        exitApp = true;
        if (mainActivity.mTabViewPager != null) {
            Pref.getInstance(mainActivity).setLastTab(getCurrentTab());
        }
        return mEditorAdapter.removeAll(new TabCloseListener() {
            @Override
            public void onClose(String path, String encoding, int line, int column) {
                DBHelper.getInstance(mainActivity).updateRecentFile(path, encoding, line, column);
                int count = getTabCount();
                if (count == 0) {
                    mainActivity.finish();
                } else {
                    mEditorAdapter.removeAll(this);
                }
            }
        });
    }
}
