package com.dandrzas.inertialsensorsviewer.MVVM.View;

import android.graphics.Color;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dandrzas.inertialsensorsviewer.MVVM.ViewModel.GyroscopeViewModel;
import com.dandrzas.inertialsensorsviewer.MVVM.ViewModel.MagnetometerViewModel;
import com.dandrzas.inertialsensorsviewer.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GyroscopeFragment extends Fragment {

    private GyroscopeViewModel gyroscopeViewModel;
    GraphView graph;
    private int graphMaxY = 300;
    private final int GRAPH_LENGTH = 2000;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gyroscopeViewModel =
                ViewModelProviders.of(this).get(GyroscopeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_accelerometer, container, false);

        graph =  root.findViewById(R.id.graph_accelerometer);
        Button button_zoom_in = root.findViewById(R.id.button_zoom_in);
        Button button_zoom_out = root.findViewById(R.id.button_zoom_out);


        //graph config
        Viewport viewport = graph.getViewport();
        viewport.setMinY((-1)*graphMaxY);
        viewport.setMaxY(graphMaxY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(GRAPH_LENGTH);
        viewport.setXAxisBoundsManual(true);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"", "", "","","","","","",""});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getGridLabelRenderer().setNumVerticalLabels(9);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setBackgroundColor(Color.LTGRAY);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        button_zoom_in.setOnClickListener(view-> {
            graphMaxY = graphMaxY/2;
            if(graphMaxY<1) graphMaxY=1;
            viewport.setMinY((-1)*graphMaxY);
            viewport.setMaxY(graphMaxY);
            graph.refreshDrawableState();
            graph.invalidate();
        });

        button_zoom_out.setOnClickListener(view-> {
            graphMaxY = graphMaxY*2;
            if(graphMaxY>300) graphMaxY=300;
            viewport.setMinY((-1)*graphMaxY);
            viewport.setMaxY(graphMaxY);
            graph.refreshDrawableState();
            graph.invalidate();
        });

        gyroscopeViewModel.getGraphSeriesX().observe(this, new Observer<LineGraphSeries<DataPoint>>() {

            @Override
            public void onChanged(LineGraphSeries<DataPoint> dataPointLineGraphSeries) {
                graph.addSeries(dataPointLineGraphSeries);
            }
        });

        gyroscopeViewModel.getGraphSeriesY().observe(this, new Observer<LineGraphSeries<DataPoint>>() {

            @Override
            public void onChanged(LineGraphSeries<DataPoint> dataPointLineGraphSeries) {
                graph.addSeries(dataPointLineGraphSeries);
            }
        });

        gyroscopeViewModel.getGraphSeriesZ().observe(this, new Observer<LineGraphSeries<DataPoint>>() {
            @Override
            public void onChanged(LineGraphSeries<DataPoint> dataPointLineGraphSeries) {
                graph.addSeries(dataPointLineGraphSeries);
            }
        });


        return root;
    }
}