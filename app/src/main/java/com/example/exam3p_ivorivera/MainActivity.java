package com.example.exam3p_ivorivera;

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    static final int REQUEST_IMAGE = 1;
    static final int ACCESS_CAMERA = 101;
    public List<Entrevista> listadeentrevistas = new ArrayList<Entrevista>();
    ArrayAdapter<Entrevista> arrayAdapterEntrevista;
    Entrevista entrevistaSelected;

    ListView listViewEntrevistas;

    public MediaRecorder grabacion;
    public String archivoSalida = null;
    public Button btnGrabar, btnplay, btnCrear, btnLimpiar, btnActualizar, btnEliminar;
    public ImageButton btnTomarfoto;
    public ImageView imageView;
    public EditText txtdescripcion, txtperiodista, txtfecha;

    String currentPhotoPath;
    ProgressDialog progressDialog;
    FirebaseFirestore mFirestore;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnGrabar = findViewById(R.id.btnGrabar);
        btnTomarfoto = findViewById(R.id.btnTomarfoto);
        btnplay = findViewById(R.id.btnPlay);
        btnCrear = findViewById(R.id.btnCrear);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        txtdescripcion = findViewById(R.id.txtdescripcion);
        txtperiodista = findViewById(R.id.txtPeriodista);
        txtfecha = findViewById(R.id.txtFecha);
        listViewEntrevistas = findViewById(R.id.listaEntrevista);
        imageView = findViewById(R.id.imgFoto);

        progressDialog = new ProgressDialog(this);

        mFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        listarEntrevistas();

        //Evento de clink en la lista
        listViewEntrevistas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                entrevistaSelected = (Entrevista) parent.getItemAtPosition(position);
                txtdescripcion.setText(entrevistaSelected.getDescripcion());
                txtperiodista.setText(entrevistaSelected.getPeriodista());
                txtfecha.setText(entrevistaSelected.getFecha());
            }
        });


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
        }



        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearEntrevista();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUpdate();
            }
        });
        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiar();
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDelete();
            }
        });


        btnTomarfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    PermisosCamera();
                } else {
                    PermisosCamera();
                }
            }

        });



    }//fin oncreate

    private void dialogDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar Registro!")
                .setMessage("Esta seguro que desea eliminar la Entrevista?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        limpiar();
                        dialog.dismiss();
                    }
                }).setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEntrevista();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteEntrevista() {
        Entrevista c = new Entrevista();
        c.setIdOrden(entrevistaSelected.getIdOrden());
        databaseReference.child("Entrevista").child(c.getIdOrden()).removeValue();
        Toast.makeText(MainActivity.this,"Registro Eliminado!", Toast.LENGTH_LONG).show();
        limpiar();
    }

    private void dialogUpdate() {
        new AlertDialog.Builder(this)
                .setTitle("Actualizacion De Registros!")
                .setMessage("Esta seguro de actualizar los datos?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        limpiar();
                        dialog.dismiss();
                    }
                }).setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actualizarEntrevista();
                        limpiar();
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private void actualizarEntrevista() {
        Entrevista c = new Entrevista();
        c.setIdOrden(entrevistaSelected.getIdOrden());
        c.setDescripcion(txtdescripcion.getText().toString().trim());
        c.setPeriodista(txtperiodista.getText().toString().trim());
        c.setFecha(txtfecha.getText().toString().trim());
        databaseReference.child("Entrevista").child(c.getIdOrden()).setValue(c);
        Toast.makeText(MainActivity.this,"Datos Actualizados!", Toast.LENGTH_LONG).show();

    }

    private void listarEntrevistas() {
        databaseReference.child("Entrevista").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listadeentrevistas.clear();
                for(DataSnapshot objSnapshot : snapshot.getChildren()){
                    Entrevista c = objSnapshot.getValue(Entrevista.class); //*retorna todo el objeto llamdao c*/
                    listadeentrevistas.add(c);

                    arrayAdapterEntrevista = new ArrayAdapter<Entrevista>( MainActivity.this, android.R.layout.simple_list_item_1,listadeentrevistas);
                    listViewEntrevistas.setAdapter(arrayAdapterEntrevista);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void crearEntrevista() {

        String descripcion = txtdescripcion.getText().toString().trim();
        String periodista = txtperiodista.getText().toString().trim();
        String fecha = txtfecha.getText().toString().trim();
        String foto = "";
        String audio = "";

        if (descripcion.equals("")) {
            txtdescripcion.setError("Debe escribir una descripcion!");
        } else if (periodista.equals("")) {
            txtperiodista.setError("Ingrese un periodista!");
        }else if (fecha.equals("")) {
            txtfecha.setError("Ingrese un fecha!");
        }else{
            progressDialog.setMessage("Registrando Entrevista");
            progressDialog.setTitle("Registro");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            Entrevista c = new Entrevista();
            c.setIdOrden(UUID.randomUUID().toString());
            c.setDescripcion(descripcion);
            c.setPeriodista(periodista);
            c.setFecha(fecha);
            c.setFoto(foto);
           // c.setFoto(ConvertImageBase64(currentPhotoPath));
            c.setAudio(audio);

            databaseReference.child("Entrevista").child(c.getIdOrden()).setValue(c);

            progressDialog.dismiss();

            Toast.makeText(MainActivity.this,"Registro Exitso!", Toast.LENGTH_SHORT).show();
            limpiar();

        }
    }

    private void limpiar() {
        txtdescripcion.requestFocus();
        txtdescripcion.setText("");
        txtperiodista.setText("");
        txtfecha.setText("");
    }




    /*===================================METEDO PARA GRABAR==============================================================*/
    public void Recorder(View view){
        if(grabacion == null){
            archivoSalida = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Grabacion.mp3";
            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            grabacion.setOutputFile(archivoSalida);

            try{
                grabacion.prepare();
                grabacion.start();
            } catch (IOException e){
            }

            btnGrabar.setBackgroundResource(R.drawable.rec);
            Toast.makeText(getApplicationContext(), "Grabando...", Toast.LENGTH_SHORT).show();
        } else if(grabacion != null){
            grabacion.stop();
            grabacion.release();
            grabacion = null;
            btnGrabar.setBackgroundResource(R.drawable.stop_rec);
            Toast.makeText(getApplicationContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show();
        }
    }


    public void reproducir(View view) {

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(archivoSalida);
            mediaPlayer.prepare();
        } catch (IOException e){
        }

        mediaPlayer.start();
        Toast.makeText(getApplicationContext(), "Reproduciendo audio", Toast.LENGTH_SHORT).show();
    }
    
    
    
    /*=============================================METODOS PAR TOMAR FOTO============================================*/
    private void PermisosCamera()
    {
        // Metodo para obtener los permisos requeridos de la aplicacion
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ACCESS_CAMERA)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "se necesita el permiso de la camara",Toast.LENGTH_LONG).show();
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
                        "com.example.exam3p_ivorivera.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE)
        {
            try {
                File foto = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(foto));
            }
            catch (Exception ex)
            {
                ex.toString();
            }
        }
    }


    private String ConvertImageBase64(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imagearray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imagearray, Base64.DEFAULT);

    }

}