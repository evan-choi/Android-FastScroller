# Android-FastScroll

[![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=14)

`FastSroller` for Android [SectionIndexer](https://developer.android.com/reference/android/widget/SectionIndexer) interface

Supports only vertical mode in this library.

## Preview

![Preview](https://github.com/SteaI/Android-FastScroller/blob/master/preview/preview.gif?raw=true)

## Gradle

```
COMING SOON
```

## Basic Usage

### xml


``` xml
<com.steal.FastScroller
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:fs_debug="true"
    app:fs_sectionHeight="24dp"
    app:fs_sectionWidth="24dp"
    app:fs_spacing="12dp"
    app:fs_textColor="#ff0000"
    app:fs_textSize="14sp" />
```

### Setup

``` java
scroller.setSectionIndexer(new SectionIndexer() {
    @Override
    public Object[] getSections() {
        // implements SectionIndexer.getSections
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        // implements SectionIndexer.getPositionForSection
    }

    @Override
    public int getSectionForPosition(int position) {
        // implements SectionIndexer.getSectionForPosition
    }
});
```

### Event Listener

``` java
scroller.addOnSectionScrolledListener(new OnSectionScrolledListener() {
    @override
    public void onSectionScrolled(SectionIndexer indexer, int section) {
        // your code in here
        // ex) indexer.getPositionForSection(section)
    }
});
```

in java >= 1.8

``` java
scroller.addOnSectionScrolledListener((indexer, section) => {
    // your code in here
    // ex) indexer.getPositionForSection(section)
});
```

---

## Decoration

### Interface

``` java
public interface DecorationItem {
    void onDraw(
        Canvas canvas,         // canvas
        Property<String> text, // section text
        Property<Paint> paint, // section text paint (TextPaint)
        int index,             // section index
        int distance);         // distance from section index (selected index distance is 0)
}
```

### Sample

``` java
// Bolding to selected section index
scroller.addDecoration(new FastScroller.DecorationItem() {
    @Override
    public void onDraw(Canvas canvas, Property<String> text, Property<Paint> paint, int index, int distance) {
        if (distance == 0) {
            paint.getValue().setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            paint.getValue().setTypeface(Typeface.DEFAULT);
        }
    }
});
```

## ENJOY!
