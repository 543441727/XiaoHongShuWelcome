package com.qianmo.xiaohongshuwelcome.parallaxpager;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.qianmo.xiaohongshuwelcome.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 感谢https://github.com/prolificinteractive/ParallaxPager
 */
public class ParallaxContainer extends FrameLayout {

    private String TAG = "ParallaxContainer";

    //视觉views
    private List<View> parallaxViews = new ArrayList<View>();
    //Viewpager
    private ViewPager viewPager;
    //page的大小
    private int pageCount = 0;
    //容器的宽度
    private int containerWidth;
    //是否循环
    private boolean isLooping = false;
    //viewpager的适配器
    private final ParallaxPagerAdapter adapter;
    //上下文环境
    Context context;
    //viewpager的滑动监听
    public ViewPager.OnPageChangeListener mCommonPageChangeListener;
    //所有xml文件的list集合
    private List<View> viewlist = new ArrayList<View>();
    //当前页
    public int currentPosition = 0;

    public ParallaxContainer(Context context) {
        super(context);
        this.context = context;
        adapter = new ParallaxPagerAdapter(context);
    }

    public ParallaxContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        adapter = new ParallaxPagerAdapter(context);
    }

    public ParallaxContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        adapter = new ParallaxPagerAdapter(context);
    }

    /**
     * 从onWindowFocusChanged被执行起，用户可以与应用进行交互了，而这之前，对用户的操作需要做一点限制。
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        containerWidth = getMeasuredWidth();
        if (viewPager != null) {
            mCommonPageChangeListener.onPageScrolled(viewPager.getCurrentItem(), 0, 0);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
        updateAdapterCount();
    }

    ImageView iv;

    //将小人图片添加进来
    public void setImage(ImageView iv) {
        this.iv = iv;
    }

    //被调用的时候好像是0
    private void updateAdapterCount() {
        adapter.setCount(isLooping ? Integer.MAX_VALUE : pageCount);
    }

    //添加子view
    public void setupChildren(LayoutInflater inflater, int... childIds) {
        if (getChildCount() > 0) {
            throw new RuntimeException("setupChildren should only be called once when ParallaxContainer is empty");
        }

        //创建打气筒
        ParallaxLayoutInflater parallaxLayoutInflater = new ParallaxLayoutInflater(
                inflater, getContext());

        //将所有的view添加到本控件上去
        for (int childId : childIds) {
            View view = parallaxLayoutInflater.inflate(childId, this);
            viewlist.add(view);
        }
        //添加视觉view
        pageCount = getChildCount();
        for (int i = 0; i < pageCount; i++) {
            View view = getChildAt(i);
            addParallaxView(view, i);
        }

        //更新ViewPagerAdapter的数量
        updateAdapterCount();

        //创建viewpager
        viewPager = new ViewPager(getContext());
        viewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        viewPager.setId(R.id.parallax_pager);
        //给viewpager添加滑动监听
        attachOnPageChangeListener();
        //设置适配器
        viewPager.setAdapter(adapter);
        //将viewpager添加到主控件中
        addView(viewPager, 0);
    }

    /**
     * 至少持续时间
     */
    private static final long DELAY_TIME = 600;

    protected void attachOnPageChangeListener() {
        mCommonPageChangeListener = new ViewPager.OnPageChangeListener() {
            /**
             * 此方法是在状态改变的时候调用，其中arg0这个参数
             有三种状态（0，1，2）。arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
             * @param state
             */
            @Override
            public void onPageScrollStateChanged(int state) {
                Log.v(TAG, "onPageScrollStateChanged" + state);
                iv.setBackgroundResource(R.drawable.man_run);
                final AnimationDrawable animationDrawable = (AnimationDrawable) iv.getBackground();
                switch (state) {
                    case 0:
                        //处于展示阶段
                        finishAnim(animationDrawable);
                        break;
                    case 1:
                        //正在滑动
                        isEnd = false;
                        animationDrawable.start();
                        break;
                    case 2:
                        //滑动完毕
                        finishAnim(animationDrawable);
                        break;
                }
            }

            //判断是否还是在左边
            boolean isleft = false;

            /**
             *  onPageScrolled(int arg0,float arg1,int arg2)    ，当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：
             * @param pageIndex 当前页面，及你点击滑动的页面
             * @param offset 当前页面偏移的百分比
             * @param offsetPixels 当前页面偏移的像素位置
             */
            @Override
            public void onPageScrolled(int pageIndex, float offset, int offsetPixels) {
//				Log.v(TAG, "onPageScrolled" + pageIndex + "  offset" + offset + "   offsetPixels" + offsetPixels);

                if (offsetPixels < 10) {
                    isleft = false;
                }

                if (pageCount > 0) {
                    pageIndex = pageIndex % pageCount;
                }

                if (pageIndex == 3) {
                    if (isleft) {

                    } else {
                        iv.setX(iv.getLeft() - offsetPixels);
                    }
                }
                ParallaxViewTag tag;
                for (View view : parallaxViews) {
                    tag = (ParallaxViewTag) view.getTag(R.id.parallax_view_tag);
                    if (tag == null) {
                        continue;
                    }

                    if ((pageIndex == tag.index - 1 || (isLooping && (pageIndex == tag.index
                            - 1 + pageCount)))
                            && containerWidth != 0) {

                        // make visible
                        view.setVisibility(VISIBLE);

                        // slide in from right
                        view.setTranslationX((containerWidth - offsetPixels) * tag.xIn);

                        // slide in from top
                        view.setTranslationY(0 - (containerWidth - offsetPixels) * tag.yIn);

                        // fade in
                        view.setAlpha(1.0f - (containerWidth - offsetPixels) * tag.alphaIn / containerWidth);

                    } else if (pageIndex == tag.index) {

                        // make visible
                        view.setVisibility(VISIBLE);

                        // slide out to left
                        view.setTranslationX(0 - offsetPixels * tag.xOut);

                        // slide out to top
                        view.setTranslationY(0 - offsetPixels * tag.yOut);

                        // fade out
                        view.setAlpha(1.0f - offsetPixels * tag.alphaOut / containerWidth);

                    } else {
                        view.setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(TAG, "onPageSelected" + position);
                currentPosition = position;
            }
        };
        viewPager.setOnPageChangeListener(mCommonPageChangeListener);
    }

    boolean isEnd = false;

    /**
     * 结束动画
     *
     * @param animationDrawable
     */
    private synchronized void finishAnim(final AnimationDrawable animationDrawable) {
        if (isEnd) {
            return;
        }
        isEnd = true;
        final long delay = DELAY_TIME;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "onPageScrollStateChanged   delay" + delay);
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (animationDrawable.isRunning() && isEnd) {
                    animationDrawable.stop();
                }
            }
        }).start();
    }

    /**
     * 添加视觉view方法
     *
     * @param view
     * @param pageIndex
     */
    private void addParallaxView(View view, int pageIndex) {
        //通过递归方法拿到最小单元的view
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
                addParallaxView(viewGroup.getChildAt(i), pageIndex);
            }
        }
        //创建视觉差view绑定，并添加到集合中去
        ParallaxViewTag tag = (ParallaxViewTag) view.getTag(R.id.parallax_view_tag);
        if (tag != null) {
            tag.index = pageIndex;
            parallaxViews.add(view);
        }
    }
}
