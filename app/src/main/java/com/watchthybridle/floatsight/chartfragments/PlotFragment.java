/*
 *
 *     FloatSight
 *     Copyright 2018 Thomas Hirsch
 *     https://github.com/84n4n4/FloatSight
 *
 *     This file is part of FloatSight.
 *     FloatSight is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FloatSight is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with FloatSight.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.watchthybridle.floatsight.chartfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import com.watchthybridle.floatsight.R;
import com.watchthybridle.floatsight.data.FlySightTrackData;
import com.watchthybridle.floatsight.customcharts.GlideOverlayChart;
import com.watchthybridle.floatsight.linedatasetcreation.ChartDataSetHolder;
import com.watchthybridle.floatsight.linedatasetcreation.XAxisValueProviderWrapper;

import static com.watchthybridle.floatsight.linedatasetcreation.ChartDataSetHolder.*;

public class PlotFragment extends ChartFragment {

    private GlideOverlayChart chart;
    private XAxisValueProviderWrapper xAxisValueProviderWrapper;

    public PlotFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        xAxisValueProviderWrapper = new XAxisValueProviderWrapper();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FrameLayout frameLayout = view.findViewById(R.id.root_chart_view);

        chart = new GlideOverlayChart(getContext());

        frameLayout.addView(chart.glideChart);
        frameLayout.addView(chart);
    }

    public void actOnDataChanged(FlySightTrackData flySightTrackData) {
        if(getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }

        if (!isValid(flySightTrackData)) {
            return;
        }

        ChartDataSetHolder chartDataSetHolder =
                new ChartDataSetHolder(getContext(), flySightTrackData, xAxisValueProviderWrapper.xAxisValueProvider);

        chart.setDataSetHolder(chartDataSetHolder);
        chart.invalidate();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.plot_fragment_menu, menu);
        boolean enabled = chart.getDataSetHolder() != null &&
                isValid(chart.getDataSetHolder().getFlySightTrackData());
        menu.findItem(R.id.menu_item_y_axis).setEnabled(enabled);
        menu.findItem(R.id.menu_item_x_axis).setEnabled(enabled);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_y_axis:
                showYAxisDialog();
                return true;
            case R.id.menu_item_x_axis:
                showXAxisDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showYAxisDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.y_axis_dialog_title)
                .setPositiveButton(R.string.ok, null);
        final FrameLayout frameView = new FrameLayout(getContext());
        builder.setView(frameView);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.y_axis_dialog, frameView);
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_altitude)).setChecked(chart.getDataSetHolder().getDataSetPropertiesList().get(ALTITUDE).iLineDataSet.isVisible());
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_glide)).setChecked(chart.glideChart.getData().getDataSets().get(0).isVisible());
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_hor_velocity)).setChecked(chart.getDataSetHolder().getDataSetPropertiesList().get(HOR_VELOCITY).iLineDataSet.isVisible());
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_vert_velocity)).setChecked(chart.getDataSetHolder().getDataSetPropertiesList().get(VERT_VELOCITY).iLineDataSet.isVisible());
        dialog.show();
    }

    public void onYAxisDialogCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.checkbox_altitude:
                chart.getDataSetHolder().getDataSetPropertiesList().get(ALTITUDE).iLineDataSet.setVisible(checked);
                break;
            case R.id.checkbox_glide:
                chart.glideChart.getData().getDataSets().get(0).setVisible(checked);
                break;
            case R.id.checkbox_hor_velocity:
                chart.getDataSetHolder().getDataSetPropertiesList().get(HOR_VELOCITY).iLineDataSet.setVisible(checked);
                break;
            case R.id.checkbox_vert_velocity:
                chart.getDataSetHolder().getDataSetPropertiesList().get(VERT_VELOCITY).iLineDataSet.setVisible(checked);
                break;
        }
        chart.invalidate();
    }

    private void showXAxisDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.x_axis_dialog_title)
                .setPositiveButton(R.string.ok, null);
        final FrameLayout frameView = new FrameLayout(getContext());
        builder.setView(frameView);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.x_axis_dialog, frameView);
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_distance)).setChecked(xAxisValueProviderWrapper.isDistance());
        ((CheckBox) dialoglayout.findViewById(R.id.checkbox_time)).setChecked(xAxisValueProviderWrapper.isTime());
        dialog.show();
    }

    public void onXAxisDialogCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.checkbox_time:
                ((CheckBox) view.getRootView().findViewById(R.id.checkbox_distance)).setChecked(!checked);
                if(checked) {
                    xAxisValueProviderWrapper.setTime();
                } else {
                    xAxisValueProviderWrapper.setDistance();
                }
                break;
            case R.id.checkbox_distance:
                ((CheckBox) view.getRootView().findViewById(R.id.checkbox_time)).setChecked(!checked);
                if(checked) {
                    xAxisValueProviderWrapper.setDistance();
                } else {
                    xAxisValueProviderWrapper.setTime();
                }
                break;
        }
        chart.resetUserChanges();
        actOnDataChanged(chart.getDataSetHolder().getFlySightTrackData());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        chart.saveState(outState);
        chart.glideChart.saveState(outState);
        outState.putString(XAxisValueProviderWrapper.BUNDLE_KEY, xAxisValueProviderWrapper.getStringValue());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chart.restoreState(savedInstanceState);
        chart.glideChart.restoreState(savedInstanceState);
        if(savedInstanceState != null) {
            xAxisValueProviderWrapper = new XAxisValueProviderWrapper(
                    savedInstanceState.getString(XAxisValueProviderWrapper.BUNDLE_KEY));
        }
    }

    @VisibleForTesting
    public GlideOverlayChart getChart() {
        return chart;
    }
}