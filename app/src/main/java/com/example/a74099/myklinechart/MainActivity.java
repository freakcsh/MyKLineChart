package com.example.a74099.myklinechart;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private CombinedChart combinedchart;
    private BarChart barChart;
    private DataParse mData;
    private ArrayList<KLineBean> kLineDatas;
    XAxis xAxisBar, xAxisK;
    YAxis axisLeftBar, axisLeftK;
    YAxis axisRightBar, axisRightK;
    BarDataSet barDataSet;
    private BarLineChartTouchListener mChartTouchListener;
    private CoupleChartGestureListener coupleChartGestureListener;
    float sum = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            barChart.setAutoScaleMinMaxEnabled(true);
            combinedchart.setAutoScaleMinMaxEnabled(true);

            combinedchart.notifyDataSetChanged();
            barChart.notifyDataSetChanged();

            combinedchart.invalidate();
            barChart.invalidate();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        combinedchart = findViewById(R.id.combinedchart);
        barChart = findViewById(R.id.barchart);
        initChart();
        getOffLineData();
    }

    private void getOffLineData() {
           /*方便测试，加入假数据*/
        mData = new DataParse();
        JSONObject object = null;
        try {
            object = new JSONObject(ConstantTest.KLINEURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mData.parseKLine(object);

        mData.getKLineDatas();


        setData(mData);
    }

    @NonNull
    private LineDataSet setMaLine(int ma, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + ma);
        if (ma == 5) {
            lineDataSetMa.setHighlightEnabled(true);
            lineDataSetMa.setDrawHorizontalHighlightIndicator(false);
            lineDataSetMa.setHighLightColor(Color.WHITE);
        } else {/**此处必须得写*/
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setDrawValues(false);
        if (ma == 5) {
            lineDataSetMa.setColor(Color.GREEN);
        } else if (ma == 10) {
            lineDataSetMa.setColor(Color.GRAY);
        } else {
            lineDataSetMa.setColor(Color.YELLOW);
        }
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);
        return lineDataSetMa;
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    private float getSum(Integer a, Integer b) {

        for (int i = a; i <= b; i++) {
            sum += mData.getKLineDatas().get(i).close;
        }
        return sum;
    }

    private void setData(DataParse mData) {

        kLineDatas = mData.getKLineDatas();
        int size = kLineDatas.size();   //点的个数
        // axisLeftBar.setAxisMaxValue(mData.getVolmax());
        String unit = MyUtils.getVolUnit(mData.getVolmax());
        int u = 1;
        if ("万手".equals(unit)) {
            u = 4;
        } else if ("亿手".equals(unit)) {
            u = 8;
        }
        axisLeftBar.setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
        // axisRightBar.setAxisMaxValue(mData.getVolmax());
        Log.e("@@@", mData.getVolmax() + "da");

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<CandleEntry> candleEntries = new ArrayList<>();
        ArrayList<Entry> line5Entries = new ArrayList<>();
        ArrayList<Entry> line10Entries = new ArrayList<>();
        ArrayList<Entry> line30Entries = new ArrayList<>();
        for (int i = 0, j = 0; i < mData.getKLineDatas().size(); i++, j++) {
            xVals.add(mData.getKLineDatas().get(i).date + "");
            barEntries.add(new BarEntry(mData.getKLineDatas().get(i).vol, i));
            candleEntries.add(new CandleEntry(i, mData.getKLineDatas().get(i).high, mData.getKLineDatas().get(i).low, mData.getKLineDatas().get(i).open, mData.getKLineDatas().get(i).close));
            if (i >= 4) {
                sum = 0;
                line5Entries.add(new Entry(getSum(i - 4, i) / 5, i));
            }
            if (i >= 9) {
                sum = 0;
                line10Entries.add(new Entry(getSum(i - 9, i) / 10, i));
            }
            if (i >= 29) {
                sum = 0;
                line30Entries.add(new Entry(getSum(i - 29, i) / 30, i));
            }

        }
        barDataSet = new BarDataSet(barEntries, "成交量");
        barDataSet.setBarSpacePercent(50); //bar空隙
        barDataSet.setHighlightEnabled(true);
        barDataSet.setHighLightAlpha(255);
        barDataSet.setHighLightColor(Color.WHITE);
        barDataSet.setDrawValues(false);
        barDataSet.setColor(Color.RED);
        BarData barData = new BarData(xVals, barDataSet);
        barChart.setData(barData);
        final ViewPortHandler viewPortHandlerBar = barChart.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        final float xscale = 3;
        touchmatrix.postScale(xscale, 1f);


        CandleDataSet candleDataSet = new CandleDataSet(candleEntries, "KLine");
        candleDataSet.setDrawHorizontalHighlightIndicator(false);
        candleDataSet.setHighlightEnabled(true);
        candleDataSet.setHighLightColor(Color.WHITE);
        candleDataSet.setValueTextSize(10f);
        candleDataSet.setDrawValues(false);
        candleDataSet.setColor(Color.RED);
        candleDataSet.setShadowWidth(1f);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        CandleData candleData = new CandleData(xVals, candleDataSet);


        ArrayList<ILineDataSet> sets = new ArrayList<>();

        /******此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
        if (size >= 30) {
            sets.add(setMaLine(5, xVals, line5Entries));
            sets.add(setMaLine(10, xVals, line10Entries));
            sets.add(setMaLine(30, xVals, line30Entries));
        } else if (size >= 10 && size < 30) {
            sets.add(setMaLine(5, xVals, line5Entries));
            sets.add(setMaLine(10, xVals, line10Entries));
        } else if (size >= 5 && size < 10) {
            sets.add(setMaLine(5, xVals, line5Entries));
        }


        CombinedData combinedData = new CombinedData(xVals);
        LineData lineData = new LineData(xVals, sets);
        combinedData.setData(candleData);
        combinedData.setData(lineData);
        combinedchart.setData(combinedData);
        combinedchart.moveViewToX(mData.getKLineDatas().size() - 1);
        final ViewPortHandler viewPortHandlerCombin = combinedchart.getViewPortHandler();
        viewPortHandlerCombin.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        final float xscaleCombin = 3;
        matrixCombin.postScale(xscaleCombin, 1f);

        combinedchart.moveViewToX(mData.getKLineDatas().size() - 1);
        barChart.moveViewToX(mData.getKLineDatas().size() - 1);
        setOffset();

/****************************************************************************************
 此处解决方法来源于CombinedChartDemo，k线图y轴显示问题，图表滑动后才能对齐的bug，希望有人给出解决方法
 (注：此bug现已修复，感谢和chenguang79一起研究)
 ****************************************************************************************/

        handler.sendEmptyMessageDelayed(0, 300);

    }

    /**
     * 设置量表对齐
     */
    private void setOffset() {
        float lineLeft = combinedchart.getViewPortHandler().offsetLeft();
        float barLeft = barChart.getViewPortHandler().offsetLeft();
        float lineRight = combinedchart.getViewPortHandler().offsetRight();
        float barRight = barChart.getViewPortHandler().offsetRight();
        float barBottom = barChart.getViewPortHandler().offsetBottom();
        float offsetLeft, offsetRight;
        float transLeft = 0, transRight = 0;
        /**注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (barLeft < lineLeft) {
           /* offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            barChart.setExtraLeftOffset(offsetLeft);*/
            transLeft = lineLeft;
        } else {
            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
            combinedchart.setExtraLeftOffset(offsetLeft);
            transLeft = barLeft;
        }
        /**注：setExtraRight...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (barRight < lineRight) {
          /*  offsetRight = Utils.convertPixelsToDp(lineRight);
            barChart.setExtraRightOffset(offsetRight);*/
            transRight = lineRight;
        } else {
            offsetRight = Utils.convertPixelsToDp(barRight);
            combinedchart.setExtraRightOffset(offsetRight);
            transRight = barRight;
        }
        barChart.setViewPortOffsets(transLeft, 15, transRight, barBottom);
    }

    private void initChart() {
        /********************************** 以下是条形柱形图的设置 ********************************************/
        /**
         * 将边框绘制为true。如果启用此功能，则没有必要绘制x轴和y轴的轴线。
         */
        barChart.setDrawBorders(true);
        /**
         * 设置dp中边框线的宽度。
         */
        barChart.setBorderWidth(1);
        /**
         * 设置图表边框线的颜色。
         */
        barChart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        /**
         * 设置出现在图表右下角的description text, size = Y-legend文本大小
         */
        barChart.setDescription("");
        /**
         * 将其设置为true以启用对图表的拖动(用手指移动图表)(这不会影响缩放)。
         */
        barChart.setDragEnabled(true);
        barChart.setScaleYEnabled(false);
        /**
         * Legend  表示图表图例的类。图例将包含每个颜色和数据集的一个条目。一个数据集中的多个颜色被分组在一起。在将数据设置到图表之前，传奇对象不可用。
         * getLegend() 返回图表的Legend对象。此方法可用于获取传奇实例，以便自定义自动生成的传奇。
         */
        Legend barChartLegend = barChart.getLegend();
        /**
         * 设置该组件应该被启用(应该被绘制)，则将其设置为true，如果不是，则设置为false。如果禁用，则不会绘制此组件的任何内容。默认值:true
         */
        barChartLegend.setEnabled(false);

        //BarYAxisFormatter  barYAxisFormatter=new BarYAxisFormatter();
        //bar x y轴
        /**
         * XAxis 类表示x轴标签设置。只使用setter方法修改它。不要直接访问公共变量。请注意，并不是xlabel类提供的所有特性都适合于RadarChart。
         */
        /**
         * getXAxis() 返回表示所有x-label的对象，此方法可用于获取XAxis对象并对其进行修改(例如更改标签的位置、样式等)。
         */
        xAxisBar = barChart.getXAxis();

        /**
         *  setDrawLabels(true) 将其设置为true以启用绘制此轴的标签(这不会影响绘制网格线或轴线)。
         */
        xAxisBar.setDrawLabels(true);
        /**
         * setDrawGridLines() 将其设置为true，以便为该轴绘制网格线。
         */
        xAxisBar.setDrawGridLines(false);
        /**
         * setDrawAxisLine() 设置轴边的线应该画还是不画，则将其设置为true。
         */
        xAxisBar.setDrawAxisLine(false);
        /**
         * 设置x轴标签字体颜色
         */
        xAxisBar.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        /**
         * 设置x标签的位置
         */
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        /**
         * 设置此轴的网格线的颜色(来自每个标签的水平线)。
         */
        xAxisBar.setGridColor(getResources().getColor(R.color.minute_grayLine));


        /**
         * getAxisLeft() 返回左y轴对象。在水平条形图中，这是上轴。
         */
        axisLeftBar = barChart.getAxisLeft();
        /**
         * 设置此轴的自定义最小值。如果设置此值，则不会根据提供的数据自动计算此值。使用resetAxisMinValue()来撤销它。如果使用此方法，请不要忘记调用setStartAtZero(false)。否则，轴最小值仍将被强制为0。
         */
        axisLeftBar.setAxisMinValue(0);
        /**
         * 将其设置为true，以便为该轴绘制网格线。
         */
        axisLeftBar.setDrawGridLines(false);
        /**
         * setDrawAxisLine() 设置轴边的线应该画还是不画，则将其设置为true。
         */
        axisLeftBar.setDrawAxisLine(false);
        /**
         * 设置左y轴字体颜色
         */
        axisLeftBar.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        /**
         * 将其设置为true以启用绘制此轴的标签(这不会影响绘制网格线或轴线)。
         */
        axisLeftBar.setDrawLabels(true);
        /**
         * 设置最大的轴空间百分比的全部范围。默认10 f
         */
        axisLeftBar.setSpaceTop(0);
        /**
         * 如果启用，ylabel将只显示图表的最小值和最大值。这将忽略 / 覆盖set标签计数。
         */
        axisLeftBar.setShowOnlyMinMax(true);
        /**
         * getAxisRight() 返回右y轴对象。在水平条形图中，这是上轴。
         */
        axisRightBar = barChart.getAxisRight();
        /**
         * 将其设置为true以启用绘制此轴的标签(这不会影响绘制网格线或轴线)。
         */
        axisRightBar.setDrawLabels(false);
        /**
         * 将其设置为true，以便为该轴绘制网格线。
         */
        axisRightBar.setDrawGridLines(false);
        /**
         * setDrawAxisLine() 设置轴边的线应该画还是不画，则将其设置为true。
         */
        axisRightBar.setDrawAxisLine(false);


        /******************************* 以下是K线图的设置 *********************************/
        combinedchart.setDrawBorders(true);
        combinedchart.setBorderWidth(1);
        combinedchart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        combinedchart.setDescription("");
        combinedchart.setDragEnabled(true);
        combinedchart.setScaleYEnabled(false);

        Legend combinedchartLegend = combinedchart.getLegend();
        combinedchartLegend.setEnabled(false);
        //bar x y轴
        xAxisK = combinedchart.getXAxis();
        xAxisK.setDrawLabels(true);
        xAxisK.setDrawGridLines(false);
        xAxisK.setDrawAxisLine(false);
        xAxisK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        xAxisK.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisK.setGridColor(getResources().getColor(R.color.minute_grayLine));

        axisLeftK = combinedchart.getAxisLeft();
        axisLeftK.setDrawGridLines(true);
        axisLeftK.setDrawAxisLine(false);
        axisLeftK.setDrawLabels(true);
        axisLeftK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        axisLeftK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftK.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRightK = combinedchart.getAxisRight();
        axisRightK.setDrawLabels(false);
        axisRightK.setDrawGridLines(true);
        axisRightK.setDrawAxisLine(false);
        axisRightK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        combinedchart.setDragDecelerationEnabled(true);
        barChart.setDragDecelerationEnabled(true);
        combinedchart.setDragDecelerationFrictionCoef(0.2f);
        barChart.setDragDecelerationFrictionCoef(0.2f);


        // 将K线控的滑动事件传递给交易量控件
        combinedchart.setOnChartGestureListener(new CoupleChartGestureListener(combinedchart, new Chart[]{barChart}));
        // 将交易量控件的滑动事件传递给K线控件
        barChart.setOnChartGestureListener(new CoupleChartGestureListener(barChart, new Chart[]{combinedchart}));
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.e("%%%%", h.getXIndex() + "");
                combinedchart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                combinedchart.highlightValue(null);
            }
        });
        combinedchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                barChart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                barChart.highlightValue(null);
            }
        });


    }
}
