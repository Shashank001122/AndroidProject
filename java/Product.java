package com.londonappbrewery.climapm.Utils;

public class Product {

    public String imagenames;

    public String images;

    public String sectionNames;

    public String publication;
    public String id;

    public boolean paused = true;
    public boolean faved = false;

    private int favId;
    public Product(String imagenames, String images, String sectionNames, String publication, String id) {
        this.imagenames = imagenames;
        this.images = images;
        this.sectionNames = sectionNames;
        this.publication = publication;
        this.id=id;
}

    public int getFavId() {
        return favId;}
    public void setFavId(int favId) {
        this.favId = favId;}


    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Product)) {
            return false;
        }

        Product prod = (Product) o;

        return prod.id
                .equals(id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();

        return result;
    }

}
