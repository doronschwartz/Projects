package edu.yu.cs.intro.doomGame;
import java.util.SortedSet;
import java.util.*;

/**
 * A specific monster
 */
public class Monster implements Comparable<Monster>{
    protected  MonsterType type;
    protected MonsterType customProtectedBy;
    protected int currenthealth;
    protected Room room;
    /**
     * create a monster with no customr protectors; its protectors will be determined by its MonsterType
     * @param type the type of monster to create
     */
    protected Monster(MonsterType type){
          this.type = type;
          this.currenthealth = type.ammunitionCountNeededToKill;
    }
    /**
     * create a monster with a custom protector, i.e. a different protector than the one specified in its MonsterType
     * @param type
     * @param customProtectedBy
     */
    public Monster(MonsterType type, MonsterType customProtectedBy){
        this.type = type;
        this.currenthealth = type.ammunitionCountNeededToKill;
        this.customProtectedBy = customProtectedBy;
        //**
}

    /**
     * set the room that the Monster is located in
     * @param room
     */
    protected void setRoom(Room room){
      this.room = room;
  }


    public MonsterType getMonsterType(){
      return this.type;
    }

    /**
     * Attack this monster with the given weapon, firing the given number of rounds at it
     * @param weapon
     * @param rounds
     * @return indicates if the monster is dead after this attack
     * @throws IllegalArgumentException if the weapon is one that dones't hurt this monster, if the weapon is null, or if rounds < 1
     * @throws IllegalStateException if the monster is already dead
     */
    protected boolean attack(Weapon weapon, int rounds){
      if(weapon != this.type.weaponNeededToKill || weapon == null || rounds < 1)
      {
        throw new IllegalArgumentException();
      }
      if(this.isDead()){
        throw new IllegalStateException();
      }
      if(this.currenthealth - rounds <= 0){
        //add to dead monsters List
        this.currenthealth = 0;
        return true;
      }
      else
      {
        this.currenthealth = this.currenthealth - rounds;
        return false;
      }

    }

    /**
     * @return is this monster dead?
     */

    public boolean isDead(){
       return this.currenthealth <= 0;
    }

    /**
     * if this monster has its customProtectedBy set, return it. Otherwise, return the protectedBy of this monster's type
     * @return
     */
    public MonsterType getProtectedBy(){
      if(customProtectedBy!=null){
        return this.customProtectedBy;
      }
      else{
        return this.type.getProtectedBy();
      }
    }

    /**
    * Used to sort a set of monsters into the order in which they must be killed, assuming they are in the same room.
    * If the parameter refers to this monster, return 0
    * If this monster is protected by the other monster's type, return 1
    * If this monster's type protects the other monster, return -1
    * If(this.hashCode() < other.hashCode()), then return -1
    * Otherwise, return 1
    * @param other the other monster
    * @return see above
     */
     public int compareTo(Monster other) {
     if(other == this){
         return 0;
     }else if(this.getProtectedBy() == other.getMonsterType()){
         return 1;
     }else if(other.getProtectedBy() == this.getMonsterType()){
         return -1;
     }else if(this.getMonsterType().ordinal() < other.getMonsterType().ordinal()){
         return -1;
     }else if(this.getMonsterType().ordinal() > other.getMonsterType().ordinal()){
         return 1;
     }else if(this.hashCode() < other.hashCode()){
         return -1;
     }
     return 1;
 }


}
