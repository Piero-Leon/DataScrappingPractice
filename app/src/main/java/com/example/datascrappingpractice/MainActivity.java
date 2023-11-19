package com.example.datascrappingpractice;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParseAdapter adapter;
    private ArrayList<ParseItem> parseItems = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParseAdapter(parseItems, this);
        recyclerView.setAdapter(adapter);

        Content content = new Content();
        content.execute();
    }
    private class Content extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled(){
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //FOCUS ON THIS PART. ignore everything else.Should be just this and the parse_item.xml file
            try {
                String url = "https://boardgamegeek.com/browse/boardgame";      //1st use the browse page to see a list of the BG
                Document doc = Jsoup.connect(url).get();                        //connect it to Jsoup so you can interact

                Elements data = doc.select("td.collection_thumbnail");  //putting all the data of the list of BG

                int size = data.size();     //basically tells me the number of rows = total BG

                //iterate through the rows that are shown in https://boardgamegeek.com/browse/boardgame
                for (int i = 0; i < size; i++) {
                    String boardgameLink = data.select("td.collection_thumbnail")       //the BG images were too small on the browse page and it made it blurry if I enlarged them,
                            .select("a")                                                //so instead I got the URL of the BG pages individually
                            .eq(i)                                                            //this is like clicking on the boardgame on the website,
                            .attr("href");                                          //and going to the respective pages

                    String completeUrl = "https://boardgamegeek.com" + boardgameLink;         //for example in the 1st iteratinon we get the URL for the 1st BG "https://boardgamegeek.com/boardgame/224517/brass-birmingham" on the list

                    // Log the completeUrl, this was just ot see if I did right and got the correct link (ignore this)
                    Log.d("items", "Complete URL: " + completeUrl);

                    Document BGdoc = Jsoup.connect(completeUrl).get();      //connected to Jsoup again

                    String imgUrl = BGdoc.select("meta[property=og:image]")         //found the part of the website that haf the img (these images are much bigger and clearer)
                            .attr("content");


                    String title = data.select("h4.gridminfotitle")             //this is a place holder (doesn't do anything), you would need to find the title in the html. I think its towards the top in the respective BG page
                            .select("span")
                            .eq(i)
                            .text();

                    parseItems.add(new ParseItem(imgUrl, title));
                    Log.d("items", "img: " + imgUrl + " . title: " + title);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}