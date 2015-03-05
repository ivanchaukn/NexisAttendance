package com.nexis.Fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.text.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.nexis.Constants;
import com.nexis.Activity.MainActivity;
import com.nexis.ParseOperation;
import com.nexis.R;
import com.parse.ParseObject;

import android.os.Bundle;
import com.androidplot.xy.*;

import android.support.v4.app.DialogFragment;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class FragmentStat extends DialogFragment {

	private XYPlot plot1, plot2, plot3;
	private String nexcell;
	private ArrayList<Number> graphDates;
	private static ArrayList<List<Integer>> nexisData = new ArrayList<List<Integer>>(3);
	private static ArrayList<List<Integer>> hsData = new ArrayList<List<Integer>>(3);
	private static ArrayList<List<Integer>> uniData = new ArrayList<List<Integer>>(3);
	private static ArrayList<Integer> domain = new ArrayList<Integer>();
	protected static FragmentStat frag;
	
	public static FragmentStat newInstance() {
		FragmentStat fragment = new FragmentStat();
		Bundle args = new Bundle();
		args.putInt("HData", 2);
		fragment.setArguments(args);
		frag = fragment;
		
		return fragment;
	}

	public FragmentStat() {
	}
	
	public class pointData {
		private String pointDate;
		private int pointMem;
		
		public pointData(String date, int members) {
			pointDate = date;
			pointMem = members;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_hdata, container, false);

		nexcell = ((MainActivity)getActivity()).getUserNexcell();
		
		List<ParseObject> nexcellObject;
		
		plot1 = (XYPlot) rootView.findViewById(R.id.nexcellFellowshipData);
		plot2 = (XYPlot) rootView.findViewById(R.id.nexcellServiceData);
		plot3 = (XYPlot) rootView.findViewById(R.id.nexcellCollegeData);
		
		if(((MainActivity)getActivity()).getUserAuthlevel() < Constants.COUS_LEVEL)
		{
			nexcellObject = ParseOperation.getNexcellData(nexcell, null, getActivity());
			// initialize our XYPlot reference:			 
			setUpUserPlot(plot1, nexcellObject, "Fellowship", Color.rgb(140, 255, 150), Color.rgb(0, 10, 0), Color.rgb(0, 100, 0));
			setUpUserPlot(plot2, nexcellObject, "Service", Color.rgb(255, 166, 139), Color.rgb(10, 0, 0), Color.rgb(100, 0, 0));
			setUpUserPlot(plot3, nexcellObject, "College", Color.rgb(172, 193, 255), Color.rgb(0, 0, 10), Color.rgb(0, 0, 100));
		}
		else
		{
			nexcellObject = ParseOperation.getNexcellData(null, null, getActivity());

			getNexisData();
			setUpNexisPlot(plot1, nexisData, "Nexis");
			setUpNexisPlot(plot2, hsData, "High School");
			setUpNexisPlot(plot3, uniData, "University");
		}
        Button refreshButton = (Button) rootView.findViewById(R.id.refresh);
        
        refreshButton.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
            	refresh();
				Toast toast = Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT);
				toast.show();
            }
        });
		
		return rootView;
	}
	
	public void refresh()
	{
		List<ParseObject> nexcellObject;
		
		if(((MainActivity)getActivity()).getUserAuthlevel() < Constants.COUS_LEVEL)
		{
			nexcell = ((MainActivity)getActivity()).getUserNexcell();
		
			nexcellObject = ParseOperation.getNexcellData(nexcell, null, getActivity());
		
			updateUserPlot(plot1, nexcellObject, "Fellowship", Color.rgb(140, 255, 150), Color.rgb(0, 10, 0), Color.rgb(0, 100, 0));
			updateUserPlot(plot2, nexcellObject, "Service", Color.rgb(255, 166, 139), Color.rgb(10, 0, 0), Color.rgb(100, 0, 0));
			updateUserPlot(plot3, nexcellObject, "College", Color.rgb(172, 193, 255), Color.rgb(0, 0, 10), Color.rgb(0, 0, 100));
		}
		else
		{
			nexcellObject = ParseOperation.getNexcellData(null, null, getActivity());
			getNexisData();
			updateNexisPlot(plot1, nexisData);
			updateNexisPlot(plot2, hsData);
			updateNexisPlot(plot3, uniData);
		}
	}
	private void updateUserPlot(XYPlot plot, List<ParseObject> nexcellObject, String dataCol, int fillColor, int lineColor, int vertexColor)
	{
		plot.clear();
		
		ArrayList<Integer> members = getNumberOfMembers(nexcellObject, dataCol);
		
		populateDomain(nexcellObject, members);
		
		Integer maxMemNum = 1;
		
		if (!members.isEmpty()) maxMemNum = Collections.max(members);
		setRangeValues(plot, maxMemNum);

		LineAndPointFormatter series1Format = setLinePointFormatter(fillColor, fillColor, vertexColor);
		
        XYSeries series1 = new SimpleXYSeries(domain, members, "# of members");
        
        if (!members.isEmpty()) plot.setDomainStep(XYStepMode.SUBDIVIDE, series1.size());
         
        int tickPerLabels = calculateTick(series1.size());
        
        plot.setTicksPerDomainLabel(tickPerLabels);
        plot.addSeries(series1, series1Format);
        
		plot.redraw();
	}
	
	private void setUpUserPlot (XYPlot plot, List<ParseObject> nexcellObject, String dataCol, int fillColor, int lineColor, int vertexColor)
	{
		setLabels(plot, nexcell + " Statistics " + "(" + dataCol + ")");
		setGridLineAndBackgroundFormat(plot);
		setBorderFormat(plot);
		setLabelValueFormat(plot);
		
		//Remove Legend from the plot
		plot.getLayoutManager().remove(plot.getLegendWidget());
		
		LineAndPointFormatter series1Format = setLinePointFormatter(fillColor, fillColor, vertexColor);
		
		ArrayList<Integer> members = getNumberOfMembers(nexcellObject, dataCol);
		
		populateDomain(nexcellObject, members);
		
		Integer maxMemNum = 1;
		
		if (!members.isEmpty()) maxMemNum = Collections.max(members);
		setRangeValues(plot, maxMemNum);

        XYSeries series1 = new SimpleXYSeries(domain, members, "# of members");
        
        if (!members.isEmpty()) plot.setDomainStep(XYStepMode.SUBDIVIDE, series1.size());
         
        int tickPerLabels = calculateTick(series1.size());
        
        plot.setTicksPerDomainLabel(tickPerLabels);
        plot.addSeries(series1, series1Format);
	}
	
	private void updateNexisPlot(XYPlot plot,ArrayList<List<Integer>> data)
	{
		plot.clear();
		
		LineAndPointFormatter series1Format = setLinePointFormatter(Color.rgb(140, 255, 150), Color.rgb(140, 255, 150), Color.rgb(0, 100, 0));
		LineAndPointFormatter series2Format = setLinePointFormatter(Color.rgb(255, 166, 139), Color.rgb(255, 166, 139), Color.rgb(100, 0, 0));
		LineAndPointFormatter series3Format = setLinePointFormatter(Color.rgb(172, 193, 255), Color.rgb(172, 193, 255), Color.rgb(0, 0, 100));
		
		List<Integer> fellowship = data.get(0);
		List<Integer> service = data.get(1);
		List<Integer> college = data.get(2);
		
		genDomain(fellowship.size());

		Integer maxMemNum = 1;
		
		if (!fellowship.isEmpty()) maxMemNum = Collections.max(fellowship);
		setRangeValues(plot, maxMemNum);
		
        XYSeries series1 = new SimpleXYSeries(domain, fellowship, "# of members");
        XYSeries series2 = new SimpleXYSeries(domain, service, "# of members");
        XYSeries series3 = new SimpleXYSeries(domain, college, "# of members");      
         
        if (!fellowship.isEmpty()) plot.setDomainStep(XYStepMode.SUBDIVIDE, series1.size());
        
        int tickPerLabels = calculateTick(series1.size());
        
        plot.setTicksPerDomainLabel(tickPerLabels);
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);
        plot.addSeries(series3, series3Format);
        
        plot.redraw();
	}
	private void setUpNexisPlot (XYPlot plot,ArrayList<List<Integer>> data, String plotName)
	{
		setLabels(plot,plotName+ " Statistics");
		setGridLineAndBackgroundFormat(plot);
		setBorderFormat(plot);
		setLabelValueFormat(plot);
		
		//Remove Legend from the plot
		plot.getLayoutManager().remove(plot.getLegendWidget());
		
		LineAndPointFormatter series1Format = setLinePointFormatter(Color.rgb(140, 255, 150), Color.rgb(140, 255, 150), Color.rgb(0, 100, 0));
		LineAndPointFormatter series2Format = setLinePointFormatter(Color.rgb(255, 166, 139), Color.rgb(255, 166, 139), Color.rgb(100, 0, 0));
		LineAndPointFormatter series3Format = setLinePointFormatter(Color.rgb(172, 193, 255), Color.rgb(172, 193, 255), Color.rgb(0, 0, 100));
		
		List<Integer> fellowship = data.get(0);
		List<Integer> service = data.get(1);
		List<Integer> college = data.get(2);
		
		genDomain(fellowship.size());

		Integer maxMemNum = 1;
		
		if (!fellowship.isEmpty()) maxMemNum = Collections.max(fellowship);
		setRangeValues(plot, maxMemNum);
		
        XYSeries series1 = new SimpleXYSeries(domain, fellowship, "# of members");
        XYSeries series2 = new SimpleXYSeries(domain, service, "# of members");
        XYSeries series3 = new SimpleXYSeries(domain, college, "# of members");      
         
        if (!fellowship.isEmpty()) plot.setDomainStep(XYStepMode.SUBDIVIDE, series1.size());
        
        int tickPerLabels = calculateTick(series1.size());
        
        plot.setTicksPerDomainLabel(tickPerLabels);
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);
        plot.addSeries(series3, series3Format);
	}
	
	private void populateDomain(List<ParseObject> nexcellObject, List<Integer> members)
	{
		graphDates = getEpochDates(nexcellObject, members.size());
		
		if (members.size() == 1) 
		{
			members.add(members.get(0));
			graphDates.add(graphDates.get(0));
		}
		
		genDomain(members.size());
	}
	
	private int calculateTick(int seriesSize)
	{
		int maxDomainLabels = 10;
        int tickPerLabels = 1;
        
        while(seriesSize / tickPerLabels > maxDomainLabels)
        {
        	tickPerLabels *= 2;
        }
        
        return tickPerLabels;
	}
	
	private void setLabels (XYPlot plot, String title)
	{
		plot.setTitle(title);
		plot.setRangeLabel("Members");
		plot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
		plot.getGraphWidget().setDomainLabelOrientation(-15);	
		plot.getGraphWidget().setRangeLabelWidth(60);

	}
	
	private void setRangeValues (XYPlot plot, int maxMemNum)
	{
		int rangeBound = 0;
		
		int range;
		
		if(maxMemNum<=30)
		{
			rangeBound =  (int) Math.ceil(((double)maxMemNum + 1) / 2) * 2;
			range = 2;		
		}
		else if(maxMemNum<=60)
		{
			rangeBound = (int) (Math.round((maxMemNum + 5)/ 5.0) * 5.0);
			range = 5;
		}
		else 
		{
			rangeBound = (int) (Math.round((maxMemNum + 5)/ 10.0) * 10.0);
			range = 10;
		}
		plot.setRangeBoundaries(0, rangeBound, BoundaryMode.FIXED);
		plot.setRangeStepValue((rangeBound / range) + 1);
	}
	
	private void setBorderFormat (XYPlot plot)
	{
		//plot.setBorderStyle(Plot.BorderStyle.ROUNDED, (float)1.0, (float)1.0);
		plot.getBorderPaint().setStrokeWidth(2);
		plot.getBorderPaint().setAntiAlias(false);
		plot.getBorderPaint().setColor(Color.WHITE);
	}
	
	private void setGridLineAndBackgroundFormat (XYPlot plot)
	{
		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        
		plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getDomainGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
		plot.getGraphWidget().getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        
		plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
		
	}
	
	private void setLabelValueFormat (XYPlot plot)
	{
		plot.setRangeValueFormat(new DecimalFormat("0"));
        
		plot.setDomainValueFormat(new Format() {

            private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMdd");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            	
            	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            	
            	double position =  Math.round(((Number) obj).doubleValue());
            	
                long timestamp = ((Number) graphDates.get((int) position)).longValue();
                
                //Add 1 second to solve the bug of converting object to long value
                //Date date = new Date(timestamp+1000);
                Date date = new Date(timestamp);
                
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
	}
	
	private LineAndPointFormatter setLinePointFormatter(int fillColor, int lineColor, int vertexColor)
	{
		// setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, fillColor, Shader.TileMode.MIRROR));
        
        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format  = new LineAndPointFormatter(lineColor, vertexColor, Color.CYAN , (PointLabelFormatter) null);
        series1Format.setFillPaint(lineFill);
        
        return series1Format;
	
	}
	
	private ArrayList<Number> getEpochDates(List<ParseObject> nexcellObject, int dataPoints)
	{
		ArrayList<Number> dates = new ArrayList<Number>();
		int listSize = nexcellObject.size();
		
		for (int i = 0; i < dataPoints; i++)
    	{
    		Date date = (Date)nexcellObject.get(listSize-i-1).get("Date");
    		dates.add(0, date.getTime());
    	}
		
		return dates;
	}
	
	private ArrayList<Integer> getNumberOfMembers(List<ParseObject> nexcellObject, String dataCol)
	{
		ArrayList<Integer> memberList = new ArrayList<Integer>();
		
		for(ParseObject x: nexcellObject)
    	{
    		int members = x.getInt(dataCol);
    		memberList.add(members);
    	}
		
		return memberList;
	}
	
	private void genDomain(int memberSize)
	{
		domain.clear();
		for(int num = 0; num < memberSize; num++) domain.add(num);
	}
	
	private void getNexisData()
	{	
		hsData.clear();
		uniData.clear();
		nexisData.clear();
		
		for(int i = 0; i < 3; i++)
		{
			hsData.add(new ArrayList<Integer>());
			uniData.add(new ArrayList<Integer>());
			nexisData.add(new ArrayList<Integer>());
		}
		
		List<ParseObject> nexcellObject = ParseOperation.getNexcellData(null, null, getActivity());
		
		int startRow = 0;
		List<Integer> hsNum = Arrays.asList(0, 0, 0);
		List<Integer> uniNum = Arrays.asList(0, 0, 0);
		List<Integer> nexisNum = Arrays.asList(0, 0, 0);
		graphDates = new ArrayList<Number>();
		
		while(startRow < nexcellObject.size() && !Constants.NEXCELL_LIST.contains(nexcellObject.get(startRow).get("Nexcell"))) startRow++;
		
		if (startRow == nexcellObject.size()) return;
		
		DateTime rowDate = new DateTime((Date)nexcellObject.get(startRow).get("Date"), DateTimeZone.UTC);
		
		for(int i = startRow; i < nexcellObject.size(); i++)
    	{	
			Date d = (Date)nexcellObject.get(i).get("Date");
			DateTime currentDate = new DateTime(d, DateTimeZone.UTC);
			String currentNexcell = (String)nexcellObject.get(i).get("Nexcell");
			
			if (!Constants.NEXCELL_LIST.contains(currentNexcell)) continue;
			
			if (!currentDate.equals(rowDate))
			{		
				for(int j = 0; j <= 2; j++)
				{
					hsData.get(j).add(hsNum.get(j));
					uniData.get(j).add(uniNum.get(j));
					nexisData.get(j).add(nexisNum.get(j));
				}
				
				hsNum = Arrays.asList(0,0,0);
				uniNum = Arrays.asList(0,0,0);
				nexisNum = Arrays.asList(0,0,0);
				graphDates.add(rowDate.toDate().getTime());
				rowDate = currentDate;
			}
			for(int j = 0; j <= 2; j++)
			{
				int num = nexcellObject.get(i).getInt(Constants.CATEGORY_LIST.get(j));
				
				if (Constants.NEXCELL_MAP.get(currentNexcell).equals("HighSchool")) 
					hsNum.set(j, hsNum.get(j)+num);
				else 
					uniNum.set(j, uniNum.get(j)+num);
					
				nexisNum.set(j, nexisNum.get(j)+num);
			}
    	}
		
		for(int j = 0; j <= 2; j++)
		{
			hsData.get(j).add(hsNum.get(j));
			uniData.get(j).add(uniNum.get(j));
			nexisData.get(j).add(nexisNum.get(j));
		}
		graphDates.add(rowDate.toDate().getTime());
	}
}
