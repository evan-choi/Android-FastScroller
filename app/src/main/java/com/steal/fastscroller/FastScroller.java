package com.steal.fastscroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.SectionIndexer;
import android.widget.Toast;

import com.steal.fastscroller.common.Property;

import java.util.ArrayList;
import java.util.List;

public class FastScroller extends View {
    private static final boolean DEBUG = true;

    private static final int TOUCH_IDLE = 0;
    private static final int TOUCH_DOWN = 1;
    private static final int TOUCH_SCROLL = 2;

    // property
    private float spacing = 0;
    private float textHeight = 0;
    private float textSize = 0;
    private SectionIndexer indexer;

    // cache
    private String[] sectionCache;
    private int sectionIndex = -1;
    private Property<String> stringProperty;
    private Property<Paint> paintProperty;
    private Toast toast;

    // measure
    private float measuredSpacing = 0;

    // render
    private TextPaint textPaint;
    private Paint debugPaint;
    private RectF debugRect;

    // touch
    private int touchState = TOUCH_IDLE;
    private int downY;
    private int touchSlop;

    // event
    private List<OnSectionScrolledListener> listeners;
    private List<DecorationItem> decorations;

    public FastScroller(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public FastScroller(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public FastScroller(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttrs) {
        setWillNotDraw(false);

        listeners = new ArrayList<>();
        decorations = new ArrayList<>();
        stringProperty = new Property<>(null);
        paintProperty = new Property<>(null);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);

        if (DEBUG && BuildConfig.DEBUG && isInEditMode()) {
            debugPaint = new Paint();
            debugPaint.setColor(Color.RED);
            debugPaint.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
            debugPaint.setStyle(Paint.Style.STROKE);

            debugRect = new RectF();
        }

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FastScroller, defStyleAttrs, 0);

            setTextSize(array.getDimension(R.styleable.FastScroller_is_textSize, 12));
            setTextColor(array.getColor(R.styleable.FastScroller_is_textColor, Color.BLACK));
            setSpacing(array.getDimension(R.styleable.FastScroller_is_spacing, 0));

            array.recycle();
        }

        // 프리뷰
        if (isInEditMode()) {
            setSectionIndexer(new PreviewSectionIndexer());
        }

        // touch
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
    }

    public void setSectionIndexer(SectionIndexer indexer) {
        if (this.indexer == indexer) {
            return;
        }

        if (indexer != null && indexer.getSections() != null) {
            Object[] sections = indexer.getSections();
            sectionCache = new String[sections.length];

            for (int i = 0; i < sectionCache.length; i++) {
                sectionCache[i] = String.valueOf(sections[i]);
            }

        } else {
            sectionCache = null;
        }

        this.indexer = indexer;
        invalidateMeasure();
        invalidate();
    }

    public void setTextSize(float size) {
        textSize = size;
        textHeight = -textPaint.descent() - textPaint.ascent();
        invalidateMeasure();
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextColor(@ColorInt int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        invalidateMeasure();
        invalidate();
    }

    public float getSpacing() {
        return spacing;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidateMeasure();
        invalidate();
    }

    private void invalidateMeasure() {
        if (sectionCache != null) {
            float contentHeight = sectionCache.length * textHeight;
            float spacingHeight = getMeasuredHeight() - contentHeight - (sectionCache.length - 1) * spacing;

            spacingHeight -= getPaddingTop();
            spacingHeight -= getPaddingBottom();

            measuredSpacing = spacingHeight / (sectionCache.length * 2);
        } else {
            measuredSpacing = 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();

        y -= getPaddingTop();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = y;
                touchState = TOUCH_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchState == TOUCH_DOWN) {
                    if (Math.abs(downY - y) > touchSlop) {
                        touchState = TOUCH_SCROLL;
                    }
                } else {
                    // scroll 상태
                    float groupHeight = measuredSpacing * 2 + textHeight + spacing;
                    int index = (int) Math.floor(y / groupHeight);

                    if (index < 0 || index >= sectionCache.length || index == sectionIndex) {
                        break;
                    }

                    y -= groupHeight * index;

                    if (y <= groupHeight - spacing) {
                        sectionIndex = index;
                        onSectionChanged(index);
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: // 다른 뷰와 상호작용에 의해 강제로 캔슬된 경우
                touchState = TOUCH_IDLE;
                sectionIndex = -1;
                invalidate();
                break;
        }

        return true;
    }

    private void onSectionChanged(int index) {
        raiseOnSectionScrolledListener(index);

        /*Toast toast;

        if (this.toast == null || !this.toast.getView().isShown()) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View layout = inflater.inflate(R.layout.layout_indexer_toast, null);

            toast = new Toast(getContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
        } else {
            toast = this.toast;
            toast.cancel();
        }

        TextView tvText = toast.getView().findViewById(R.id.tv_text);
        tvText.setText(sectionCache[index]);

        this.toast = toast;
        toast.show();*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sectionCache == null) {
            return;
        }

        canvas.translate(0, getPaddingTop());

        float centerX = getMeasuredWidth() / 2f;
        float y = measuredSpacing;

        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.DEFAULT);

        for (int i = 0; i < sectionCache.length; i++) {
            if (debugPaint != null) {
                debugRect.set(0, y - measuredSpacing, getMeasuredWidth() - 1, y + measuredSpacing + textHeight);
                canvas.drawRect(debugRect, debugPaint);
            }

            y += textHeight;

            if (touchState == TOUCH_SCROLL) {
                paintProperty.setValue(textPaint);
                stringProperty.setValue(sectionCache[i]);

                for (DecorationItem decoration : decorations) {
                    decoration.onDraw(canvas, stringProperty, paintProperty, i, Math.abs(i - sectionIndex));
                }

                canvas.drawText(stringProperty.getValue(), centerX, y, paintProperty.getValue());
            } else {
                canvas.drawText(sectionCache[i], centerX, y, textPaint);
            }

            y += measuredSpacing * 2 + spacing;
        }
    }

    private void raiseOnSectionScrolledListener(int section) {
        for (OnSectionScrolledListener listener : listeners) {
            listener.onSectionScrolled(indexer, section);
        }
    }

    public void addDecoration(DecorationItem decoration) {
        decorations.add(decoration);
        invalidate();
    }

    public void removeDecoration(DecorationItem decoration) {
        decorations.remove(decoration);
        invalidate();
    }

    public void addOnSectionScrolledListener(OnSectionScrolledListener listener) {
        listeners.add(listener);
    }

    public void removeOnSectionScrolledListener(OnSectionScrolledListener listener) {
        listeners.remove(listener);
    }

    public interface OnSectionScrolledListener {
        void onSectionScrolled(SectionIndexer indexer, int section);
    }

    public interface DecorationItem {
        void onDraw(Canvas canvas, Property<String> text, Property<Paint> paint, int index, int distance);
    }

    private class PreviewSectionIndexer implements SectionIndexer {
        private Object[] sections;

        public PreviewSectionIndexer() {
            sections = new Object[10];

            for (int i = 0; i < sections.length; i++) {
                sections[i] = String.valueOf(i);
            }
        }

        @Override
        public Object[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }
}
