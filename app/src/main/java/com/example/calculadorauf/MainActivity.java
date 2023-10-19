package com.example.calculadorauf;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;

    public void conseguirUF(int dia, int mes, int ano) {


        // Solicitar información de la API

        RequestQueue queue = Volley.newRequestQueue(this);
        String date = String.format("%02d-%02d-%04d", dia, mes + 1, ano);
        String url ="https://mindicador.cl/api/uf/" + date;

        // Solicitar la información en forma de String

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            EditText valorusuario = findViewById(R.id.montouf);
                            TextView valorclpfinal = findViewById(R.id.montoclp);

                            // Convertir la respuesta a un JSONObject para conseguir separarlo
                            JSONObject jsonObject = new JSONObject(response);

                            // Conseguir la serie de JSONArray
                            JSONArray serieArray = jsonObject.getJSONArray("serie");

                            // Conseguir el primer objeto del array "serie"
                            JSONObject serieObject = serieArray.getJSONObject(0);

                            // Conseguir el "valor" del objeto transformado
                            double valor = serieObject.getDouble("valor");

                            // Conseguir el TextView de UF a la fecha
                            TextView textView = findViewById(R.id.uffecha);

                            // Setear el valor
                            textView.setText(String.valueOf(valor));

                            double userInputValue = 0;

                            // Conseguir el monto de UF usuario
                            try {
                                userInputValue = Double.parseDouble(valorusuario.getText().toString());
                            } catch (NumberFormatException e) {
                                // Agarrar el 0 del principio

                            }

                            // Calcular multiplicación
                            double resultValue = valor * userInputValue;

                            DecimalFormat df = new DecimalFormat("#.##");

                            // Actualizar el textView
                            valorclpfinal.setText(df.format(resultValue));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "That didn't work!");
            }
        });

        // Añadir la solicitud a una "queue"
        queue.add(stringRequest);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new DatabaseHelper(this);

        // Calendario para la aplicación

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        final EditText etFecha = findViewById(R.id.etFecha);
        etFecha.setText(String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year));

        conseguirUF(dayOfMonth, month, year);

        TableLayout tableLayout = findViewById(R.id.tablaregistrouf);

        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            // Por si no se encuentra información
            return;
        }

        // Setear la fila que se creara

        while (res.moveToNext()) {
            TableRow tableRow = new TableRow(this);

            TextView textView1 = new TextView(this);
            textView1.setText(res.getString(1));  // Hoy
            tableRow.addView(textView1);

            TextView textView2 = new TextView(this);
            textView2.setText(res.getString(2));  // Fecha usuario
            tableRow.addView(textView2);

            TextView textView3 = new TextView(this);
            textView3.setText(res.getString(3));  // Valor UF
            tableRow.addView(textView3);

            TextView textView4 = new TextView(this);
            double resultValue = res.getDouble(4);  // Valor resultado
            String formattedResultValue = String.format("%.2f", resultValue);  // Darle formato al resultado
            textView4.setText(formattedResultValue);  // Setear en textView
            tableRow.addView(textView4);

            tableLayout.addView(tableRow);
        }

        // onClick para establecer fecha

        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etFecha.setText(String.format("%02d-%02d-%04d", dayOfMonth, monthOfYear + 1, year));

                                conseguirUF(dayOfMonth, month, year);

                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        // Encontrar los textos para detectar cambios
        final TextView uffecha = findViewById(R.id.uffecha);
        final EditText valorusuario = findViewById(R.id.montouf);
        final TextView valorclpfinal = findViewById(R.id.montoclp);

        // Metodo para detectar cambios y actualizar valores en cadena en tiempo real
        valorusuario.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nose si esto es necesario, pero esta de todas formas
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nose si esto es necesario, pero esta de todas formas

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {

                    double valor = Double.parseDouble(uffecha.getText().toString());
                    double userValue = Double.parseDouble(s.toString());
                    double result = valor * userValue;

                    Log.d("SaveButton", "Result Value: " + result);

                    // Darle formato decimal al resultado
                    String formattedResult = String.format("%.2f", result);

                    valorclpfinal.setText(formattedResult);

                } catch (NumberFormatException e) {
                    // Excepción por si no es valido
                    e.printStackTrace();
                }
            }
        });

        Button guardar = findViewById(R.id.guardarbutton);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                String todayDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year);

                boolean isInserted = myDb.insertData(
                        todayDate,
                        etFecha.getText().toString(),
                        Double.parseDouble(uffecha.getText().toString()),
                        Double.parseDouble(valorclpfinal.getText().toString())
                );
                if(isInserted)
                    Toast.makeText(MainActivity.this,"Datos ingresados",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MainActivity.this,"Datos no ingresados",Toast.LENGTH_LONG).show();

                // Limpiar tabla
                for (int i = tableLayout.getChildCount() - 1; i > 0; i--) {
                    tableLayout.removeViewAt(i);
                }

                // Llenar tabla
                Cursor res = myDb.getAllData();
                while (res.moveToNext()) {
                    TableRow tableRow = new TableRow(MainActivity.this);

                    TextView textView1 = new TextView(MainActivity.this);
                    textView1.setText(res.getString(1));  // Hoy
                    tableRow.addView(textView1);

                    TextView textView2 = new TextView(MainActivity.this);
                    textView2.setText(res.getString(2));  // Fecha usuario
                    tableRow.addView(textView2);

                    TextView textView3 = new TextView(MainActivity.this);
                    textView3.setText(res.getString(3));  // Valor API
                    tableRow.addView(textView3);

                    TextView textView4 = new TextView(MainActivity.this);
                    double resultValue = res.getDouble(4);  // Conseguir el resultado
                    String formattedResultValue = String.format("%.2f", resultValue);  // Darle formato al resultado
                    textView4.setText(formattedResultValue);  // Setear en textView
                    tableRow.addView(textView4);

                    tableLayout.addView(tableRow);
                }
            }
        });

    }
}