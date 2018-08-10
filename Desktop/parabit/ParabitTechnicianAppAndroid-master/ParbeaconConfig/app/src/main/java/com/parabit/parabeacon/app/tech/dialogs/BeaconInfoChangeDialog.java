// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.parabit.parabeacon.app.tech.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.parabit.parabeacon.app.tech.R;

/**
 * Dialog which pops up when the user wants to change either the namespace or the instance of the
 * Uid slot. It verifies the input and doesn't allow invalid information to be sent to the beacon.
 */
public class BeaconInfoChangeDialog {
    public static void show(String oldName, String oldDesc,
                            final Context ctx, final BeaconInfoChangeListener beaconChangeListener) {
        final AlertDialog editInfoDialog = new AlertDialog.Builder(ctx).show();
        editInfoDialog.setContentView(R.layout.dialog_change_info);
        editInfoDialog.setCanceledOnTouchOutside(false);

        // This is needed because there are some flags being set which prevent the keyboard from
        // popping up when an EditText is clicked
        editInfoDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText editNameView
                = (EditText) editInfoDialog.findViewById(R.id.txtEditName);
        final TextView namespaceTracker
                = (TextView) editInfoDialog.findViewById(R.id.name_tracker);
        editNameView.setText(oldName);
        editNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                namespaceTracker.setText("(" + editNameView.getText().length() + "/30)");
            }
        });

        final EditText editDescView
                = (EditText) editInfoDialog.findViewById(R.id.txtEditDescription);
        editDescView.setText(oldDesc);
        final TextView instanceTracker
                = (TextView) editInfoDialog.findViewById(R.id.desc_tracker);

        editDescView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                instanceTracker.setText("(" + editDescView.getText().length() + "/30)");
            }
        });

        editInfoDialog.findViewById(R.id.confirm_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String newName = editNameView.getText().toString();
                        final String newDesc = editDescView.getText().toString();

                        if (newName.length() > 30) {
                            editNameView.setError("Must be less than 30 characters");
                        }

                        if (newDesc.length() > 30) {
                            editDescView.setError("Must be less than 30 characters");
                        }

                        if (newName.length() <= 30 && newDesc.length() <= 30) {
                            beaconChangeListener.setNewName(newName);
                            beaconChangeListener.setNewDesc(newDesc);
                            editInfoDialog.dismiss();
                        }
                    }
                });

        editInfoDialog.findViewById(R.id.cancel_change_uid).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editInfoDialog.dismiss();
                    }
                });
    }

    /**
     * Listener interface to be called when password has been types in.
     */
    public interface BeaconInfoChangeListener {
        void setNewName(String name);
        void setNewDesc(String desc);
    }
}
