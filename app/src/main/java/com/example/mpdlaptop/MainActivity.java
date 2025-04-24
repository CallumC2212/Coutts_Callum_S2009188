package com.example.mpdlaptop;

//import android.support.v7.app.AppCompatActivity;
//import android.support.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.activity.EdgeToEdge;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mpdlaptop.databinding.ActivityMapsBinding;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.PrivateKey;
import java.util.Locale;

//import gcu.mpd.bgsdatastarter.R;


public class MainActivity extends AppCompatActivity  implements OnClickListener, OnMapReadyCallback
{//AppCompatActivity implements OnClickListener
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private TextView rawDataDisplay;

    private Button dateButton;
    private Button nearestButton;
    private Button magButton;
    private Button depthButton;

    private int nearestDirection; //0-north 1-east 2-south 4-west
    private Button eqListButton;
    private ListView listView;
    private ArrayAdapter adapter;
    private ArrayAdapter directionAdapter;
    private ArrayAdapter dateAdapter;
    private String result="";
    private String url1="";
    private String urlSource="http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private ArrayList<earthQuakeData> aList = new ArrayList<earthQuakeData>();
    int activeAdapter = 0;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    private ArrayList<earthQuakeData> offshoreList = new ArrayList<earthQuakeData>();
    private ViewSwitcher aVS;
    private boolean depthLarge = false;
    private boolean magLarge = false;
    private double NorthDistance = 999999999;
    private double EastDistance = 999999999;
    private double SouthDistance = 999999999;
    private double WestDistance = 999999999;
    earthQuakeData NorthQuake = new earthQuakeData();
    earthQuakeData EastQuake = new earthQuakeData();
    earthQuakeData SouthQuake = new earthQuakeData();
    earthQuakeData WestQuake = new earthQuakeData();
    ArrayList<earthQuakeData> filteredDateList;
    public class earthQuakeData {
        private String Title;
        private String Description;
        private String Magnitude;
        private String Location;
        private Date Datetime;
        private String geoLat;
        private String geoLong;
        private String Depth;
        private Double DistanceFromG;
        private Double degreesFromG;
        private boolean isExpanded = false;
        // Constructors, getters and setters
        public void setTitle(String Title){ this.Title = Title;}
        void setDescription(String Description){ this.Description = Description;}
        public void setMagnitude(String Magnitude){ this.Magnitude = Magnitude;}
        public void setLocation(String Loction){ this.Location = Loction;}
        public void setDatetime(Date Datetime){ this.Datetime = Datetime;}
        public void setGeoLat(String geoLat){ this.geoLat = geoLat;}
        public void setGeoLong(String geoLong){ this.geoLong = geoLong;}
        public void setDepth(String Depth){ this.Depth = Depth;}
        public void setDistanceFromG(Double DistanceFromG){ this.DistanceFromG = DistanceFromG;}
        public void setdegreesFromG(Double degreesFromG){ this.degreesFromG = degreesFromG;}
        public void setExpanded(boolean expanded) {isExpanded = expanded;}

        public String getTitle(){ return Title;}
        public String getDescription(){return Description;}
        public String getMagnitude(){ return Magnitude;}
        public String getLocation(){ return Location;}
        public Date getDatetime(){ return Datetime;}
        public String getGeoLat(){ return geoLat;}
        public String getGeoLong(){ return geoLong;}
        public String getDepth(){ return Depth;}
        public Double getDistanceFromG(){ return DistanceFromG;}
        public Double getdegreesFromG(){ return degreesFromG;}
        public boolean isExpanded() { return isExpanded;}

