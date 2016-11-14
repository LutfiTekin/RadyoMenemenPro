package com.incitorrent.radyo.menemen.pro.fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class topics_create extends Fragment implements View.OnClickListener{
    Context context;
    Menemen m;
    RequestQueue queue;
    private static final String RESPONSE_ERROR = "1";
    private static final String RESPONSE_DUPLICATE = "2";
    private static final int RESULT_LOAD_IMAGE = 2064;
    private static final int PERMISSION_REQUEST_ID = 2065;
    TextInputLayout til_title,til_descr;
    EditText et_title,et_descr;
    CheckBox checkBox;
    ImageView imageView;
    TextView textView;
    ProgressBar progressBar;
    public String imageurl = "default";
    public String imagepath = null;
    public topics_create() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        if(getActivity() != null) getActivity().setTitle(getString(R.string.topic_create_new));
        m = new Menemen(context);
        queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_CREATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response){
                            case RESPONSE_ERROR:
                                Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                                break;
                            case RESPONSE_DUPLICATE:
                                Toast.makeText(context, "DUPLICATE", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                FirebaseMessaging.getInstance().subscribeToTopic(response);
                                break;
                        }
                        Log.d("topics",response);
                    }
                },null){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put(RadyoMenemenPro.USERNAME, m.getUsername());
                dataToSend.put(RadyoMenemenPro.MOBIL_KEY, m.getMobilKey());
                return dataToSend;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return RadyoMenemenPro.MENEMEN_TIMEOUT/6;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 0;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {
                        throw  new VolleyError("NO RETRY");
                    }
                };
            }
        };
        queue.add(stringRequest);
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_topics_create, container, false);
        et_title = (EditText) rootview.findViewById(R.id.et_title);
        et_descr = (EditText) rootview.findViewById(R.id.et_descr);
        til_title = (TextInputLayout) rootview.findViewById(R.id.til_title);
        til_descr = (TextInputLayout) rootview.findViewById(R.id.til_descr);
        checkBox = (CheckBox) rootview.findViewById(R.id.checkBox);
        imageView = (ImageView) rootview.findViewById(R.id.imageView);
        textView = (TextView) rootview.findViewById(R.id.text);
        progressBar = (ProgressBar) rootview.findViewById(R.id.progressBar);
        imageView.setOnClickListener(this);
        m.runEnterAnimation(til_title,400);
        m.runEnterAnimation(til_descr,600);
        m.runEnterAnimation(checkBox,800);
        m.runEnterAnimation(imageView,1000);
        m.runEnterAnimation(textView,1000);
        setRetainInstance(true);
        return rootview;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView:
                selectTopicImage();
                break;
        }
    }

    private void selectTopicImage() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Dosya okuma izni yok izin iste
            AskReadPerm();
            return;
        }
        Intent resimsec = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*");
        startActivityForResult(resimsec, RESULT_LOAD_IMAGE);
    }
    private void AskReadPerm() {
        if(getActivity()!=null)new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.permissions))
                .setMessage(getString(R.string.askperm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_ID);
                    }
                })
                .setIcon(R.mipmap.album_placeholder)
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_granted, Toast.LENGTH_SHORT)
                        .show();// Permission Granted
                Intent resimsec = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        .setType("image/*");
                startActivityForResult(resimsec, RESULT_LOAD_IMAGE);
            }
            else
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_denied, Toast.LENGTH_SHORT)
                        .show(); // Permission Denied

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && data!=null){
            try {
                Uri selectedimage = data.getData();
                imagepath = selectedimage.toString();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedimage);
                bitmap = Menemen.resizeBitmap(bitmap,300);
                imageView.setImageBitmap(bitmap);
                progressBar.setVisibility(View.VISIBLE);
                uploadTopicImage(bitmap);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    private void uploadTopicImage(final Bitmap bitmap) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.CAPS_API_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject J = new JSONObject(response);
                            JSONObject Jo = J.getJSONObject("image");
                            if(!J.getString("status_code").equals("200")) throw new Exception(context.getString(R.string.image_not_uploaded));
                            bitmap.recycle();
                            imageurl = Jo.getString("url");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley",error.toString());
                        progressBar.setVisibility(View.GONE);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams(){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                Map<String,String> dataToSend = new HashMap<>();
                dataToSend.put("source", encodedImage);
                dataToSend.put("key", m.oku(RadyoMenemenPro.CAPS_API_KEY));
                return dataToSend;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return m.menemenRetryPolicy();
            }
        };
        queue.add(postRequest);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("imageurl",imageurl);
        outState.putString("imagepath",imagepath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            imageurl = savedInstanceState.getString("imageurl");
            imagepath = savedInstanceState.getString("imagepath");
            if(imagepath!=null){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), Uri.parse(imagepath));
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityCreated(savedInstanceState);
    }
}
