- app/src/main/java/com/jecelyin/editor/v2/adapter/mZ
  - menu的点击事件。
  app/src/main/res/layout/main_tab_layout.xml

- editorDelegate.java
  - 复制　粘贴

- EditAreaView.java
  - 编辑区
- tab_item.xml
  - 新建文件的导航区

- GroupMenuAdapter.java
  - top 菜单项的点击事件。

- MenuFactory.java
  - initAllMenuItem() //弹出的菜单项.. 新建/打开　

- MainActivity.java
  - 菜单中的item的触发实现
  - onMenuClick(int id)

- EditorDelegate.java
  - case SAVE:
  - mEditText.setCustomSelectionActionModeCallBack(new EditorSelectionActionCallback());
- MenuDialog.java
  - 菜单弹出dialog, item的点击事件; onItemClick

- FileListItemAdapter.java
  - onBindViewHolder() // 图标的设置
  - 布局的添加使用的是binding

- file_list_item.xml

- FileExploreActivity.java
  - 文件管理器的Module/ 通过启动方式传递的mode区分是打开的布局还是　保存的布局；

- 实现编辑菜单的新建　/ 关闭
  - TabManager.java //文件项的管理
  - TabViewPager.java //自定义的ViewPager
    - EditorAdapter.java //对应的适配器PagerAdapter
  - editor.xml //编辑区
  - TabAdapter.java //recyclerView对应的适配器

- ace.js 15920行

- FontSizePreference.java //字体大小

- Pref.java //修改默认字体　18sp

- select_drawables_event_handler.js

- ace.js //ace_print-margin  去掉白线

- div.ace_cursor 是选中的区域

- bridge.js
  - 主要用于 java　和　js　进行交互；
  - self.showActionMode(); //长按弹出dialog
  - EditorAreaView.java中
    - @JavascriptInterface
      public void showActionMode() {}