        public String toString() {
            return Title + '\n';
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startButton = (Button)findViewById(R.id.startButton);
       // startButton.setOnClickListener(this);
        aVS = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        startProgress();
        dateButton = (Button)findViewById(R.id.button);
        dateButton.setOnClickListener(this);
        magButton = (Button)findViewById(R.id.button3);
        magButton.setOnClickListener(this);
        depthButton = (Button)findViewById(R.id.button4);
        depthButton.setOnClickListener(this);
        nearestButton = (Button)findViewById(R.id.button2);
        nearestButton.setOnClickListener(this);
        //exploreButton = (Button)findViewById(R.id.exploreButtonDN);
        //exploreButton.setOnClickListener(this);
        //eqListButton = (Button)findViewById(R.id.eqListButton);
        //eqListButton.setOnClickListener(this);
        dateButton.setOnClickListener(v -> showDateRangeDialog());
        //adapter = new ArrayAdapter<earthQuakeData>(this, R.layout.activity_listview, aList);
        adapter = new EarthquakeAdapter(this, aList);
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                result = "";
                startProgress(); // clear and re-fetch fresh data
                refreshHandler.postDelayed(this,  2*60*60 * 1000); // every 2 hrs
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 2*60*60 * 1000); // every 2 hrs
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    public void onClick(View aview)
    {


        if( aview == dateButton)
        {
            //int button = 1;
            showDateRangeDialog();
        }
        else if( aview == nearestButton)
        {
            int button = 2;
            showDepthDialog(button);
        }
        else if( aview == magButton)
        {
             int button = 3;
            showDepthDialog(button);

        }
        else if( aview == depthButton)
        {
            int button = 4;
            showDepthDialog(button);
        }
    }
    private void showDepthDialog(int button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (button == 2) {
            builder.setTitle("Select the direction which you would like to see the nearest earthquake");

            String[] options = {"North", "East", "South", "West"};
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        // Deepest selected
                        Toast.makeText(getApplicationContext(), "North selected", Toast.LENGTH_SHORT).show();
                        nearestDirection = 0;
                        neartestDirectionFunction();
                    } else if (which == 1) {

                        Toast.makeText(getApplicationContext(), "East selected", Toast.LENGTH_SHORT).show();
                        nearestDirection = 1;
                        neartestDirectionFunction();
                    } else if (which == 2) {

                        Toast.makeText(getApplicationContext(), "South selected", Toast.LENGTH_SHORT).show();
                        nearestDirection = 2;
                        neartestDirectionFunction();
                    } else if (which == 3) {

                        Toast.makeText(getApplicationContext(), "West selected", Toast.LENGTH_SHORT).show();
                        nearestDirection = 3;
                        neartestDirectionFunction();
                    }


                }
            });
        }
        if (button == 3) {
            builder.setTitle("Select magnitude");

            String[] options = {"Largest", "Smallest"};
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        Toast.makeText(getApplicationContext(), "Largest selected", Toast.LENGTH_SHORT).show();
                        magLarge = true;
                        sortByMag();
                    } else if (which == 1) {
                        Toast.makeText(getApplicationContext(), "Smallest selected", Toast.LENGTH_SHORT).show();
                        magLarge = false;
                        sortByMag();
                    }
                }
            });
        }

        if (button == 4) {
            builder.setTitle("Select Depth");

            String[] options = {"Deepest", "Shallowest"};
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        Toast.makeText(getApplicationContext(), "Deepest selected", Toast.LENGTH_SHORT).show();
                        depthLarge = false;
                        sortByDepth();
                    } else if (which == 1) {
                        Toast.makeText(getApplicationContext(), "Shallowest selected", Toast.LENGTH_SHORT).show();
                        depthLarge = true;
                        sortByDepth();
                    }
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void showDateRangeDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog startDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            Date startDate = calendar.getTime();

            DatePickerDialog endDialog = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth1) -> {
                calendar.set(year1, month1, dayOfMonth1, 23, 59, 59);
                Date endDate = calendar.getTime();
                if (startDate.after(endDate)) {
                    Toast.makeText(this, "Start date cannot be after end date.", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<earthQuakeData> filteredDateList = filterDataByDateRange(startDate, endDate);

                EarthquakeAdapter filteredAdapter = new EarthquakeAdapter(this, filteredDateList);
                listView = (ListView) findViewById(R.id.earthquakeListView);
                listView.setAdapter(filteredAdapter);
                listView.setOnItemClickListener((parent, itemView, position, id) -> {
                    earthQuakeData quake = filteredDateList.get(position);
                    quake.setExpanded(!quake.isExpanded());
                    filteredAdapter.notifyDataSetChanged();

                    if (mMap != null) {
                        try {
                            double lat = Double.parseDouble(quake.getGeoLat());
                            double lng = Double.parseDouble(quake.getGeoLong());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14.0f));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            endDialog.setTitle("Select End Date");
            endDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        startDialog.setTitle("Select Start Date");
        startDialog.show();
    }


    public void startProgress()
    {

        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    }

    public void neartestDirectionFunction()
    {
       ArrayList<earthQuakeData> directionList = new ArrayList<earthQuakeData>();
        switch(nearestDirection){
        case 0:
            directionList.add(NorthQuake);
            break;
            case 1:
                directionList.add(EastQuake);
                break;
            case 2:
                directionList.add(SouthQuake);
                break;
            case 3:
                directionList.add(WestQuake);
                break;

        }

        EarthquakeAdapter directionAdapter = new EarthquakeAdapter(this, directionList);
        directionAdapter.setShowDistance(true);
        listView = (ListView) findViewById(R.id.earthquakeListView);
        listView.setAdapter(directionAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            earthQuakeData quake = directionList.get(position);
            quake.setExpanded(!quake.isExpanded());
            directionAdapter.notifyDataSetChanged();

            if (mMap != null) {
                try {
                    double lat = Double.parseDouble(quake.getGeoLat());
                    double lng = Double.parseDouble(quake.getGeoLong());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14.0f));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void sortByMag()
    {
        if (magLarge == false)
        {

            Collections.sort(aList, new Comparator<earthQuakeData>() {
                //returns aList sorted by magnitude decending
                public int compare(earthQuakeData o1, earthQuakeData o2) {
                    return o1.getMagnitude().compareToIgnoreCase(o2.getMagnitude());
                }
            });
        }
        if(magLarge == true)
        {
            Collections.sort(aList, new Comparator<earthQuakeData>() {
                //returns aList sorted by Deepest at top
                public int compare(earthQuakeData o1, earthQuakeData o2) {
                    return o2.getMagnitude().compareToIgnoreCase(o1.getMagnitude());
                }
            });
        }
        Log.d("MyTag", aList.toString());
        listView = (ListView) findViewById(R.id.earthquakeListView);
        listView.setAdapter(adapter);

    }
    public void sortByDepth()
    {
        if (depthLarge)
        {
            Collections.sort(aList, new Comparator<earthQuakeData>() {
                @Override
                public int compare(earthQuakeData o1, earthQuakeData o2) {
                    try {
                        double d1 = Double.parseDouble(o1.getDepth().replace(" km", "").trim());
                        double d2 = Double.parseDouble(o2.getDepth().replace(" km", "").trim());
                        return Double.compare(d1, d2); // shallowest first
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            });
            Log.d("MyTag", "Sorted by shallowest depth");
        }
        else if(depthLarge == false)
        {
            Collections.sort(aList, new Comparator<earthQuakeData>() {
                @Override
                public int compare(earthQuakeData o1, earthQuakeData o2) {
                    try {
                        double d1 = Double.parseDouble(o1.getDepth().replace(" km", "").trim());
                        double d2 = Double.parseDouble(o2.getDepth().replace(" km", "").trim());
                        return Double.compare(d2, d1); // deepest first
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            });
            Log.d("MyTag", "Sorted by deepest depth");
        }

        listView = (ListView) findViewById(R.id.earthquakeListView);
        listView.setAdapter(adapter);
    }
    public ArrayList<earthQuakeData> filterDataByDateRange(Date startDate, Date endDate)
    {

        ArrayList<earthQuakeData> filteredDateList = new ArrayList<earthQuakeData>();
        for (earthQuakeData data : aList) {
            if (data.getDatetime().after(startDate) && data.getDatetime().before(endDate)) {
                filteredDateList.add(data);
            }
        }
        return filteredDateList;
    }

    private class Task implements Runnable
    {

        private String url;

        public Task(String aurl)
        {
            url = aurl;
        }
        @Override
        public void run()
        {
            result = "";
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            Log.e("MyTag","in run");

            try
            {
                Log.d("MyTag","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                Boolean firstLine = true;
                while ((inputLine = in.readLine()) != null)
                {
                    if(firstLine) firstLine = false;
                    else result = result + inputLine;
                    //  Log.d("MyTag",inputLine);


                }
                in.close();
            }
            catch (IOException ae)
            {
                Log.e("MyTag", "ioexception");
            }

            if(result != null)
            {
                Log.e("MyTag","result is not null");
                parseData(result);

            }
            else
            {
                Log.e("MyTag","result is null");
            }

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {

                    listView = (ListView) findViewById(R.id.earthquakeListView);
                    adapter = new EarthquakeAdapter(MainActivity.this, aList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        earthQuakeData quake = aList.get(position);
                        quake.setExpanded(!quake.isExpanded());
                        adapter.notifyDataSetChanged();

                    if (mMap != null) {
                        try {
                            double lat = Double.parseDouble(quake.geoLat);
                            double lng = Double.parseDouble(quake.geoLong);
                            LatLng quakeLocation = new LatLng(lat, lng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(quakeLocation, 14.0f));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    });
                    initMap();

                }
            });
        }

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        for (int i = 0; i < aList.size(); i++)
        {
            LatLng temp = new LatLng(Double.parseDouble((aList.get(i).geoLat)), Double.parseDouble(aList.get(i).geoLong));
            mMap.addMarker(new MarkerOptions().position(temp).title(aList.get(i).getDescription()));
            if (i == 0)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(temp));
                mMap.animateCamera( CameraUpdateFactory.zoomTo( 14.0f ) );
            }
        }

    }
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapInit", "Map fragment not found!");
        }
    }
    //indexing all earthquakes that occur offsore
    private boolean isOffshoreLocation(String location) {
        String loc = location.toUpperCase(Locale.ENGLISH);
        return loc.contains(" SEA ") || loc.contains(" OCEAN ") ||
                loc.contains(" GULF ") || loc.contains(" STRAIT ") ||
                loc.contains(" BAY ") || loc.contains(" CHANNEL ");
    }
    private void parseData(String dataToParse)
    {

        try
        {
            aList.clear();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader( dataToParse ) );
            int eventType = xpp.getEventType();
            String temp;
            earthQuakeData stringData = new earthQuakeData();
            boolean itemReady = false;
            float GX = -4.251f;
            float GY = 55.861f;
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                //Log.d("MyTag","Test while loop " );

                if(eventType == XmlPullParser.START_TAG) // Found a start tag
                {


                    if (xpp.getName().equalsIgnoreCase("item"))
                    {
                        Log.d("MyTag", "New item found ");
                        itemReady = true;
                        stringData = new earthQuakeData();
                    }
                    else if (xpp.getName().equalsIgnoreCase("title") && itemReady)
                    {
                        temp = xpp.nextText();
                        stringData.setTitle(temp);
                        Log.d("MyTag","Title is " + temp);
                    }
                    else if (xpp.getName().equalsIgnoreCase("description")&& itemReady)
                    {
                        temp = xpp.nextText();
                        //set the description
                        stringData.setDescription(temp);

                        String[] myArray = temp.split( "; ");
                        String[] tempDataArray;

                        //location data
                        tempDataArray = myArray[1].split(": ");
                        stringData.setLocation(tempDataArray[1]);
                        if (isOffshoreLocation(tempDataArray[1]))
                        {
                            offshoreList.add(stringData);
                            Log.e("MyTag","offshore earthquakes" + tempDataArray[1]);
                        }
                        //depth data
                        tempDataArray = myArray[3].split(": ");
                        stringData.setDepth(tempDataArray[1]);
                        Log.d("MyTag","depth is " + tempDataArray[1]);

                        //magnitude data
                        tempDataArray = myArray[4].split(": ");
                        stringData.setMagnitude(tempDataArray[1]);
                        Log.d("MyTag","Mag is " + tempDataArray[1]);

                    }
                    else if (xpp.getName().equalsIgnoreCase("pubDate")&& itemReady)
                    {
                        temp = xpp.nextText();
                        String[] myArray = temp.split( ", ");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
                        Date tempDate = sdf.parse(myArray[1]);
                        stringData.setDatetime(tempDate);
                        //System.out.println(tempDate); // 2025-04-16T02:50:21
                        Log.d("MyTag","Date/Time is " + myArray[1]);


                    }
                    else if (xpp.getName().equalsIgnoreCase("lat")&& itemReady)
                    {
                        temp = xpp.nextText();
                        stringData.setGeoLat(temp);
                        Log.d("MyTag","Lat is " + temp);
                    }
                    else if (xpp.getName().equalsIgnoreCase("long")&& itemReady)
                    {
                        temp = xpp.nextText();
                        stringData.setGeoLong(temp);
                        Log.d("MyTag","Long is " + temp);
                    }


                }
                else if(eventType == XmlPullParser.END_TAG) // Found an end tag
                {
                    if (xpp.getName().equalsIgnoreCase("item"))
                    {
                        float x = Float.parseFloat(stringData.getGeoLong());
                        float y = Float.parseFloat(stringData.getGeoLat());
                        float ABX = x - GX;
                        float ABY = y - GY;
                        double tempMath= Math.atan2(ABY, ABX)*180/Math.PI;
                        stringData.setdegreesFromG(tempMath);
                        Log.d("degrees", "" + tempMath);
                        double distance = Math.sqrt((ABX * ABX) + (ABY *ABY));
                        stringData.setDistanceFromG(distance);
                        Log.d("distance", "" + distance);
                        aList.add(stringData);
                        System.out.println(aList);
                        //North
                        if(stringData.getdegreesFromG() > 45 && stringData.getdegreesFromG() < 135)
                        {
                            if (distance < NorthDistance)
                            {
                                NorthDistance = distance;
                                NorthQuake = stringData;
                            }
                        }
                        //East
                        else if(stringData.getdegreesFromG() <= 45 && stringData.getdegreesFromG() >= -45)
                        {
                            if (distance < EastDistance)
                            {
                                EastDistance = distance;
                                EastQuake = stringData;
                            }
                        }
                        //South
                        else if(stringData.getdegreesFromG() < -45 && stringData.getdegreesFromG() > -135)
                        {
                            if (distance < SouthDistance)
                            {
                                SouthDistance = distance;
                                SouthQuake = stringData;
                            }
                        }
                        //West
                        else if(stringData.getdegreesFromG() <= -135 || stringData.getdegreesFromG() >= 135)
                        {
                            if (distance < WestDistance)
                            {
                                WestDistance = distance;
                                WestQuake = stringData;
                            }
                        }
                        itemReady = false;
                        //Log.d("nearest", "North:" + NorthQuake +" East:" + EastQuake + " South:" + SouthQuake+ " West:" + WestQuake);
                        Log.d("MyTag","Earthquake data parsing completed!");

                        Log.d("tag ", String.valueOf(tempMath));
                    }
                }
                eventType = xpp.next();
            } // End of while
        }
        catch (XmlPullParserException ae1)
        {
            Log.e("MyTag","Parsing error" + ae1.toString());
        }
        catch (IOException ae1)
        {
            Log.e("MyTag","IO error during parsing");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Log.d("FINAL_LIST", "Final size of aList: " + aList.size());
        Log.d("MyTag","End of document reached");
    }

}
