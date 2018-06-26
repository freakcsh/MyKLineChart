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
            /**
             * 柱形图 标记，指示是否启用y轴上的自动缩放。这对于显示财务数据的图表尤其有趣。
             */
            barChart.setAutoScaleMinMaxEnabled(true);
            /**
             * K线图 标记，指示是否启用y轴上的自动缩放。这对于显示财务数据的图表尤其有趣。
             */
            combinedchart.setAutoScaleMinMaxEnabled(true);
            /**
             * 更新数据
             */
            combinedchart.notifyDataSetChanged();
            barChart.notifyDataSetChanged();
            /**
             *  invalidate() 整个视图无效。如果视图是可见的，{@link #onDraw(android.graphics.Canvas)}将在将来的某个时候被调用。
             *  这必须从UI线程调用。要从非ui线程调用，请调用{@link #postInvalidate()}。
             */
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
        /**
         * 初始化k线图玉柱形图
         */
        initChart();
        /**
         * 获取离线数据
         */
        getOffLineData();
    }

    private void getOffLineData() {
        /**方便测试，加入假数据*/
        mData = new DataParse();
        JSONObject object = null;
        try {
            object = new JSONObject(ConstantTest.KLINEURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**
         * 解析K线图数据
         */
        mData.parseKLine(object);
        /**
         * 获取k线图数据
         */
        mData.getKLineDatas();

        /**
         * 设置k线图数据
         */
        setData(mData);
    }

    @NonNull
    private LineDataSet setMaLine(int ma, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + ma);
        if (ma == 5) {
            /**
             * 设置是否启用高亮
             */
            lineDataSetMa.setHighlightEnabled(true);
            /**
             * 启用/禁用水平高光指示器。如果禁用，则不绘制指示器。
             */
            lineDataSetMa.setDrawHorizontalHighlightIndicator(false);
            lineDataSetMa.setHighLightColor(Color.WHITE);
        } else {/**此处必须得写*/
            lineDataSetMa.setHighlightEnabled(false);
        }
        /**
         * 设置是否绘制连线数据
         */
        lineDataSetMa.setDrawValues(true);
        /**
         * 设置连线数据颜色
         */
        lineDataSetMa.setValueTextColor(Color.WHITE);
        if (ma == 5) {
            /**
             * 设置线条颜色
             */
            lineDataSetMa.setColor(Color.GREEN);
        } else if (ma == 10) {
            lineDataSetMa.setColor(Color.GRAY);
        } else {
            lineDataSetMa.setColor(Color.WHITE);
        }
        /**
         * 设置线条大小
         */
        lineDataSetMa.setLineWidth(1f);
        /**
         * 将其设置为true，以便为该数据集绘制圆形指示器，默认为true
         * 设置为true是，会在节点上绘制一个圆形的指示器
         */
        lineDataSetMa.setDrawCircles(false);
        /**
         * 是否启用外圆，也就是环形样式
         */
        lineDataSetMa.setDrawCircleHole(false);
        /**
         * 设置圆形颜色
         */
        lineDataSetMa.setCircleColor(Color.BLUE);
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

    /**
     * 设置k线图数据
     *
     * @param mData 假数据对象
     */
    private void setData(DataParse mData) {
        /**
         * 获取k线图数据
         */
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
        /**
         * 设置用于格式化轴标签的格式化程序。如果没有设置格式化程序，图表将自动为图表中绘制的所有值确定合理的格式(关于小数)。使用chart.getDefaultValueFormatter()来使用图表计算的格式化程序。
         */
        axisLeftBar.setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
        // axisRightBar.setAxisMaxValue(mData.getVolmax());
        Log.e("@@@", mData.getVolmax() + "da");
        /**
         * k线图x轴数据
         */
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<CandleEntry> candleEntries = new ArrayList<>();
        ArrayList<Entry> line5Entries = new ArrayList<>();
        ArrayList<Entry> line10Entries = new ArrayList<>();
        ArrayList<Entry> line30Entries = new ArrayList<>();
        for (int i = 0, j = 0; i < mData.getKLineDatas().size(); i++, j++) {
            //mData.getKLineDatas().get(i).date 日期信息 如：2015-01-28 也就是 x轴的显示数据
            /**
             * k线图与柱形图x轴数据
             * date 日期信息 如：2015-01-08 也就是 x轴的显示数据
             */
            xVals.add(mData.getKLineDatas().get(i).date + "");
            /**
             * 柱形图数据
             * vol 数据格式如：185103.0
             * vol 代表柱形图的高度
             */
            barEntries.add(new BarEntry(mData.getKLineDatas().get(i).vol, i));
            /**
             * 蜡烛数据
             * high 数据格式如：19.25
             * low 数据格式如：18.36
             * open 数据格式如 ：19.12
             * close 数据格式如：19.0
             */
            candleEntries.add(new CandleEntry(i, mData.getKLineDatas().get(i).high, mData.getKLineDatas().get(i).low, mData.getKLineDatas().get(i).open, mData.getKLineDatas().get(i).close));
            if (i >= 4) {
                sum = 0;
                /**
                 * Entry 一个条目表示图表中的一个条目。
                 * 参数 ：
                 * val：y值(条目的实际值)
                 * i：x值数组中相应的索引(在图的x轴上的索引，不得高于x值字符串数组的长度)
                 */
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
       //TODO /********************************* 以下是柱形图的设置 ***********************************/

        /**
         * 设置柱形图数据、
         * BarDataSet(List<BarEntry> yVals, String label) 参数
         * yVals：柱形图 Y 轴数据
         * label：标题
         */
        barDataSet = new BarDataSet(barEntries, "成交量");
        /**
         * 以条宽的百分比(0-100)设置条之间的空间
         */
        barDataSet.setBarSpacePercent(50); //bar空隙
        /**
         * 设置是否启动高亮
         */
        barDataSet.setHighlightEnabled(true);
        /**
         * 设置高亮透明度
         */
        barDataSet.setHighLightAlpha(255);
        /**
         * 设置选中时线条的高亮颜色
         */
        barDataSet.setHighLightColor(Color.WHITE);
        /**
         * 设置是否绘制柱形图 Y 轴数据
         */
        barDataSet.setDrawValues(true);
        /**
         * 设置绘制的 Y 轴数据颜色
         */
        barDataSet.setValueTextColor(Color.WHITE);
        /**
         * 设置这个数据集应该使用的唯一颜色。在内部，它重新创建颜色数组并添加指定的颜色。
         * 设置柱形图图柱子颜色
         */
        barDataSet.setColor(Color.parseColor("#00c882"));
        /**
         * BarData 适用于所有线、杆、散点、蜡烛和气泡数据。
         * BarData(List<String> xVals, IBarDataSet dataSet)
         * 参数：
         * xVals：x 轴数据
         * dataSet：Y 轴数据
         */
        BarData barData = new BarData(xVals, barDataSet);
        /**
         * 设置柱形图数据
         */
        barChart.setData(barData);

        //TODO /******************************************   以下是K线图数据 *****************************************************/

        /**
         * getViewPortHandler() 返回图表的ViewPortHandler，该视图负责图表的内容区域及其偏移量和维度。
         *  ViewPortHandler 类，该类包含关于图表当前视图设置的信息，包括偏移量、比例和转换级别……
         */
        final ViewPortHandler viewPortHandlerBar = barChart.getViewPortHandler();
        /**
         * 设置x轴的最大比例因子
         */
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(xVals.size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        final float xscale = 3;
        /**
         * 用指定的比例变换矩阵。M' = S(sx, sy) * M
         */
        touchmatrix.postScale(xscale, 1f);

        /**
         * CandleDataSet(List<CandleEntry> yVals, String label)
         * 参数：
         * yVals：Y 轴数据
         * label：标题
         */
        CandleDataSet candleDataSet = new CandleDataSet(candleEntries, "KLine");
        /**
         * 启用/禁用水平高光指示器。如果禁用，则不绘制指示器。
         */
        candleDataSet.setDrawHorizontalHighlightIndicator(true);
        /**
         * 设置是否启用高亮
         */
        candleDataSet.setHighlightEnabled(true);
        /**
         * 设置选中时线条的高亮颜色
         */
        candleDataSet.setHighLightColor(Color.WHITE);
        /**
         * 设置值的字体大小
         */
        candleDataSet.setValueTextSize(10f);
        /**
         * 设置是否绘制数据
         * 绘制的数据是最大的数据进行四舍五入，只取以为小数，这里去的是 high 字段
         */
        candleDataSet.setDrawValues(true);
        /**
         * 设置绘制的字体颜色
         */
        candleDataSet.setValueTextColor(Color.WHITE);
        /**
         * 设置蜡烛显示的颜色
         */
        candleDataSet.setColor(Color.parseColor("#e86e42"));
        /**
         * 以像素为单位设置蜡烛阴影线的宽度。默认3 f。
         */
        candleDataSet.setShadowWidth(1f);
        /**
         * setAxisDependency(YAxis.AxisDependency.LEFT)：设置依赖关系
         * YAxis ：表示y轴标签设置及其条目的类。只使用setter方法修改它。不要直接访问公共变量。请注意，并不是ylabel类提供的所有特性都适合于RadarChart。在为图表设置数据之前，需要应用影响axis值范围的自定义。
         * AxisDependency：指定数据集应该标定的轴的枚举，可以是左的，也可以是右的。
         */
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        /**
         * 设置蜡烛数据
         */
        CandleData candleData = new CandleData(xVals, candleDataSet);


        ArrayList<ILineDataSet> sets = new ArrayList<>();

        /******此处修复如果显示的点的个数达不到 MA 均线的位置所有的点都从0开始计算最小值的问题******************************/
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
        /**
         * getViewPortHandler()
         * 返回图表的ViewPortHandler，该图表负责图表的内容区域及其偏移量和维度。
         */
        final ViewPortHandler viewPortHandlerCombin = combinedchart.getViewPortHandler();
        /**
         * 设置x轴的最大比例因子
         */
        viewPortHandlerCombin.setMaximumScaleX(culcMaxscale(xVals.size()));
        /**
         * 返回用于平移和缩放的图表触摸矩阵。
         */
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        final float xscaleCombin = 3;
        /**
         * 使用指定的比例后矩阵。 M'= S（sx，sy）* M
         */
        matrixCombin.postScale(xscaleCombin, 1f);
        /**
         * 将当前视口的左侧移动到指定的x-index。 这也通过调用invalidate（）来刷新图表。
         */
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
        /****************************************************************/

        /**
         * 将边界矩形绘制为真。如果这是启用的，那么绘制x轴和y轴的轴线是没有意义的。
         */
        combinedchart.setDrawBorders(true);
        /**
         * 在dp中设置边界线的宽度。
         */
        combinedchart.setBorderWidth(1);
        /**
         * 设置图表边框的颜色。
         */
        combinedchart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        /**
         * 设置一个描述文本，它出现在图表的右下角，大小=y-图例文本大小
         */
        combinedchart.setDescription("");
        combinedchart.setDragEnabled(true);
        /**
         * 将其设置为true，以支持拖动（用手指移动图表）（这不会影响缩放）。
         */
        combinedchart.setScaleYEnabled(false);
        /**
         * Legend 代表图表图例的类。这个图例将包含每个颜色和数据集的一个条目。一个数据集中的多个颜色组合在一起。图例对象在将数据设置到图表之前是不可用的。
         *
         * getLegend() 返回图表的图例对象。这个方法可以用来获得图例的实例，以便定制自动生成的图例。
         */
        Legend combinedchartLegend = combinedchart.getLegend();
        /**
         * 如果这个组件应该被启用（应该被绘制），如果不是，则将其设置为true。如果禁用，则不会绘制此组件。默认值:true
         */
        combinedchartLegend.setEnabled(false);
        //bar x y轴
        /**
         * 返回表示所有x标签的对象，此方法可用于获取XAxis对象并对其进行修改（例如，更改标签位置，样式等）
         */
        xAxisK = combinedchart.getXAxis();
        /**
         * 将其设置为true以启用绘制该轴的标签（这不会影响绘制网格线或轴线）。
         */
        xAxisK.setDrawLabels(true);
        /**
         * 将其设置为true以启用绘制该轴的网格线。
         */
        xAxisK.setDrawGridLines(false);
        /**
         * 如果应绘制轴旁边的线条，则将其设置为true。
         */
        xAxisK.setDrawAxisLine(false);
        /**
         * 设置用于标签的文字颜色。 确保在使用资源中的颜色时使用getResources（）.getColor（...）。
         */
        xAxisK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        /**
         * 设置x标签的位置
         */
        xAxisK.setPosition(XAxis.XAxisPosition.BOTTOM);
        /**
         * 设置此轴网格线的颜色（来自每个标签的水平线）。
         */
        xAxisK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        /**
         * 返回左侧的y轴对象。 在水平条形图中，这是顶轴。
         */
        axisLeftK = combinedchart.getAxisLeft();
        /**
         * 将其设置为true以启用绘制该轴的网格线。
         */
        axisLeftK.setDrawGridLines(true);
        /**
         * 如果应绘制轴旁边的线条，则将其设置为true。
         */
        axisLeftK.setDrawAxisLine(false);
        /**
         * 将其设置为true以启用绘制该轴的标签（这不会影响绘制网格线或轴线）。
         */
        axisLeftK.setDrawLabels(true);
        /**
         * 设置用于标签的文字颜色。 确保在使用资源中的颜色时使用getResources（）。getColor（...）。
         */
        axisLeftK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        /**
         * 设置此轴网格线的颜色（来自每个标签的水平线）。
         */
        axisLeftK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        /**
         * 设置y标签的位置
         */
        axisLeftK.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRightK = combinedchart.getAxisRight();
        axisRightK.setDrawLabels(false);
        axisRightK.setDrawGridLines(true);
        axisRightK.setDrawAxisLine(false);
        axisRightK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        /**
         * 如果设置为true，则触摸后图表会继续滚动。 默认值：true。
         */
        combinedchart.setDragDecelerationEnabled(true);
        /**
         * 如果设置为true，则触摸后图表会继续滚动。 默认值：true。
         */
        barChart.setDragDecelerationEnabled(true);
        /**
         * 减速摩擦系数在[0; 1]间隔时，较高的值表示速度将缓慢下降，例如，如果设置为0，则会立即停止。 1是一个无效值，将自动转换为0.999f。
         */
        combinedchart.setDragDecelerationFrictionCoef(0.2f);
        /**
         * 减速摩擦系数在[0; 1]间隔时，较高的值表示速度将缓慢下降，例如，如果设置为0，则会立即停止。 1是一个无效值，将自动转换为0.999f。
         */
        barChart.setDragDecelerationFrictionCoef(0.2f);


        // 将K线控的滑动事件传递给交易量控件
        /**
         * 在图表表面上执行手势时，为自定义回调设置图表的手势监听器。
         */
        combinedchart.setOnChartGestureListener(new CoupleChartGestureListener(combinedchart, new Chart[]{barChart}));
        // 将交易量控件的滑动事件传递给K线控件
        barChart.setOnChartGestureListener(new CoupleChartGestureListener(barChart, new Chart[]{combinedchart}));
        /**
         * 为图表设置一个选择监听器
         */
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
