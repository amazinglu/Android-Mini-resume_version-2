package com.parabit.parabeacon.app.demo.components;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parabit.mmrbt.api.BankLocation;
import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.manager.AppLogManager;

import org.slf4j.Logger;

/**
 * Created by williamsnyder on 4/5/18.
 */

public class ContactView extends RelativeLayout {

    private TextView mTextName;
    private TextView mTextPhone;

    private String contactName;
    private String contactPhone;

    public ContactView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.contact_info, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ContactView);

        contactName = a.getString(R.styleable.ContactView_contact_name);
        contactPhone = a.getString(R.styleable.ContactView_contact_phone);
        boolean showSMS = a.getBoolean(R.styleable.ContactView_show_sms, true);

        mTextName = (TextView) findViewById(R.id.txt_contact_name);
        mTextName.setText(contactName);

        mTextPhone = (TextView) findViewById(R.id.txt_contact_phone);
        mTextPhone.setText(contactPhone);

        ImageButton mButtomSMS = (ImageButton) findViewById(R.id.btn_contact_message);
        mButtomSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickMessage();
            }
        });

        ImageButton mButtonCall = (ImageButton) findViewById(R.id.btn_contact_call);
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickCall();
            }
        });

        int imgDrawable = a.getResourceId(R.styleable.ContactView_contact_image, R.drawable.ic_person_black_24dp);
        ImageView mContactImage = (ImageView) findViewById(R.id.img_contact);
        mContactImage.setImageResource(imgDrawable);

        if (!showSMS) {
            mButtomSMS.setVisibility(INVISIBLE);
        }

    }

    public void setContact(BankLocation.EmergencyContact contact) {
        mTextName.setText(contact.getName());
        mTextPhone.setText(contact.getPhone());
    }

    private void handleClickCall() {
        log().debug("Calling " + contactPhone);
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + contactPhone));
        getContext().startActivity(callIntent);
    }

    private void handleClickMessage() {
        log().debug("Messaging " + contactPhone);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + contactPhone));
        sendIntent.putExtra("sms_body", "");
        getContext().startActivity(sendIntent);
    }

    private Logger log() {
        return AppLogManager.getLogger();
    }

}



