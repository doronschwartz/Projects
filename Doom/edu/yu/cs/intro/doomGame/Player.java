package edu.yu.cs.intro.doomGame;
import java.util.SortedSet;
import java.util.Map;
import java.util.*;
/**
 * Represents a player in the game.
 * A player whose health is <= 0 is dead.
 */
public class Player implements Comparable<Player> {
      protected String name;
      protected int health;
      protected SortedSet<Weapon> weapons = new TreeSet();
      protected Map<Weapon,Integer> weapontoAmmunition = new TreeMap();
    /**
     * @param name the player's name
     * @param health the player's starting health level
     */
    public Player(String name, int health) {
        this.name = name;
        this.health = health;
        this.weapons.add(Weapon.FIST);
        this.weapontoAmmunition.put(Weapon.FIST,Integer.MAX_VALUE);
    }

    public String getName(){
      return this.name;
    }

    /**
     * does this player have the given weapon?
     * @param w
     * @return
     */
    public boolean hasWeapon(Weapon w){
          return weapons.contains(w);
    }

    /**
     * how much ammunition does this player have for the given weapon?
     * @param w
     * @return
     */
    public int getAmmunitionRoundsForWeapon(Weapon w){
        return weapontoAmmunition.get(w);
    }

    /**
     * Change the ammunition amount by a positive or negative amount
     * @param weapon weapon whose ammunition count is to be changed
     * @param change amount to change ammunition count for that weapon by
     * @return the new total amount of ammunition the player has for the weapon.
     */
    public int changeAmmunitionRoundsForWeapon(Weapon weapon, int change){
      int oldammunition = weapontoAmmunition.get(weapon);
      int newammuntion = oldammunition + change;
      weapontoAmmunition.put(weapon,newammuntion);
      return weapontoAmmunition.get(weapon);
      }

    /**
     * A player can have ammunition for a weapon even without having the weapon itself.
     * @param weapon weapon for which we are adding ammunition
     * @param rounds number of rounds of ammunition to add
     * @return the new total amount of ammunition the player has for the weapon
     * @throws IllegalArgumentException if rounds < 0 or weapon is null
     * @throws IllegalStateException if the player is dead
     */
    protected int addAmmunition(Weapon weapon, int rounds){
      if(rounds < 0 || weapon == null){
          throw new IllegalArgumentException();
        }
      if(this.isDead()){
          throw new IllegalStateException();
        }
      if(this.weapontoAmmunition.keySet().contains(weapon)){
        this.changeAmmunitionRoundsForWeapon(weapon,rounds);
      }
      else{
        this.weapontoAmmunition.put(weapon,rounds);
      }

      return this.weapontoAmmunition.get(weapon);
    }

    /**
     * When a weapon is first added to a player, the player should automatically be given 5 rounds of ammunition.
     * If the player already has the weapon before this method is called, this method has no effect at all.
     * @param weapon
     * @return true if the weapon was added, false if the player already had it
     * @throws IllegalArgumentException if weapon is null
     * @throws IllegalStateException if the player is dead
     */
    protected boolean addWeapon(Weapon weapon){
      if(weapon == null)
        {
          throw new IllegalArgumentException();
        }
      if(this.isDead())
        {
          throw new IllegalStateException();
        }
      if(this.weapons.add(weapon)){
        this.addAmmunition(weapon,5);
        return true;
      }
      else{
        return false;
      }
    }

    /**
     * Change the player's health level
     * @param amount a positive or negative number, to increase or decrease the player's health
     * @return the player's health level after the change
     * @throws IllegalStateException if the player is dead
     */
    public int changeHealth(int amount){
      this.health = this.health + amount;
      return this.health;
    }

    /**
     * set player's current health level to the given level
     * @param amount
     */
    protected void setHealth(int amount){
      this.health = amount;
    }

    /**
     * get the player's current health level
     * @return
     */
    public int getHealth(){
      return this.health;
    }

    /**
     * is the player dead?
     * @return
     */
    public boolean isDead(){
        return this.health <= 0;
    }

    /**
     * Compare criteria, in order:
     * Does one have a greater weapon?
     * If they have the same greatest weapon, who has more ammunition for it?
     * If they are the same on weapon and ammunition, who has more health?
     * If they are the same on greatest weapon, ammunition for it, and health, they are equal.
     * Recall that all enums have a built-in implementation of Comparable, and they compare based on ordinal()
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Player other) {
        if(this.weapons.last().ordinal() > other.weapons.last().ordinal()){
          return 1;
        }
        else if(this.weapons.last().ordinal() < other.weapons.last().ordinal()){
          return -1;
        }

        if(this.getAmmunitionRoundsForWeapon(this.weapons.last()) > other.getAmmunitionRoundsForWeapon(other.weapons.last())) {
          return 1;
        }
        else if(this.getAmmunitionRoundsForWeapon(this.weapons.last()) < other.getAmmunitionRoundsForWeapon(other.weapons.last())){
          return -1;
        }

        if(this.getHealth() > other.getHealth()){
          return 1;
        }
        else if(this.getHealth() < other.getHealth()){
          return -1;
        }
        else{
          return 0;
        }

    }

    /**
     * Only equal if it is literally the same player
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    /**
     * @return the hash code of the player's name
     */
    @Override
    public int hashCode() {
      return this.getName().hashCode();
    }

}
