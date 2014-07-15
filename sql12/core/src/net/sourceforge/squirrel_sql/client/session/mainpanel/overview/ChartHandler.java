package net.sourceforge.squirrel_sql.client.session.mainpanel.overview;

import net.sourceforge.squirrel_sql.client.gui.mainframe.MainFrame;
import net.sourceforge.squirrel_sql.client.resources.SquirrelResources;
import net.sourceforge.squirrel_sql.client.session.mainpanel.overview.datascale.*;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;
import org.jfree.chart.*;
import org.jfree.chart.block.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class ChartHandler
{
   private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(ChartHandler.class);


   public static final int MAX_LEGEND_ENTRIES = 10;

   public static void doChart(DataScale xAxisDataScale, DataScale yAxisDataScale, DataScaleTable dataScaleTable, Integer callDepth, ChartConfigPanelTabMode chartConfigPanelTabMode, ChartConfigMode mode, SquirrelResources resources, Frame parent)
   {
      try
      {

         String title;

         DataScale newScale;
         JFreeChart chart;
         String label;


         if (chartConfigPanelTabMode == ChartConfigPanelTabMode.XY_CHART)
         {
            title = "Chart for x = " + xAxisDataScale.getColumnDisplayDefinition().getColumnName() + ", y = " + yAxisDataScale.getColumnDisplayDefinition().getColumnName();

            String category = xAxisDataScale.getColumnDisplayDefinition().getColumnName();

            DefaultXYDataset defaultXYDataset = new DefaultXYDataset();


            ArrayList<Double> xValues = dataScaleTable.getDoubleValuesForColumn(xAxisDataScale.getColumnDisplayDefinition());
            ArrayList<Double> yValues = dataScaleTable.getDoubleValuesForColumn(yAxisDataScale.getColumnDisplayDefinition());

            ArrayList<XYPair> pairs = XYPair.createSortedPairs(xValues, yValues);

            double[][] series = new double[2][pairs.size()];

            for (int i = 0; i < pairs.size(); i++)
            {
               series[0][i] = pairs.get(i).getX();
               series[1][i] = pairs.get(i).getY();
            }


            defaultXYDataset.addSeries(title, series);



            chart =  ChartFactory.createXYLineChart(
                     title,   // chart title
                     category,               // domain axis label
                     yAxisDataScale.getColumnDisplayDefinition().getColumnName(),  // range axis label
                     defaultXYDataset,                  // data
                     PlotOrientation.VERTICAL,
                     false,                     // include legend
                     true,                     // tooltips?
                     false                     // URLs?
            );

            label = "";
         }
         else
         {
            ArrayList<Object[]> rows = new ArrayList<Object[]>();

            for (Interval xAxisInterval : xAxisDataScale.getIntervals())
            {
               rows.addAll(xAxisInterval.getResultRows());
            }


            ScaleFactory scaleFactory = new ScaleFactory(rows, xAxisDataScale.getColumnIx(), xAxisDataScale.getColumnDisplayDefinition(), callDepth);

            DataScaleListener dumDataScaleListener = new DataScaleListener()
            {
               @Override
               public void intervalSelected(Interval interval, JButton intervalButtonClicked)
               {
               }

               @Override
               public void showInTableWin(Interval interval)
               {
               }

               @Override
               public void showInTable(Interval interval)
               {
               }
            };



            newScale = scaleFactory.createScale(dumDataScaleListener);


            DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();

            String category = xAxisDataScale.getColumnDisplayDefinition().getColumnName();

            for (Interval interval : newScale.getIntervals())
            {
               categoryDataset.addValue(calculateValue(interval, mode, yAxisDataScale), interval.getLabel(), category);
            }


            title = "Chart for column: " + xAxisDataScale.getColumnDisplayDefinition().getColumnName();

            chart = ChartFactory.createBarChart(
                  title,   // chart title
                  category,               // domain axis label
                  mode.getYAxisLabel(yAxisDataScale),  // range axis label
                  categoryDataset,                  // data
                  PlotOrientation.VERTICAL,
                  false,                     // include legend
                  true,                     // tooltips?
                  false                     // URLs?
            );

            label = createLabel(newScale, rows);

         }

         JFrame f = new JFrame(title);

         final ImageIcon icon = resources.getIcon(SquirrelResources.IImageNames.APPLICATION_ICON);
         if (icon != null)
         {
            f.setIconImage(icon.getImage());
         }


         LegendTitle legendtitle = new LegendTitle(createLegendItemSource(chart));
         BlockContainer blockcontainer = new BlockContainer(new BorderArrangement());
         //blockcontainer.setBorder(new BlockBorder(1.0D, 1.0D, 1.0D, 1.0D));
         LabelBlock labelblock = new LabelBlock(label, new Font("SansSerif", 1, 12));
         //labelblock.setTextAnchor(RectangleAnchor.TOP_LEFT);
         labelblock.setPadding(5D, 5D, 5D, 5D);
         blockcontainer.add(labelblock, RectangleEdge.TOP);
         //LabelBlock labelblock1 = new LabelBlock(createLabel(newScale, rows));
         //labelblock1.setPadding(8D, 20D, 2D, 5D);
         //blockcontainer.add(labelblock1, RectangleEdge.BOTTOM);
         BlockContainer blockcontainer1 = legendtitle.getItemContainer();
         blockcontainer1.setPadding(2D, 10D, 5D, 2D);
         blockcontainer.add(blockcontainer1);
         legendtitle.setWrapper(blockcontainer);
         legendtitle.setPosition(RectangleEdge.BOTTOM);
         legendtitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
         chart.addSubtitle(legendtitle);


         ChartPanel chartPanel = new ChartPanel(chart);
         f.getContentPane().add(chartPanel);


         f.setLocation(parent.getLocationOnScreen());
         f.setSize(parent.getSize());

         f.setVisible(true);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   private static double calculateValue(Interval interval, ChartConfigMode mode, DataScale yAxisDataScale)
   {
      switch(mode)
      {
         case COUNT:
            return interval.getLen();
         case SUM:

            double retSum = 0;
            for (int i = 0; i < interval.getLen(); i++)
            {
               Object o = interval.get(i);
               if (null != o)
               {
                  retSum += ((Number) o).doubleValue();
               }
            }
            return retSum;
         case XY_COUNT_DISTINCT:
            HashSet distinctSet = new HashSet();

            for (int i = 0; i < interval.getLen(); i++)
            {
               int dataSetRowIndex = interval.getDataSetRowIndex(i);
               Object obj = yAxisDataScale.getIndexedColumn().getRow(dataSetRowIndex);
               distinctSet.add(obj);
            }

            return distinctSet.size();
         case XY_SUM_COL:
            double retXYSumCol = 0;

            for (int i = 0; i < interval.getLen(); i++)
            {
               int dataSetRowIndex = interval.getDataSetRowIndex(i);
               Object obj = yAxisDataScale.getIndexedColumn().getRow(dataSetRowIndex);
               if (null != obj)
               {
                  retXYSumCol += ((Number) obj).doubleValue();
               }
            }

            return retXYSumCol;

         default:
            throw new IllegalStateException("Dont know how to handle mode: " + mode);



      }
   }

   private static String createLabel(DataScale dataScale, ArrayList<Object[]> rows)
   {
      String ret = "Contains " + rows.size() + " query result values in " + dataScale.getIntervals().size() + " intervals";

      String intervalWidth = dataScale.getIntervalWidth();
      if(null != intervalWidth)
      {
         ret += " of width " + intervalWidth;
      }

      ret += ".\nLegend shows the first and the last query result value of intervals. Intervals that contain no values are omitted.";


      return ret;
   }

   private static LegendItemSource createLegendItemSource(final JFreeChart chart)
   {
      return new LegendItemSource()
      {
         @Override
         public LegendItemCollection getLegendItems()
         {
            return reduceLegendItems(chart);
         }
      };
   }

   private static LegendItemCollection reduceLegendItems(JFreeChart chart)
   {
      LegendItemCollection legendItems = chart.getPlot().getLegendItems();

      LegendItemCollection ret = new LegendItemCollection();

      for (int i = 0; i < legendItems.getItemCount(); i++)
      {
         if(MAX_LEGEND_ENTRIES < i && MAX_LEGEND_ENTRIES < legendItems.getItemCount())
         {
            ret.add(new LegendItem("..."));
            ret.add(legendItems.get(legendItems.getItemCount() - 1));
            break;
         }

         ret.add(legendItems.get(i));
      }

      return ret;
   }
}
