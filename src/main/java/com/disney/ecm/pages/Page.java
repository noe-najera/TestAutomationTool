package com.disney.ecm.pages;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Page {

    private String url="";
    private String locale="";
    private String entity="";
    private String urlFriendly="";
    private String environment="";
    private int imagesTotal=0;
    private int anchors=0;
    private String divDescription="";
    private ArrayList<String> imagesSrc=null;
    private ArrayList<String> hrefAnchors=null;
    private List<String> textsInPage=null;
    private List<String> urlImages=null;

    HashMap<String, String> altTextMap = null; //new HashMap<>();

    public Page(String url, String locale, String entity,String urlFriendly,String environment, List<String> textsInPage,List<String> urlImages,HashMap<String,String> altTextMap) {
        this.url=url;
        this.locale=locale;
        this.entity=entity;
        this.urlFriendly=urlFriendly;
        this.textsInPage=textsInPage;
        this.urlImages=urlImages;
        this.environment=environment;
        this.altTextMap= altTextMap;
    }

    public HashMap<String, String> getAltTextMap() {
        return altTextMap;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getLocale() {
        return locale;
    }

    public String getEntity() {
        return entity;
    }

    public String getUrlFriendly() {
        return urlFriendly;
    }

    public List<String> getUrlImages() {
        return urlImages;
    }

    public List<String> getTextsInPage() {
        return textsInPage;
    }

    public String getDivDescription() {
        return divDescription;
    }

    public void setDivDescription(String divDescription) {
        this.divDescription = divDescription;
    }

    public int getAnchors() {
        return anchors;
    }

    public void setAnchors(int anchors) {
        this.anchors = anchors;
    }

    public ArrayList<String> getHrefAnchors() {
        return hrefAnchors;
    }

    public void setHrefAnchors(ArrayList<String> hrefAnchors) {
        this.hrefAnchors = hrefAnchors;
    }

    public String getUrl() {
        return url;
    }

    public int getImagesTotal() {
        return imagesTotal;
    }

    public void setImagesTotal(int imagesTotal) {
        this.imagesTotal = imagesTotal;
    }

    public ArrayList<String> getImagesSrc() {
        return imagesSrc;
    }

    public void setImagesSrc(ArrayList<String> imagesSrc) {
        this.imagesSrc = imagesSrc;
    }
}