package com.miaozi.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;


public class KGSlidingMenuLayout extends HorizontalScrollView {
    private Context mContext;
    private int mMenuRightMargin;
    //处理快速滑动
    private GestureDetector mGestureDetector;
    private View mMenuView;
    private View mContentView;
    //菜单是否打开
    private boolean mMenuIsOpen = false;
    //是否拦截自己的onTouch事件
    private boolean isIntercept;

    public KGSlidingMenuLayout(Context context) {
        this(context,null);
    }

    public KGSlidingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public KGSlidingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KGSlidingMenuLayout);
        float rightMargin = typedArray.getDimension(R.styleable.KGSlidingMenuLayout_menuRightMargin,dp2px(50));
        mMenuRightMargin = (int) (getScreenWidth() - rightMargin);
        mGestureDetector = new GestureDetector(context,mGestureListener);
        typedArray.recycle();

    }

    /**
     * 布局加载完毕
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //指定宽高 内容页面 的宽度为屏幕的宽度 菜单页面的宽度为 屏幕的宽度 减去 设定宽度
        //获取到的是LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        //限制用户只能加入两个布局 如果不是就抛出异常告诉用户
        if(childCount != 2){
            throw new RuntimeException("有且只能添加两个布局");
        }
        //第一步 设置宽高
        //获取菜单 布局
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams();
        menuParams.width = mMenuRightMargin;
        //7.0以上的手机必须设置这个
        mMenuView.setLayoutParams(menuParams);
        //获取内容布局
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentViewParams = mContentView.getLayoutParams();
        contentViewParams.width = getScreenWidth();
        mContentView.setLayoutParams(contentViewParams);

        //默认是关闭的 但是设置没有用 ? 为什么呢 因为这句话是在 onLayout之前执行的 所以要放到 onLayout方法中
        scrollTo(mMenuRightMargin,0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //第二步 设置 滑动到主页
        scrollTo(mMenuRightMargin,0);
    }

    /**
     * 处理事件拦截 当用户触摸右边部分的时候 应该先关闭菜单
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        isIntercept = false;
        if(mMenuIsOpen){
            float currentX = ev.getX();
            //触摸点 大于 菜单宽度
            if(currentX > mMenuRightMargin){
                //1. 关闭
                closeMenu();
                // 2. 子 view 不需要响应任何的触摸事件 也就是拦截子view的触摸事件 但是会响应自己的onTouch事件
                //所以要拦截自己的事件
                isIntercept = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }


    /**
     * 手指抬起的时候 要么打开 要么关闭
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isIntercept){
            return true;
        }
        //设置滑动到手势上
        //在这里有一个问题 在下面的 up 事件中 也对菜单进行了打开关闭操作 所以某些时候会重复 需要做判断
        //快速滑动触发了 就不要再执行下面的方案了
        if(mGestureDetector.onTouchEvent(ev)){
            return true;
        }
        // 第三步 只需要监听抬起状态 根据滚动距离判断
        if(ev.getAction() == MotionEvent.ACTION_UP){
            float currentScrollX = getScrollX();
            if(currentScrollX > mMenuRightMargin / 2){
                //关闭
                closeMenu();
            }else {
                //打开
                openMenu();
            }
            //这里返回是为了保证了 super不执行 否则 smoothScrollTo() 没有效果
            return true;
        }
        return super.onTouchEvent(ev);
    }

    // 第四步 左右两边的缩放
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.d("TAG","left="+l+" top="+t+" old-left="+oldl+" old-top="+oldt);
        //算一个比例
        float scale = 1f * l/mMenuRightMargin;
//        Log.d("TAG","scale = "+ scale);
        //右边缩放 最小是 0.7f  最大是 1f
        //缩放 1.0f->0.7f
        float rightScale = 0.7f + 0.3f * scale;

//        Log.d("TAG","rightScale = "+ rightScale);

        //设置右边的缩放 默认是中心点缩放
        // 设置缩放的中心点

        mContentView.setPivotX(0);
        mContentView.setPivotY(mContentView.getMeasuredHeight()/2);
        mContentView.setScaleX(rightScale);
        mContentView.setScaleY(rightScale);

        //左边菜单的缩放 和透明度
        //透明度是 半透明到全透明
        // 缩放 0.7f->1.0f
        float leftScale = 0.7f + 0.3f * (1-scale);
        mMenuView.setScaleX(leftScale);
        mMenuView.setScaleY(leftScale);

        //设置透明度
        float alphaScale = 0.5f + 0.5f * (1-scale);
        mMenuView.setAlpha(alphaScale);

        //最后一个 酷狗 音乐 地下的 退出 按钮一开始 是在右边 被覆盖的
        // 通过平移实现
        mMenuView.setTranslationX(0.4f*l);


    }

    /**
     * 打开菜单 也就是滚动到 0 的位置
     */
    public void openMenu(){
        //带动画 打开
        mMenuIsOpen = true;
        smoothScrollTo(0,0);
    }
    /**
     * 关闭菜单 也就是滚动到 mMenuRightMargin 的位置
     */
    public void closeMenu(){
        mMenuIsOpen = false;
        smoothScrollTo(mMenuRightMargin,0);
    }

    /**
     * 获取屏幕宽度
     */
    private int getScreenWidth(){
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * dp 转 px
     * @param dp
     * @return
     */
    private int dp2px(int dp){
        float density = this.mContext.getResources().getDisplayMetrics().density;
        return (int)(dp * density + 0.5f);
    }
    private GestureDetector.OnGestureListener mGestureListener = new  GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //只要快速滑动 就会回调
            //所以我们只关注快速滑动 向右滑动的时候 打开 向左的时候关闭
            //快速往右滑动的时候 velocityX 是正数 往左滑动的时候 是负数
            if (mMenuIsOpen) {//已经打开
                //向右快速滑动 执行关闭
                if (velocityX < 0) {
                    closeMenu();
                    return true;
                }
            } else {//已经关闭
                //向左快速滑动 执行打开
                if (velocityX > 0) {
                    openMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };
}
