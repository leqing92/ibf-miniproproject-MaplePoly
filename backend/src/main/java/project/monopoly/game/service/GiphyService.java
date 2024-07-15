package project.monopoly.game.service;

import java.io.StringReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class GiphyService {
    @Value("${giphy.key}")
    private String giphyKey;
    private final String BASE_URL="https://api.giphy.com/v1/gifs/search";

    public List<String> search(String q, int limit){

        String url = UriComponentsBuilder
                        .fromUriString(BASE_URL)
                        .queryParam("q", q.replaceAll(" ", "+"))                        
                        .queryParam("limit", 25)
                        .queryParam("api_key", giphyKey)
                        .toUriString();
        
        RequestEntity<Void> req = RequestEntity
                                    .get(url)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .build();

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> resp = null;
        try {
            resp = template.exchange(req, String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return List.of();
        }

        JsonReader reader = Json.createReader(new StringReader(resp.getBody()));
        JsonObject giphyResp = reader.readObject(); 
        // eg of structure : https://api.giphy.com/v1/gifs/search?api_key=tmYYz3vSBNVJN5EkzU5snDyB54qTXSVe&q=polar+bear&limit=25&offset=0&rating=g&lang=en&bundle=messaging_non_clips
        
        return giphyResp.getJsonArray("data").stream()
         .map(item -> item.asJsonObject())
         .map(json -> json.getJsonObject("images")
               .getJsonObject("fixed_height_small").getString("url"))
         .toList();
    }
}
