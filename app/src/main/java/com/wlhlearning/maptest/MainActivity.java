package com.wlhlearning.maptest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private TextView position_text;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isfirstLocate = true;
    private GeoCoder mSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        position_text = (TextView)findViewById(R.id.position);
        mapView = (MapView)findViewById(R.id.baidumap);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionlist = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionlist.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionlist.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionlist.isEmpty()){
            String []permissions = permissionlist.toArray(new String[permissionlist.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        } else {
            requestLocation();
        }
    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mSearch=GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(new MyAddressListener());
    }
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        mSearch.destroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    private void navigateTo(BDLocation location){
        if (isfirstLocate){
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isfirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
//            currentPosition.append("纬度:").append(location.getLatitude()).append("\n");
//            currentPosition.append("经度:").append(location.getLongitude()).append("\n");
//            currentPosition.append("国家:").append(location.getCountry()).append("\n");
//            currentPosition.append("省:").append(location.getProvince()).append("\n");
//            currentPosition.append("市:").append(location.getCity()).append("\n");
//            currentPosition.append("区:").append(location.getDistrict()).append("\n");
//            currentPosition.append("街道:").append(location.getStreet()).append("\n");
//            currentPosition.append("定位方式:");
//            if (location.getLocType()==BDLocation.TypeGpsLocation){
//                currentPosition.append("GPS");
//            }else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
//                currentPosition.append("网络");
//            }
            //根据经纬度进行反地理编码
            LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
            mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(myLocation));
            if (location.getLocType()==BDLocation.TypeGpsLocation || location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }
    }
    public class MyAddressListener implements OnGetGeoCoderResultListener{
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
            }
        }
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this,"反地理编码失败",Toast.LENGTH_SHORT).show();
            }
            if (result!=null){
                StringBuilder currentPosition = new StringBuilder();
                currentPosition.append(result.getAddress());
                position_text.setText(currentPosition);
            }


        }
    }

}
