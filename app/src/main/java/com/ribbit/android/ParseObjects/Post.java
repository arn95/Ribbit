package com.ribbit.android.ParseObjects;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by ArnoldB on 3/13/2015.
 */
@ParseClassName("Post")
public class Post extends ParseObject{

    public Post(String theClassName) {
        super(theClassName);
    }

    public Post() {
        super();
    }

    public ParseFile getFile() {
        return getParseFile("file");
    }

    public void setFile(ParseFile file) {
        put("file",file);
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

    public void setThumbnail(ParseFile thumbnail) {
        put("thumbnail",thumbnail);
    }

    public String getType() {
       return getString("type");
    }

    public void setType(String type) {
        put("type",type);
    }

    public ParseUser getOwner() {
        return getParseUser("owner");
    }

    public void setOwner(ParseUser owner) {
        put("owner",owner.getUsername());
    }

}
