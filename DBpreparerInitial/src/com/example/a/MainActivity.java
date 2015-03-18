package com.example.a;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.R.menu;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        new Thread(new Runnable() {
			 
			@Override
			public void run() {

				ArrayList<Movie> MovieList;
				try {
					MovieList = retrieveMoviesList(""); 
					MovieDbHelper dbHelper = new MovieDbHelper(MainActivity.this);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					ContentValues values = new ContentValues(); 
					
//					ArrayList<ArrayList<String>> trialList = new ArrayList<ArrayList<String>>();
//					ArrayList<String> trialVoteList = new ArrayList<String>();
//					ArrayList<String> trialTitleList = new ArrayList<String>();
					
					for(int i = 0 ; i < MovieList.size() ; i ++){
						 
						System.out.println("Ankur insert data into DB");
						Movie movieFetched = MovieList.get(i);
						
						values.put("entryid", movieFetched.entryId); 
						values.put("title", movieFetched.title);
						values.put("votecount", movieFetched.votecount);
						
//						System.out.println("Data Crunching: #"+i + " Title = "+movieFetched.title + " Votecount = "+movieFetched.votecount);
						
//						trialTitleList.add(i, movieFetched.title);
//						trialVoteList.add(i, Integer.toString(movieFetched.votecount));
//						
//						trialList.add(0,trialTitleList);
//						trialList.add(0,trialVoteList);
						
						db.insert("entry", null, values);
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}
		}).start();
        
    }

    
    static int pageNo = 0;
static int count = 0;
    public static ArrayList<Movie> parseMovieResponse(String jsonStr) {
		
    	try{
		
    		JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray results = jsonObject.getJSONArray("results");
			
			ArrayList<Movie> completeMovieList = new ArrayList<Movie>();
			for(int i = 0; i < results.length(); i++){
				 
				Movie movie = new Movie();
				
				JSONObject result = results.getJSONObject(i);
				
				//Handle title
				movie.title = result.getString("title");
				movie.votecount = result.getInt("vote_count"); 

				
				String[] movieWords = null;
				String jumbledMovieName = "";  
				
				if ( 
						( movie.title .length() < 12 ) 
						&& ( movie.title .matches("[a-zA-Z0-9.? ]*") ) 
						&& (!movie.title .toLowerCase().contains("sex"))  
						&& (!movie.title .toLowerCase().contains("dick"))  
						&& (!movie.title .toLowerCase().contains("ass")) 
						&& (!movie.title .toLowerCase().contains("fuck")) 
						){
						
					count++;
//					System.out.println("ankur MOVIE NAME : "+ movie.title );
					System.out.println("Data Crunching: #"+count + " Title = "+movie.title + " Votecount = "+movie.votecount);
					
					//Handle votecount
					movie.votecount = result.getInt("vote_count");

					//Handle entryId
					movie.entryId = result.getInt("id");

					
					completeMovieList.add(movie); 
					
					/*movieWords = movie.title.split("\\s+");
					
					for(int j = 0 ; j < movieWords.length ; j++){
						
						jumbledMovieName += shuffle(movieWords[j]);
						
						if( j < (movieWords.length - 1) )
						jumbledMovieName += " ";
						
					}
					jumbledMoviesList.add(jumbledMovieName );*/
				}
			}

			long seed = System.nanoTime();
//			Collections.shuffle(nonjumbledMoviesList, new Random(seed));
//			Collections.shuffle(jumbledMoviesList, new Random(seed));
			
//			completeMovieList.add(0, nonjumbledMoviesList);
//			completeMovieList.add(1, jumbledMoviesList);
			
			return completeMovieList;

		}
		catch(JSONException e){
			System.out.println("ERROR: Response field changed !!");
			e.printStackTrace();
		}
		return null;
	}
    
  ArrayList<Movie> retrieveMoviesList(String query) throws JSONException{
        
    	
    	HttpRetriever httpRetriever = new HttpRetriever();
    	String url = constructSearchUrl(query, 1);
        String response = httpRetriever.retrieve(url);
        
        System.out.println("Ankur response fetched !!");
        JSONObject jsonObject = new JSONObject(response); 
		pageNo = jsonObject.getInt("total_pages");
		
		ArrayList<Movie> completeMovieList, additionalMovieList;
		completeMovieList = parseMovieResponse(response);
	      
		
		for(int i = 2 ; i <= pageNo ; i++ ){
			String url1 = constructSearchUrl(query, i);
	        String response1 = httpRetriever.retrieve(url1);
	        
	        additionalMovieList = parseMovieResponse(response1); 
	        completeMovieList.addAll(additionalMovieList); 
	        			
		}
        
        return completeMovieList;
    }
 
 private static String shuffle(String input){
     List<Character> characters = new ArrayList<Character>();
     for(char c:input.toCharArray()){
         characters.add(c);
     }
     StringBuilder output = new StringBuilder(input.length());
     while(characters.size()!=0){
         int randPicker = (int)(Math.random()*characters.size()); 
         output.append(characters.remove(randPicker));
     }
     return output.toString();
 }
 
 
// http://api.themoviedb.org/3/discover/movie?api_key=196527b28198a82e77196ba38b0d32fb&sort_by=vote_count.asc&language=en&vote_count.gte=50
 protected String constructSearchUrl(String query, int pageNo) {
     
     Uri.Builder builder = new Uri.Builder();
     builder.scheme("http")  
     .authority("api.themoviedb.org")  
     .appendPath("3")  
     .appendPath("discover")    
     .appendPath("movie")
     .appendQueryParameter("api_key", "7b5e30851a9285340e78c201c4e4ab99")
//     .appendQueryParameter("primary_release_year.lte", "2010")
     .appendQueryParameter("sort_by", "vote_count.desc")
//     certification_country=US&certification.gte=G
     .appendQueryParameter("certification_country", "US")
     .appendQueryParameter("certification.gte", "G") 
     .appendQueryParameter("country", "US")
     .appendQueryParameter("language", "en")
     .appendQueryParameter("vote_count.gte", "10")
     .appendQueryParameter("page", ""+pageNo);
     
     return builder.build().toString();
 }
}
