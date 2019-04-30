package com.steal.project;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.steal.FastScroller;
import com.steal.common.Property;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FastScroller scroller = findViewById(R.id.indexer_scroller);

        scroller.setSectionIndexer(new PreviewSectionIndexer());

        // alpha decoration
        scroller.addDecoration(new FastScroller.DecorationItem() {
            @Override
            public void onDraw(Canvas canvas, Property<String> text, Property<Paint> paint, int index, int distance) {
                float progress = Math.min(distance, 4) / 4f;

                if (distance == 0) {
                    progress = 1;
                }

                paint.getValue().setAlpha(50 + (int) (progress * 205));
            }
        });

        // text size decoration
        scroller.addDecoration(new FastScroller.DecorationItem() {
            @Override
            public void onDraw(Canvas canvas, Property<String> text, Property<Paint> paint, int index, int distance) {
                float progress = 0.5f + Math.min(distance, 4) / 4f * 0.5f;

                if (distance == 0) {
                    progress = 1f;
                }

                paint.getValue().setTextSize(scroller.getTextSize() * progress);
            }
        });

        // bold decoration
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

        // string property
        scroller.addDecoration(new FastScroller.DecorationItem() {
            @Override
            public void onDraw(Canvas canvas, Property<String> text, Property<Paint> paint, int index, int distance) {
                if (distance == 0) {
                    text.setValue("★");
                }
            }
        });
    }

    private class PreviewSectionIndexer implements SectionIndexer {
        private Object[] sections;

        public PreviewSectionIndexer() {
            String preset = "ABCDEFGHIJKLMNOPQRSTUVWXYZㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ0123456789";
            sections = new Object[preset.length()];

            for (int i = 0; i < preset.length(); i++) {
                sections[i] = preset.charAt(i);
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
