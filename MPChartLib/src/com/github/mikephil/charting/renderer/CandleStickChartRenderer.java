
package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * 画蜡烛图、值、高亮
 */
public class CandleStickChartRenderer extends LineScatterCandleRadarRenderer {

    protected CandleDataProvider mChart;

    private float[] mShadowBuffers = new float[8];
    private float[] mBodyBuffers = new float[4];
    private float[] mRangeBuffers = new float[4];
    private float[] mOpenBuffers = new float[4];
    private float[] mCloseBuffers = new float[4];

    public CandleStickChartRenderer(CandleDataProvider chart, ChartAnimator animator,
                                    ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
    }

    @Override
    public void initBuffers() {

    }

    @Override
    public void drawData(Canvas c) {

        CandleData candleData = mChart.getCandleData();

        for (ICandleDataSet set : candleData.getDataSets()) {

            if (set.isVisible() && set.getEntryCount() > 0)
                drawDataSet(c, set);
        }
    }

    @SuppressWarnings("ResourceAsColor")
    protected void drawDataSet(Canvas c, ICandleDataSet dataSet) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        float phaseX = Math.max(0.f, Math.min(1.f, mAnimator.getPhaseX()));
        /**
         * 这会得到用于动画值的y相。
         */
        float phaseY = mAnimator.getPhaseY();
        /**
         * 返回每个蜡烛左侧和右侧遗漏的空间。
         */
        float barSpace = dataSet.getBarSpace();
        /**
         * 返回蜡烛条是否应该显示？ 如果为false，则只会显示“ticks” 默认为true
         */
        boolean showCandleBar = dataSet.getShowCandleBar();

        int minx = Math.max(mMinX, 0);
        int maxx = Math.min(mMaxX + 1, dataSet.getEntryCount());
        /**
         * setStrokeWidth(dataSet.getShadowWidth())
         * 参数：设置涂料的描边宽度，每当涂料的风格是Stroke或StrokeAndFill时使用。
         *
         * 设置抚摸的宽度。 在发线模式下将0传递给中风。 细线总是绘制一个独立于Canva矩阵的像素。
         */
        mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

