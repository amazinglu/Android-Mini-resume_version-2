package com.example.amazinglu.mini_resume.summary_edit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.amazinglu.mini_resume.R;
import com.example.amazinglu.mini_resume.model.Summary;
import com.example.amazinglu.mini_resume.util.ImageUtil;
import com.example.amazinglu.mini_resume.util.PermissionUtil;

import java.security.Permission;

public class SummaryEditFragment extends Fragment {

    public static final String KEY_SUMMRY = "summary_edit";
    private static final int REQ_CODE_PICK_IMAGE = 201;

    private EditText name, email, phoneNum;
    private ImageView userImageEdit;
    private Summary summary;

    public static SummaryEditFragment newInstance(Bundle args) {
        SummaryEditFragment fragment = new SummaryEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * key point1: this is needed in order to show the setting of the option menus
         * */
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.summary_edit, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        name = view.findViewById(R.id.summary_edit_name);
        email = view.findViewById(R.id.summary_edit_email);
        phoneNum = view.findViewById(R.id.summary_edit_phone_num);
        userImageEdit = view.findViewById(R.id.summary_edit_user_image);

        // set the text of the previous summary
        // summary could be null
        summary = getArguments().getParcelable(KEY_SUMMRY);
        name.setText(summary.name);
        email.setText(summary.email);
        phoneNum.setText(summary.phoneNum);

        if (summary.userIamgeUri != null) {
            ImageUtil.loadImage(getContext(), summary.userIamgeUri, userImageEdit);
        } else {
            userImageEdit.setImageResource(R.drawable.user_ghost);
        }

        userImageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PermissionUtil.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) &&
                        !PermissionUtil.checkPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionUtil.requestReadExternalStoragePermission(getActivity());
                    PermissionUtil.requestWriteExternalStoragePermission(getActivity());
                } else if (!PermissionUtil.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    PermissionUtil.requestReadExternalStoragePermission(getActivity());
                } else if (!PermissionUtil.checkPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionUtil.requestWriteExternalStoragePermission(getActivity());
                } else {
                    pickImage();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                loadImage(imageUri);
            }
        }
    }

    /**
     * 这部不可以忘记，在request permission成功之后，要自动跳到 pick image 的界面
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.REQ_CODE_READ_EXTERNAL_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /**
         * key point 3: menus 和 layout的inflate 不太一样
         * */
        inflater.inflate(R.menu.menus_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.edit_save:
                saveAndExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAndExit() {
        summary.name = name.getText().toString();
        summary.email = email.getText().toString();
        summary.phoneNum = phoneNum.getText().toString();
        Uri userIamgeUri = (Uri) userImageEdit.getTag();
        Intent intent = new Intent();
        intent.putExtra(KEY_SUMMRY, summary);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void pickImage() {
        /**
         * 打开选择图片的界面
         **/
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE_PICK_IMAGE);
    }

    private void loadImage(Uri imageUri) {
        userImageEdit.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Uri localUri =  ImageUtil.loadImage(getContext(), imageUri, userImageEdit);
        userImageEdit.setTag(localUri);
        summary.userIamgeUri = localUri;
    }
}




