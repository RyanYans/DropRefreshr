package com.rya.droprefreshr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rya.droprefreshr.R;

/**
 * Created by Rya32 on 广东石油化工学院.
 * Version 1.0
 */

public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private static final int PULL_REFRESH = 100;
    private static final int RELEASE_REFRESH = 101;
    private static final int REFRESHING = 102;
    private View mHeaderView;
    private float downY;
    private float moveY;
    private float upY;
    private int mHeaderHeight;
    private ProgressBar pb_refresh;
    private ImageView iv_drop;
    private TextView tv_title;
    private TextView tv_desc;
    private int currentState;
    private RotateAnimation rotatePullAnimation;
    private RotateAnimation rotateReleaseAnimation;
    private OnDataRefreshing onDataRefreshing;
    private View mFooterView;
    private int mFooterHeight;
    private boolean isLoding;

    public RefreshListView(Context context) {
        super(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化 头布局， 脚布局，
     * 滚动监听
     */
    private void init() {

        initUI();

        initHeadrView();

        initAnimation();

        initFooterView();

        setOnScrollListener(this);
    }

    private void initUI() {
        mHeaderView = View.inflate(getContext(), R.layout.layout_refresh_listview, null);
        pb_refresh = (ProgressBar) mHeaderView.findViewById(R.id.pb_refresh);
        iv_drop = (ImageView) mHeaderView.findViewById(R.id.iv_drop);
        tv_title = (TextView) mHeaderView.findViewById(R.id.tv_title);
        tv_desc = (TextView) mHeaderView.findViewById(R.id.tv_desc);
    }

    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.layout_footer_listview, null);

        mFooterView.measure(0, 0);

        mFooterHeight = mFooterView.getMeasuredHeight();

        mFooterView.setPadding(0, -mFooterHeight, 0, 0);

        this.addFooterView(mFooterView);


    }

    private void initAnimation() {
        rotatePullAnimation = new RotateAnimation(0, -180f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotatePullAnimation.setFillAfter(true);
        rotatePullAnimation.setDuration(300);

        rotateReleaseAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotatePullAnimation.setFillAfter(true);
        rotatePullAnimation.setDuration(300);

    }

    private void initHeadrView() {

        mHeaderView.measure(0, 0);
        mHeaderHeight = mHeaderView.getMeasuredHeight();

        // 隐藏下拉
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);

        this.addHeaderView(mHeaderView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == REFRESHING) {
                    return super.onTouchEvent(event);
                }
                moveY = event.getY();
                float offset = moveY - downY;
                if (offset > 0 && getFirstVisiblePosition() == 0) {
                    int newTop = (int) (mHeaderHeight - (moveY - downY));
                    // 隐藏下拉
                    mHeaderView.setPadding(0, -(newTop), 0, 0);
                    if ((newTop > 0) && (currentState != PULL_REFRESH)) {
                        currentState = PULL_REFRESH;
                        updateState();
                    } else if ((newTop < 0) && (currentState != RELEASE_REFRESH)) {
                        currentState = RELEASE_REFRESH;
                        updateState();
                    }
                    // 关键！ 事件处理并被消费
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == PULL_REFRESH) {
                    mHeaderView.setPadding(0, -(mHeaderHeight), 0, 0);
                } else if (currentState == RELEASE_REFRESH) {
                    mHeaderView.setPadding(0, 0, 0, 0);
                    currentState = REFRESHING;
                    updateState();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private void updateState() {
        switch (currentState) {
            case PULL_REFRESH:
                tv_title.setText("下拉刷新..");
                tv_desc.setText("下拉刷新标识符...");
                iv_drop.setVisibility(VISIBLE);
                pb_refresh.setVisibility(GONE);
                iv_drop.startAnimation(rotatePullAnimation);
                break;
            case RELEASE_REFRESH:
                tv_title.setText("释放刷新..");
                tv_desc.setText("释放刷新标识符...");
                iv_drop.startAnimation(rotateReleaseAnimation);
                break;
            case REFRESHING:
                tv_title.setText("正在刷新..");
                tv_desc.setText("正在刷新标识符...");
                iv_drop.setVisibility(GONE);
                pb_refresh.setVisibility(VISIBLE);

                if (onDataRefreshing != null) {
                    onDataRefreshing.onRefreshing();
                }

                break;
        }
    }

    public void refreshComplete() {
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
        currentState = PULL_REFRESH;
        updateState();

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                if (getLastVisiblePosition() == (getCount() - 1)) {
                    if (isLoding) {
                        return;
                    }
                    isLoding = true;

                    mFooterView.setPadding(0, 0, 0, 0);
                    setSelection(getCount());
                    if (onDataRefreshing != null) {
                        this.onDataRefreshing.onFooterRefreshing();
                    }
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void footerRefreshComplete() {
        mFooterView.setPadding(0, -mFooterHeight, 0, 0);

        isLoding = false;
    }

    public interface OnDataRefreshing {
        void onRefreshing();

        void onFooterRefreshing();
    }

    public void setOnDataRefreshing(OnDataRefreshing onDataRefreshing) {
        this.onDataRefreshing = onDataRefreshing;
    }

}
