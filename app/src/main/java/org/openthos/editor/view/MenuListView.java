package org.openthos.editor.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.openthos.editor.R;
import org.openthos.editor.interfaces.OnMenuClickListener;
import org.openthos.editor.view.menu.MenuItemInfo;

import java.util.ArrayList;
import java.util.List;

public class MenuListView extends ListView {
    public static final int HEIGHT_MASK = 0x3fffffff;
    private OnMenuClickListener mOnMenuClickListener;
    private List<MenuItemInfo> mDatas;
    private MenuListAdapter mAdapter;
    private boolean mCanCancel;
    private int[] mParentPosition;
    private View mParentView;

    public MenuListView(Context context) {
        this(context, null);
    }

    public MenuListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mParentPosition = new int[2];
        mDatas = new ArrayList<>();
        mAdapter = new MenuListAdapter(getContext(), mDatas);
        setAdapter(mAdapter);
    }

    public void setParentView(View view) {
        mParentView = view;
    }

    public void show(View view, List<MenuItemInfo> datas) {
        if (getVisibility() == VISIBLE) {
            setVisibility(GONE);
        }
        mDatas.clear();
        mDatas.addAll(datas);
        mAdapter.notifyDataSetChanged();

        int[] location = new int[2];
        view.getLocationInWindow(location);

        mParentView.getLocationInWindow(mParentPosition);
        int width = 0;
        int height = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View v = mAdapter.getView(i, null, null);
            v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            width = Math.max(v.getMeasuredWidth(), width);
            height = height + v.getMeasuredHeight();
        }
        int paddingOne = getContext().getResources().getDimensionPixelSize(R.dimen.padding_one);
        width = width + paddingOne * 2;
        height = height + paddingOne * 2;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = location[0] + view.getMeasuredWidth() / 2 - width / 2 - mParentPosition[0];
        params.topMargin = location[1] + view.getMeasuredHeight() - mParentPosition[1];
        if (params.leftMargin < 0) {
            params.leftMargin = 0;
        }
        params.gravity = Gravity.LEFT | Gravity.TOP;
        setLayoutParams(params);
        setVisibility(VISIBLE);
    }

    public void show(List<MenuItemInfo> datas, int x, int y) {
        if (getVisibility() == VISIBLE) {
            setVisibility(GONE);
        }
        mDatas.clear();
        mDatas.addAll(datas);
        mAdapter.notifyDataSetChanged();

        mParentView.getLocationInWindow(mParentPosition);
        int width = 0;
        int height = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View v = mAdapter.getView(i, null, null);
            v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            width = Math.max(v.getMeasuredWidth(), width);
            height = height + v.getMeasuredHeight();
        }
        int paddingOne = getContext().getResources().getDimensionPixelSize(R.dimen.padding_one);
        width = width + paddingOne * 2;
        height = height + paddingOne * 2;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = x;
        params.topMargin = y;
        if (params.leftMargin < 0) {
            params.leftMargin = 0;
        }
        params.gravity = Gravity.LEFT | Gravity.TOP;
        setLayoutParams(params);
        setVisibility(VISIBLE);
    }

    public void dismiss() {
        if (isVisibility() && mCanCancel) {
            setVisibility(GONE);
        }
    }

    public boolean isVisibility() {
        return getVisibility() == VISIBLE;
    }

    public void setCanCancel(boolean canCancel) {
        mCanCancel = canCancel;
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE & HEIGHT_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, height);
    }

    private class MenuListAdapter extends BaseAdapter {
        private Context mContext;
        private List<MenuItemInfo> mDatas;

        public MenuListAdapter(Context context, List<MenuItemInfo> datas) {
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
                        setCanCancel(false);
                        mOnMenuClickListener.onMenuItemClick((MenuItemInfo) v.getTag());
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        }
    }
}