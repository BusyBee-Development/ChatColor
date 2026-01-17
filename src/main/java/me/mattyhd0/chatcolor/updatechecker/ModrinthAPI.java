package me.mattyhd0.chatcolor.updatechecker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ModrinthAPI {

    public static ModrinthVersion getLatestVersion(String slug){

        StringBuilder response = new StringBuilder();

        try {

            URL urlObject = new URL("https://api.modrinth.com/v2/project/"+slug+"/version");
            URLConnection urlConnection = urlObject.openConnection();
            urlConnection.setRequestProperty("User-Agent", "MattyHD0/ChatColor");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {

                response.append(line);

            }

            bufferedReader.close();

        } catch (IOException e){
            return null;
        }


        Gson gson = new Gson();
        Type listType = new TypeToken<List<ModrinthVersion>>(){}.getType();
        List<ModrinthVersion> versions = gson.fromJson(response.toString(), listType);
        
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0);
        }
        return null;

    }


}
