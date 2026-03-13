package com.example.cloudticketreservationwk.controller;

import com.example.cloudticketreservationwk.R;
import com.example.cloudticketreservationwk.service.InMemoryStore;
import com.example.cloudticketreservationwk.firebase.AuthService;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventListActivity extends AppCompatActivity implements EventAdapter.Listener {

    private final List<EventAdapter.EventItem> events = new ArrayList<>();
    private final List<EventAdapter.EventItem> allEvents = new ArrayList<>();

    private EventAdapter adapter;

    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;
    private View tvEmpty;

    private int searchMode = 0; // 0=All,1=Date,2=Location,3=Category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        RecyclerView rvEvents = findViewById(R.id.rvEvents);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmptyEvents);
        MaterialButton btnMyReservations = findViewById(R.id.btnMyReservations);

        adapter = new EventAdapter(events, this);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());

        tilSearch = findViewById(R.id.tilSearch);
        if (tilSearch != null) {
            tilSearch.setStartIconOnClickListener(v -> {

                PopupMenu menu = new PopupMenu(EventListActivity.this, v);

                menu.getMenu().add(0,0,0,"All");
                menu.getMenu().add(0,1,1,"Date");
                menu.getMenu().add(0,2,2,"Location");
                menu.getMenu().add(0,3,3,"Category");

                menu.setOnMenuItemClickListener(item -> {

                    searchMode = item.getItemId();

                    if(searchMode==0) tilSearch.setHint("Search events");
                    if(searchMode==1){
                        tilSearch.setHint("Pick a date");
                        openMaterialDatePicker();
                    }
                    if(searchMode==2) tilSearch.setHint("Search location");
                    if(searchMode==3) tilSearch.setHint("Search category");

                    filter(etSearch.getText()==null?"":etSearch.getText().toString());
                    return true;
                });

                menu.show();
            });
        }

        if(etSearch!=null){
            etSearch.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){
                    filter(s==null?"":s.toString());
                }
                public void afterTextChanged(Editable s){}
            });
        }

        btnMyReservations.setOnClickListener(v ->
                startActivity(new Intent(this,MyReservationsActivity.class))
        );

        reloadFromStore();
    }

    @Override
    protected void onResume(){
        super.onResume();
        reloadFromStore();
    }

    private void reloadFromStore(){

        allEvents.clear();
        events.clear();

        for(InMemoryStore.EventItem e : InMemoryStore.EVENTS){

            EventAdapter.EventItem item =
                    new EventAdapter.EventItem(
                            e.id,
                            e.title,
                            e.date,
                            e.location,
                            e.category,
                            e.description
                    );

            allEvents.add(item);
        }

        events.addAll(allEvents);

        if(adapter!=null) adapter.notifyDataSetChanged();

        if(tvEmpty!=null)
            tvEmpty.setVisibility(events.isEmpty()?View.VISIBLE:View.GONE);
    }

    private void filter(String q){

        String query = q.toLowerCase(Locale.US).trim();

        events.clear();

        if(query.isEmpty()){
            events.addAll(allEvents);
        }
        else{

            for(EventAdapter.EventItem e : allEvents){

                boolean match=false;

                if(searchMode==0){
                    match =
                            contains(e.title,query)||
                                    contains(e.date,query)||
                                    contains(e.location,query)||
                                    contains(e.category,query);
                }
                else if(searchMode==1) match = contains(e.date,query);
                else if(searchMode==2) match = contains(e.location,query);
                else if(searchMode==3) match = contains(e.category,query);

                if(match) events.add(e);
            }
        }

        adapter.notifyDataSetChanged();

        if(tvEmpty!=null)
            tvEmpty.setVisibility(events.isEmpty()?View.VISIBLE:View.GONE);
    }

    private boolean contains(String value,String query){
        return value!=null && value.toLowerCase(Locale.US).contains(query);
    }

    private void openMaterialDatePicker(){

        if(getSupportFragmentManager().findFragmentByTag("DATE_PICKER_FILTER")!=null)
            return;

        MaterialDatePicker<Long> picker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd",Locale.US);

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            etSearch.setText(sdf.format(new Date(selection)));
        });

        picker.show(getSupportFragmentManager(),"DATE_PICKER_FILTER");
    }

    @Override
    public void onViewClicked(EventAdapter.EventItem event){

        Intent i = new Intent(this,EventDetailsActivity.class);

        i.putExtra("EVENT_ID",event.id);
        i.putExtra("TITLE",event.title);
        i.putExtra("DATE",event.date);
        i.putExtra("LOCATION",event.location);
        i.putExtra("CATEGORY",event.category);
        i.putExtra("DESCRIPTION",event.description);

        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        menu.add(0,1,0,"My Reservations");
        menu.add(0,2,1,"Logout");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId()==1){

            startActivity(new Intent(this,MyReservationsActivity.class));
            return true;
        }

        if(item.getItemId()==2){

            confirmLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout(){

        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel",(d,w)->d.dismiss())
                .setPositiveButton("Logout",(d,w)->doLogout())
                .show();
    }

    private void doLogout(){

        new AuthService().logout();

        Intent i = new Intent(this,MainActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);

        finish();
    }
}