package com.example.restapiucenm1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.restapiucenm1.config.Personas;
import com.example.restapiucenm1.config.RestApiMethods;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivityCreate extends AppCompatActivity {

    static final int REQUEST_IMAGE = 101;
    static final int ACCESS_CAMERA =  201;
    ImageView imageView;
    Button btnfoto, btncreate;
    String currentPhotoPath;
    EditText nombres, apellidos, fechanac, telefono, foto, direccion;
    private RequestQueue requestQueue;
    Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);


        imageView =(ImageView) findViewById(R.id.imageView);
        btnfoto =(Button) findViewById(R.id.btntakefoto);
        btncreate =(Button) findViewById(R.id.btncreate);

        nombres =(EditText) findViewById(R.id.nombres);
        apellidos =(EditText) findViewById(R.id.apellidos);
        direccion =(EditText) findViewById(R.id.direccion);
        fechanac =(EditText) findViewById(R.id.fecha);
        telefono =(EditText) findViewById(R.id.telefono);


        // Evento clic en el EditText
        fechanac.setOnClickListener(view -> {
            int año = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ActivityCreate.this,
                    (view1, year, month, dayOfMonth) -> {
                        // Formato de fecha: dd/MM/yyyy
                        String fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        fechanac.setText(fechaSeleccionada);
                    },
                    año, mes, dia
            );
            datePickerDialog.show();
        });

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermisosCamara();
            }
        });

        btncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendData();
            }
        });
    }

    private void SendData()
    {
        requestQueue = Volley.newRequestQueue(this);
        Personas personas = new Personas();

        personas.setNombres(nombres.getText().toString());
        personas.setApellidos(apellidos.getText().toString());
        personas.setDireccion(direccion.getText().toString());
        personas.setFechanac(fechanac.getText().toString());
        personas.setTelefono(telefono.getText().toString());
        personas.setFoto(ConvertImageBase64(currentPhotoPath));

        JSONObject jsonObject = new JSONObject();

        try
        {
            jsonObject.put("nombres",personas.getNombres());
            jsonObject.put("apellidos",personas.getApellidos());
            jsonObject.put("direccion",personas.getDireccion());
            jsonObject.put("telefono",personas.getTelefono());
            jsonObject.put("fechanac",personas.getFechanac());
            jsonObject.put("foto",personas.getFoto());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RestApiMethods.EndpointCreatePerson,
                    jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        String mensaje = response.getString("message");
                        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getApplicationContext(), error.getMessage().toString(),
                            Toast.LENGTH_LONG).show();

                }
            });


            requestQueue.add(request);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private String ConvertImageBase64(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(imageArray, Base64.DEFAULT);
    }

    private void PermisosCamara()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},ACCESS_CAMERA);
        }
        else
        {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

            if(requestCode == ACCESS_CAMERA)
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    dispatchTakePictureIntent();
                }
                else
                {

                    Toast.makeText(getApplicationContext(),"Se necesita permiso de la camara",
                            Toast.LENGTH_LONG).show();
                }
            }

    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.restapiucenm1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
    return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE)
        {
            try {
                File Foto = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(Foto));
            }
            catch (Exception ex)
            {
                ex.toString();
            }
        }

    }
}