        /**
         * draw the body 画出身体
         */
        for (int j = minx,
             count = (int) Math.ceil((maxx - minx) * phaseX + (float) minx);
             j < count;
             j++) {

            /**
             *  get the entry 得到天目
             */
            CandleEntry e = dataSet.getEntryForIndex(j);

            final int xIndex = e.getXIndex();

            if (xIndex < minx || xIndex >= maxx)
                continue;
            /***
             * 返回自身的开放值。
             */
            final float open = e.getOpen();
            /**
             * 返回自身的近距离值
             */
            final float close = e.getClose();
            /**
             * 返回上部阴影最高值。
             */
            final float high = e.getHigh();
            /**
             * 返回阴影最低值。
             */
            final float low = e.getLow();

            if (showCandleBar) {
                /**
                 * calculate the shadow 计算阴影
                 */

                mShadowBuffers[0] = xIndex;
                mShadowBuffers[2] = xIndex;
                mShadowBuffers[4] = xIndex;
                mShadowBuffers[6] = xIndex;

                if (open > close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = close * phaseY;
                } else if (open < close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = close * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = open * phaseY;
                } else {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = mShadowBuffers[3];
                }
                /***
                 * 用所有矩阵变换点数组。 非常重要：转换时保持矩阵顺序“值 - 触摸偏移”。
                 */
                trans.pointValuesToPixel(mShadowBuffers);

                /**
                 * draw the shadows 画出阴影
                 */

                /**
                 * getShadowColorSameAsCandle()
                 * 阴影颜色与蜡烛颜色相同吗？
                 */
                if (dataSet.getShadowColorSameAsCandle()) {

                    if (open > close)
                    /**
                     * getDecreasingColor()：返回减少的颜色（对于打开>关闭）。
                     * getColor(j)：返回DataSet的颜色数组的给定索引处的颜色。 按模量执行IndexOutOfBounds检查。
                     */
                        mRenderPaint.setColor(
                                dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getDecreasingColor()
                        );

                    else if (open < close)
                        mRenderPaint.setColor(
                                dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getIncreasingColor()
                        );

                    else
                        mRenderPaint.setColor(
                                dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getNeutralColor()
                        );

                } else {
                    mRenderPaint.setColor(
                            dataSet.getShadowColor() == ColorTemplate.COLOR_NONE ?
                                    dataSet.getColor(j) :
                                    dataSet.getShadowColor()
                    );
                }

                mRenderPaint.setStyle(Paint.Style.STROKE);

                c.drawLines(mShadowBuffers, mRenderPaint);

                // calculate the body 计算本身

                mBodyBuffers[0] = xIndex - 0.5f + barSpace;
                mBodyBuffers[1] = close * phaseY;
                mBodyBuffers[2] = (xIndex + 0.5f - barSpace);
                mBodyBuffers[3] = open * phaseY;

                trans.pointValuesToPixel(mBodyBuffers);

                /**
                 * draw body differently for increasing and decreasing entry 以不同的方式增加和减少进入
                 */
                if (open > close) { // decreasing 减少

                    if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
//                        mRenderPaint.setColor(dataSet.getColor(j));
                        mRenderPaint.setColor(Color.parseColor("#e86e42"));
                    } else {
                        mRenderPaint.setColor(dataSet.getDecreasingColor());
                    }
                    /**
                     * getIncreasingPaintStyle()  设置为空心的矩形
                     *
                     * getDecreasingPaintStyle()  设置为实心得矩形样式
                     */
                    mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());

                    c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[3],
                            mBodyBuffers[2], mBodyBuffers[1],
                            mRenderPaint);

                } else if (open < close) { //增加

                    if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
//                        mRenderPaint.setColor(dataSet.getColor(j));
                        mRenderPaint.setColor(Color.parseColor("#00c882"));
                    } else {
                        mRenderPaint.setColor(dataSet.getIncreasingColor());
                    }
                    /**
                     * getIncreasingPaintStyle()  设置为空心的矩形
                     *
                     * getDecreasingPaintStyle() 设置为实心得矩形样式
                     */
                    mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());
                    /**
                     *  使用指定的绘图绘制指定的矩形。 该矩形将根据油漆中的样式进行填充或框定。
                     */
                    c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);
                } else { // equal values 相等

                    if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getNeutralColor());
                    }
                    /**
                     * 使用指定的绘图绘制一条具有指定的起点和停止x，y坐标的线段。
                     * 请注意，由于一条线总是被“框住”，所以在油漆中风格被忽略。
                     * 退化线（长度为0）将不会绘制。
                     *
                     * 参数：
                     *  startX：线的起点的x坐标
                     *  startY：线条起点的y坐标
                     *  paint：用于绘制线条的油漆
                     */
                    c.drawLine(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);
                }
            } else {

                mRangeBuffers[0] = xIndex;
                mRangeBuffers[1] = high * phaseY;
                mRangeBuffers[2] = xIndex;
                mRangeBuffers[3] = low * phaseY;

                mOpenBuffers[0] = xIndex - 0.5f + barSpace;
                mOpenBuffers[1] = open * phaseY;
                mOpenBuffers[2] = xIndex;
                mOpenBuffers[3] = open * phaseY;

                mCloseBuffers[0] = xIndex + 0.5f - barSpace;
                mCloseBuffers[1] = close * phaseY;
                mCloseBuffers[2] = xIndex;
                mCloseBuffers[3] = close * phaseY;

                trans.pointValuesToPixel(mRangeBuffers);
                trans.pointValuesToPixel(mOpenBuffers);
                trans.pointValuesToPixel(mCloseBuffers);

                // draw the ranges 绘制范围
                int barColor;

                if (open > close)
                    barColor = dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getDecreasingColor();
                else if (open < close)
                    barColor = dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getIncreasingColor();
                else
                    barColor = dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getNeutralColor();

                mRenderPaint.setColor(barColor);
                c.drawLine(
                        mRangeBuffers[0], mRangeBuffers[1],
                        mRangeBuffers[2], mRangeBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mOpenBuffers[0], mOpenBuffers[1],
                        mOpenBuffers[2], mOpenBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mCloseBuffers[0], mCloseBuffers[1],
                        mCloseBuffers[2], mCloseBuffers[3],
                        mRenderPaint);

            }
        }
    }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (mChart.getCandleData().getYValCount() < mChart.getMaxVisibleCount()
                * mViewPortHandler.getScaleX()) {

            List<ICandleDataSet> dataSets = mChart.getCandleData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                ICandleDataSet dataSet = dataSets.get(i);

                if (!dataSet.isDrawValuesEnabled() || dataSet.getEntryCount() == 0)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                int minx = Math.max(mMinX, 0);
                int maxx = Math.min(mMaxX + 1, dataSet.getEntryCount());

                float[] positions = trans.generateTransformedValuesCandle(
                        dataSet, mAnimator.getPhaseX(), mAnimator.getPhaseY(), minx, maxx);

                float yOffset = Utils.convertDpToPixel(5f);

                for (int j = 0; j < positions.length; j += 2) {

                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    CandleEntry entry = dataSet.getEntryForIndex(j / 2 + minx);

                    drawValue(c, dataSet.getValueFormatter(), entry.getHigh(), entry, i, x, y - yOffset, dataSet.getValueTextColor(j / 2));
                }
            }
        }
    }

    @Override
    public void drawExtras(Canvas c) {
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        CandleData candleData = mChart.getCandleData();

        for (Highlight high : indices) {

            final int minDataSetIndex = high.getDataSetIndex() == -1
                    ? 0
                    : high.getDataSetIndex();
            final int maxDataSetIndex = high.getDataSetIndex() == -1
                    ? candleData.getDataSetCount()
                    : (high.getDataSetIndex() + 1);
            if (maxDataSetIndex - minDataSetIndex < 1)
                continue;

            for (int dataSetIndex = minDataSetIndex;
                 dataSetIndex < maxDataSetIndex;
                 dataSetIndex++) {

                int xIndex = high.getXIndex(); // get the
                // x-position

                ICandleDataSet set = mChart.getCandleData().getDataSetByIndex(dataSetIndex);

                if (set == null || !set.isHighlightEnabled())
                    continue;

                CandleEntry e = set.getEntryForXIndex(xIndex);

                if (e == null || e.getXIndex() != xIndex)
                    continue;

                float lowValue = e.getLow() * mAnimator.getPhaseY();
                float highValue = e.getHigh() * mAnimator.getPhaseY();
                float y = (lowValue + highValue) / 2f;

                float[] pts = new float[]{
                        xIndex, y
                };

                mChart.getTransformer(set.getAxisDependency()).pointValuesToPixel(pts);

                // draw the lines
                drawHighlightLines(c, pts, set);
            }
        }
    }

}
