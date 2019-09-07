package com.example.guessthecelebrity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    /* object declarations */
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    ImageView imageView;
    LinearLayout linearLayout;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    /* variable declarations */
    int chosenCeleb;
    int locOFAns;
    String[] answers = new String[4];


    /* subclass for downloading image */
    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                // get passed url
                URL url = new URL(urls[0]);

                // create connection and connect to url
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // get input stream
                InputStream inputStream = connection.getInputStream();

                // create bitmap from the input stream
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }
    }


    /*class to download content of website */
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                // get the passed URL
                url = new URL(urls[0]);

                // activate url connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                // get input stream
                InputStream inputStream = urlConnection.getInputStream();

                // reader for the input stream
                InputStreamReader reader = new InputStreamReader(inputStream);

                // read first bit of data
                int data = reader.read();

                while(data != -1){

                    // cast data as a char
                    char current = (char) data;

                    // append to result
                    result += current;

                    // continue on
                    data = reader.read();

                }

                return result;

            } catch (Exception e){ // general exception handler

                e.printStackTrace();

            }

            return null;

        }
    }


    /* Run on creation of application  */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find views
        imageView = (ImageView) findViewById(R.id.celebPic);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        button1 = (Button) findViewById(R.id.button0);
        button2 = (Button) findViewById(R.id.button1);
        button3 = (Button) findViewById(R.id.button2);
        button4 = (Button) findViewById(R.id.button3);

        // create downloadTask class
        DownloadTask downloadTask = new DownloadTask();

        // string to store result in
        String result;

        // get website data
        try {

            // get the html from the URL
            result = downloadTask.execute("http://www.posh24.se/kandisar").get();
            if(result == null) {
                Log.i("jfdksal", "not here");
            }
            // isolate information about celebrities and their pictures
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            // gets the celebrity picture URL's
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()){
                celebURLs.add(m.group(1));
            }

            // gets the celebrity names
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while(m.find()){
                celebNames.add(m.group(1));
            }

            generateQuestion();

            /*EXCEPTION CATCHING*/
        } catch (ExecutionException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            e.printStackTrace();

        }
    }


    /* user has chosen a celebrity as their guess */
    public void celebChosen(View view){

        if (view.getTag().toString().equals(Integer.toString(locOFAns))){
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "wrong, it was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();

        }
        generateQuestion();

        // generate new question

    }


    /* Generate a new question */
    public void generateQuestion(){

        // generate new random number within size of celebrity list and have it be the answer for the current question
        Random rand = new Random();
        chosenCeleb = rand.nextInt(celebURLs.size());

        // randomly choose and set the image
        DownloadImage task = new DownloadImage();
        Bitmap celebImage;
        try {
            celebImage = task.execute(celebURLs.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);

            int incorrectAnswerLocation;

            // randomly set 1 of the 4 buttons to the answer
            locOFAns = rand.nextInt(4);
            for (int i = 0; i < 4; i++) {

                // place the correct answer in randomly generated index
                if (i == locOFAns) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {

                    // choose a random celebrity for one of the incorrect answers
                    incorrectAnswerLocation = rand.nextInt(celebURLs.size());

                    // handles randomly generated incorrect answers that are the same as the actual correct answer
                    while (incorrectAnswerLocation == chosenCeleb) {

                        incorrectAnswerLocation = rand.nextInt(celebURLs.size());

                    }

                    // generates new incorrect answer for duplicate incorrect answers
                    for(int j = 0; j < answers.length; j++){
                        while(answers[j] == celebNames.get(incorrectAnswerLocation)){
                            incorrectAnswerLocation = rand.nextInt(celebURLs.size());

                        }
                    }

                    // place name of an incorrect answer at index i
                    answers[i] = celebNames.get(incorrectAnswerLocation);

                }
            }

            // update buttons
            button1.setText(answers[0]);
            button2.setText(answers[1]);
            button3.setText(answers[2]);
            button4.setText(answers[3]);

        } catch (ExecutionException e){

            e.printStackTrace();

        } catch(InterruptedException e){

            e.printStackTrace();

        }
    }
}
