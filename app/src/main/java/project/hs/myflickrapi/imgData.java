package project.hs.myflickrapi;

public class imgData {
    //http://farm{ farm }.static.flickr.com/{ server }/{ id }_{ secret }.jpg"
    String title;
    String farm;
    String server;
    String id;
    String secret;
    public imgData(String title, String farm, String server, String id, String secret) {
        this.title = title;
        this.farm = farm;
        this.server = server;
        this.id = id;
        this.secret = secret;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFarm() {
        return farm;
    }

    public void setFarm(String farm) {
        this.farm = farm;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
