package com.sunzn.tangram.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.sunzn.tangram.library.R;
import com.sunzn.tangram.library.adapter.TangramAdapter;
import com.sunzn.tangram.library.bean.TangramBean;
import com.sunzn.tangram.library.interfaces.LoadMoreListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunzn on 2017/9/5.
 */

public class TangramView extends RecyclerView {

    private boolean hasMore = true;
    private boolean isProcess = false;
    private TangramAdapter mAdapter;
    private LoadMoreListener mLoadMoreListener;
    private ArrayList<Integer> mFootViewIDs = new ArrayList<>();

    public TangramView(Context context) {
        this(context, null);
    }

    public TangramView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TangramView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TangramView);
        int mFooterLoadView = typedArray.getResourceId(R.styleable.TangramView_FooterLoad, -1);
        int mFooterFailView = typedArray.getResourceId(R.styleable.TangramView_FooterFail, -1);
        int mFooterDoneView = typedArray.getResourceId(R.styleable.TangramView_FooterDone, -1);

        if (mFooterLoadView != -1) mFootViewIDs.add(mFooterLoadView);
        if (mFooterFailView != -1) mFootViewIDs.add(mFooterFailView);
        if (mFooterDoneView != -1) mFootViewIDs.add(mFooterDoneView);

        typedArray.recycle();
    }

    public <T extends TangramAdapter> void setCompatAdapter(T adapter) {
        adapter.setFootViews(mFootViewIDs);
        setAdapter(adapter);
        mAdapter = adapter;
    }

    public <T extends TangramBean> void setSuccess(List<T> beans) {
        mAdapter.addData(beans);
        notifyDataChanged();
        setProcess(false);
    }

    public void setInitial() {
        setHasMore(true);
        setProcess(false);
        mAdapter.setFootStateLoad();
        notifyDataChanged();
    }

    public void setLoading() {
        setHasMore(true);
        setProcess(true);
        mAdapter.setFootStateLoad();
        notifyDataChanged();
    }

    public void setFailure() {
        setProcess(true);
        mAdapter.setFootStateFail();
        notifyDataChanged();
    }

    public void setClosure() {
        setHasMore(false);
        setProcess(false);
        mAdapter.setTerminal();
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        if (getAdapter() != null) getAdapter().notifyDataSetChanged();
    }

    public void setHasMore(boolean more) {
        hasMore = more;
    }

    public void setProcess(boolean process) {
        isProcess = process;
    }

    public void setLoadMoreListener(LoadMoreListener listener) {
        mLoadMoreListener = listener;
        initScrollListener();
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
    }

    private void initScrollListener() {
        if (mFootViewIDs != null && mFootViewIDs.size() == 3) {
            this.addOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (mLoadMoreListener != null && !isProcess && hasMore) {
                        LayoutManager manager = getLayoutManager();
                        if (manager != null && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            int last = findLastVisibleItemPosition(manager);
                            int total = getAdapter() == null ? 0 : getAdapter().getItemCount();
                            if (last >= total - 1) {
                                isProcess = true;
                                mLoadMoreListener.onLoadMore();
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        } else {
            throw new IllegalArgumentException("init ScrollListener must set FooterLoad, FooterFail, FooterDone attribute");
        }
    }

    private int findLastVisibleItemPosition(LayoutManager manager) {
        if (manager instanceof StaggeredGridLayoutManager) {
            int[] last = ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(null);
            return last[last.length - 1];
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else {
            return 0;
        }
    }

    public abstract static class Decoration extends ItemDecoration {

        public abstract void onDecoration(int position, @NonNull Rect outRect);

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int position = parent.getChildAdapterPosition(view);
            onDecoration(position, outRect);
        }

    }

}
