/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package management.model;

/**
 *
 * @author limmi
 */
public class Room {
    private String id;
    private boolean blocked;

    public Room(String id, boolean blocked) {
        this.id = id;
        this.blocked = blocked;
    }
    public String getId() { return id; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    @Override public String toString() { return id + (blocked ? " (차단)" : ""); }
}
