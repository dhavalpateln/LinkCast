package com.dhavalpateln.linkcast.interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.shapes.PathShape;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dhavalpateln.linkcast.database.SharedPrefContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsObject {

    public interface ResultListener {
        void onResult(String result);
    }

    private String value;
    private String title;
    private List<String> options;
    private boolean multiSelect;

    public SettingsObject(String title, String[] options, boolean multiSelect) {
        this.title = title;
        this.options = new ArrayList<>(Arrays.asList(options));
        this.multiSelect = multiSelect;
        this.value = "Any";
    }

    public SettingsObject(String title) {
        this.title = title;
        this.value = "";
    }

    public void showDialog(Context context, ResultListener listener) {
        if(options != null) {
            showMultiSelectDialog(context, listener);
        }
        else {
            showEditTextDialog(context, listener);
        }
    }

    private void showEditTextDialog(Context context, ResultListener listener) {
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(50, 0, 50, 0);
        input.setLayoutParams(layoutParams);
        input.setText(value);
        input.setHint("Optional");
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Set " + title)
                .setView(input)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    value = input.getText().toString();
                    listener.onResult(value);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showMultiSelectDialog(Context context, ResultListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(multiSelect) {
            if(options.get(0).equals("Any"))    options.remove(0);
            boolean[] checkedItems = new boolean[options.size()];
            Set<Integer> selectedItems = new HashSet<>();

            for(String v: value.split(",")) {
                if(v.equals("Any")) break;
                checkedItems[options.indexOf(v)] = true;
                selectedItems.add(options.indexOf(v));
            }

            builder.setMultiChoiceItems(options.toArray(new String[0]), checkedItems, (dialog, indexSelected, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(indexSelected);
                }
                else if (selectedItems.contains(indexSelected)) {
                    selectedItems.remove(Integer.valueOf(indexSelected));
                }
            }).setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                    .setPositiveButton("Select", (dialogInterface, i) -> {
                        String result = "";
                        for(Integer selectedIntex: selectedItems) {
                            result += "," + options.get(selectedIntex);
                        }
                        if(result.equals(""))   result = "Any";
                        else result = result.substring(1);
                        listener.onResult(result);
                    });
        }
        else {
            if(!options.get(0).equals("Any"))   options.add(0, "Any");
            builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                listener.onResult(options.get(which));
            });
        }
        builder.show();
    }

    public void setValue(String value) {this.value = value;}
    public String getValue() {return this.value;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
