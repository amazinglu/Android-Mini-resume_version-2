package com.parabit.parabeacon.app.demo.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parabit.mmrbt.api.BankLocation;
import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.components.ContactView;
import com.parabit.parabeacon.app.demo.state.AppState;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by williamsnyder on 8/28/17.
 */

public class EmergencyContactFragment extends BaseDemoFragment {

    private ContactView mFireContact;
    private ContactView mPoliceContact;

    public static EmergencyContactFragment newInstance(Bundle bundle) {
        EmergencyContactFragment fragmentEmergencyContact = new EmergencyContactFragment();
        fragmentEmergencyContact.setArguments(bundle);
        return fragmentEmergencyContact;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_emergency, container, false);

        setupView(fragmentView);

        return fragmentView;

    }

    private void setupView(View view) {
        mFireContact = (ContactView) view.findViewById(R.id.contact_view_fire);
        mPoliceContact = (ContactView) view.findViewById(R.id.contact_view_police);

        refreshContacts();

        getCurrentState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                refreshContacts();
            }
        });
    }

    private void refreshContacts() {
        AppState currentState = getCurrentState();
        if (currentState == null) {
            return;
        }
        List<BankLocation.EmergencyContact> contacts = currentState.getContacts();
        if (contacts != null) {
            log().debug("Refreshing contacts.");
            for (BankLocation.EmergencyContact contact: contacts) {
                if (contact.getContactType() == BankLocation.EmergencyContact.ContactType.FIRE) {
                    mFireContact.setContact(contact);
                }
                if (contact.getContactType() == BankLocation.EmergencyContact.ContactType.POLICE) {
                    mPoliceContact.setContact(contact);
                }
            }
        }
    }

}
