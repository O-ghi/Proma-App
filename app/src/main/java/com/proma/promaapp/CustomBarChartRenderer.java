package com.proma.promaapp;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomBarChartRenderer extends BarChartRenderer {

    private final float barPadding = 2f; // Set the desired padding between columns

    public CustomBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        // Customize how the value text is drawn in the center of the column
        Paint valuePaint = mRenderPaint;
        valuePaint.setColor(color);
        valuePaint.setTextSize(Utils.convertDpToPixel(12f)); // Adjust the text size as needed
        valuePaint.setTextAlign(Paint.Align.CENTER);

        c.drawText(valueText, x, y - barPadding, valuePaint); // Place the text in the center, adjusted by the padding
    }
}
