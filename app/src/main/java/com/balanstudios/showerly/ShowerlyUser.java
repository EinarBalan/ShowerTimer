package com.balanstudios.showerly;

public class ShowerlyUser {

    private String displayName;
    private String email;
    private double avgShowerLength;
    private int position = 0;

    public ShowerlyUser(String email, String displayName, double avgShowerLength) {
        this.email = email;
        this.displayName = displayName;
        this.avgShowerLength = avgShowerLength;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getAvgShowerLength() {
        return avgShowerLength;
    }

    public void setAvgShowerLength(double avgShowerLength) {
        this.avgShowerLength = avgShowerLength;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public String toString(){
        return "Display Name: " + displayName + ", Position: "  + position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean equals(ShowerlyUser user){
        return (this.email.equals(user.getEmail()));
    }
}
